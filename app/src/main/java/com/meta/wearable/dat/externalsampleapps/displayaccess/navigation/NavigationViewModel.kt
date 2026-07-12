package com.meta.wearable.dat.externalsampleapps.displayaccess.navigation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meta.wearable.dat.externalsampleapps.displayaccess.AppConfig
import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.OpenAiRepositoryImpl
import com.meta.wearable.dat.externalsampleapps.displayaccess.audio.SpeechRecognizerCoordinatorImpl
import com.meta.wearable.dat.externalsampleapps.displayaccess.audio.TextToSpeechCoordinatorImpl
import com.meta.wearable.dat.externalsampleapps.displayaccess.location.DirectionCalculator
import com.meta.wearable.dat.externalsampleapps.displayaccess.location.DirectionCalculatorImpl
import com.meta.wearable.dat.externalsampleapps.displayaccess.location.LocationRepositoryImpl
import com.meta.wearable.dat.externalsampleapps.displayaccess.location.SensorRepositoryImpl
import com.meta.wearable.dat.externalsampleapps.displayaccess.location.model.LocationSample
import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.PlacesRepositoryImpl
import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.StaticMapCachePolicy
import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.StaticMapRepositoryImpl
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.NavigationSnapshot
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.NavigationTarget
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.PlaceResolutionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NavigationViewModel(application: Application) : AndroidViewModel(application) {
  private val speechCoordinator = SpeechRecognizerCoordinatorImpl(application)
  private val ttsCoordinator = TextToSpeechCoordinatorImpl(application)
  private val navigationOrchestrator: NavigationOrchestrator =
      NavigationOrchestratorImpl(
          openAiRepository = OpenAiRepositoryImpl(apiKey = AppConfig.openAiApiKey),
          placesRepository = PlacesRepositoryImpl(apiKey = AppConfig.googleMapsApiKey),
      )

  private val locationRepository = LocationRepositoryImpl(application)
  private val sensorRepository = SensorRepositoryImpl(application)
  private val directionCalculator: DirectionCalculator = DirectionCalculatorImpl()
  private val staticMapRepository = StaticMapRepositoryImpl(apiKey = AppConfig.googleMapsApiKey)
  private val staticMapCachePolicy = StaticMapCachePolicy()

  private val _uiState = MutableStateFlow(NavigationUiState())
  val uiState: StateFlow<NavigationUiState> = _uiState.asStateFlow()

  private var started = false
  private var lastResolvedUtterance: String? = null
  private var isFetchingMiniMap = false

  fun start() {
    if (started) return
    started = true

    viewModelScope.launch {
      speechCoordinator.state.collect { speechState ->
        _uiState.update {
          it.copy(
              speech = speechState.speech,
              isListening = speechState.isListening,
              errorMessage = speechState.errorMessage,
          )
        }

        val finalText = speechState.speech.finalText
        if (finalText.isNotBlank() && finalText != lastResolvedUtterance) {
          lastResolvedUtterance = finalText
          resolveDestination(finalText)
        }
      }
    }

    viewModelScope.launch {
      ttsCoordinator.isSpeaking.collect { isSpeaking ->
        _uiState.update { it.copy(isSpeaking = isSpeaking) }
        if (isSpeaking) {
          speechCoordinator.pauseListening()
        } else {
          speechCoordinator.resumeListening()
        }
      }
    }

    viewModelScope.launch {
      combine(locationRepository.location, sensorRepository.headingDegrees) { location, heading ->
            location to heading
          }
          .collect { (location, heading) ->
            if (location != null && heading != null) {
              onLocationOrHeadingUpdated(location, heading)
            }
          }
    }

    speechCoordinator.startListening()
    locationRepository.startUpdates()
    sensorRepository.start()
  }

  fun startListening() {
    speechCoordinator.startListening()
  }

  fun stopListening() {
    speechCoordinator.stopListening()
  }

  fun speak(text: String) {
    ttsCoordinator.speak(text)
  }

  private fun resolveDestination(utterance: String) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(isResolvingDestination = true, aiErrorMessage = null, placesErrorMessage = null)
      }

      when (val result = navigationOrchestrator.resolveDestination(utterance)) {
        is PlaceResolutionResult.Success -> {
          _uiState.update {
            it.copy(
                parsedIntent = result.parsedIntent,
                target = result.target,
                miniMapState = null,
                isResolvingDestination = false,
            )
          }
        }
        is PlaceResolutionResult.AiFailure -> {
          _uiState.update {
            it.copy(aiErrorMessage = result.message, isResolvingDestination = false)
          }
          ttsCoordinator.speak("Sorry, I didn't catch that. Could you say your destination again?")
        }
        is PlaceResolutionResult.PlacesFailure -> {
          _uiState.update {
            it.copy(
                parsedIntent = result.parsedIntent,
                placesErrorMessage = result.message,
                isResolvingDestination = false,
            )
          }
          ttsCoordinator.speak("I couldn't find that place. Could you try a different destination?")
        }
        is PlaceResolutionResult.NoCandidates -> {
          _uiState.update {
            it.copy(
                parsedIntent = result.parsedIntent,
                placesErrorMessage = "No matching destination found",
                isResolvingDestination = false,
            )
          }
          ttsCoordinator.speak("I couldn't find that place. Could you try a different destination?")
        }
      }
    }
  }

  private fun onLocationOrHeadingUpdated(location: LocationSample, headingDegrees: Double) {
    val target = _uiState.value.target ?: return

    val bearing =
        directionCalculator.bearingDegrees(
            fromLatitude = location.latitude,
            fromLongitude = location.longitude,
            toLatitude = target.place.latitude,
            toLongitude = target.place.longitude,
        )
    val distance =
        directionCalculator.distanceMeters(
            fromLatitude = location.latitude,
            fromLongitude = location.longitude,
            toLatitude = target.place.latitude,
            toLongitude = target.place.longitude,
        )
    val turnAngle = directionCalculator.normalizeTurnAngle(bearing, headingDegrees)

    _uiState.update {
      it.copy(
          snapshot =
              NavigationSnapshot(
                  currentLatitude = location.latitude,
                  currentLongitude = location.longitude,
                  headingDegrees = headingDegrees,
                  targetBearingDegrees = bearing,
                  distanceMeters = distance,
                  turnAngleDegrees = turnAngle,
              ),
      )
    }

    maybeRefreshMiniMap(location, headingDegrees, target)
  }

  private fun maybeRefreshMiniMap(
      location: LocationSample,
      headingDegrees: Double,
      target: NavigationTarget,
  ) {
    if (isFetchingMiniMap) return
    val previous = _uiState.value.miniMapState
    val now = System.currentTimeMillis()
    if (!staticMapCachePolicy.shouldRefresh(
        previous = previous,
        currentLatitude = location.latitude,
        currentLongitude = location.longitude,
        currentHeadingDegrees = headingDegrees,
        nowMillis = now,
    )
    ) {
      return
    }

    isFetchingMiniMap = true
    viewModelScope.launch {
      staticMapRepository
          .createMap(location.latitude, location.longitude, headingDegrees, target)
          .onSuccess { newState ->
            _uiState.update { it.copy(miniMapState = newState, mapErrorMessage = null) }
          }
          .onFailure { error ->
            _uiState.update {
              it.copy(
                  miniMapState = it.miniMapState?.copy(isFallbackMode = true),
                  mapErrorMessage = error.message,
              )
            }
          }
      isFetchingMiniMap = false
    }
  }

  override fun onCleared() {
    speechCoordinator.stopListening()
    ttsCoordinator.shutdown()
    locationRepository.stopUpdates()
    sensorRepository.stop()
    super.onCleared()
  }
}
