package com.meta.wearable.dat.externalsampleapps.displayaccess.navigation

import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.OpenAiRepository
import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.PlacesRepository
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.NavigationTarget
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.PlaceResolutionResult

class NavigationOrchestratorImpl(
    private val openAiRepository: OpenAiRepository,
    private val placesRepository: PlacesRepository,
) : NavigationOrchestrator {

  override suspend fun resolveDestination(utterance: String): PlaceResolutionResult {
    val parsedIntent =
        openAiRepository
            .parsePlaceIntent(utterance)
            .getOrElse { error ->
              return PlaceResolutionResult.AiFailure(error.message ?: "OpenAI request failed")
            }

    val candidates =
        placesRepository
            .search(parsedIntent.placeQuery)
            .getOrElse { error ->
              return PlaceResolutionResult.PlacesFailure(
                  parsedIntent,
                  error.message ?: "Places request failed",
              )
            }

    val selected =
        candidates.firstOrNull() ?: return PlaceResolutionResult.NoCandidates(parsedIntent)

    return PlaceResolutionResult.Success(
        parsedIntent = parsedIntent,
        target = NavigationTarget(place = selected, selectedAtMillis = System.currentTimeMillis()),
    )
  }
}
