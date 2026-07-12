package com.meta.wearable.dat.externalsampleapps.displayaccess.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.meta.wearable.dat.externalsampleapps.displayaccess.audio.model.RecognizedSpeech
import com.meta.wearable.dat.externalsampleapps.displayaccess.audio.model.SpeechRecognizerState
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val TAG = "SpeechRecognizerCoordinator"
private const val RESTART_DELAY_MILLIS = 350L

/**
 * Android's [SpeechRecognizer] only supports single-utterance recognition sessions, so continuous
 * listening is simulated by automatically restarting a new session after every result or error.
 */
class SpeechRecognizerCoordinatorImpl(private val context: Context) : SpeechRecognizerCoordinator {

  private val _state = MutableStateFlow(SpeechRecognizerState())
  override val state: StateFlow<SpeechRecognizerState> = _state.asStateFlow()

  private val mainHandler = Handler(Looper.getMainLooper())
  private var recognizer: SpeechRecognizer? = null
  private var isPaused = false
  private var isStopped = true

  private val recognizerIntent =
      Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
      }

  private val listener =
      object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() = Unit

        override fun onError(error: Int) {
          val isSilence =
              error == SpeechRecognizer.ERROR_NO_MATCH ||
                  error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
          Log.w(TAG, "Speech recognition error: ${describeError(error)}")
          _state.update {
            it.copy(isListening = false, errorMessage = if (isSilence) null else describeError(error))
          }
          scheduleRestart()
        }

        override fun onResults(results: Bundle?) {
          val text = results.bestMatch()
          if (text != null) {
            _state.update {
              it.copy(
                  speech =
                      RecognizedSpeech(
                          partialText = "",
                          finalText = text,
                          timestampMillis = System.currentTimeMillis(),
                      ),
                  errorMessage = null,
              )
            }
          }
          scheduleRestart()
        }

        override fun onPartialResults(partialResults: Bundle?) {
          val text = partialResults.bestMatch() ?: return
          _state.update {
            it.copy(
                speech =
                    it.speech.copy(partialText = text, timestampMillis = System.currentTimeMillis()),
            )
          }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
      }

  override fun startListening() {
    isStopped = false
    isPaused = false
    mainHandler.post { beginListening() }
  }

  override fun stopListening() {
    isStopped = true
    mainHandler.post {
      recognizer?.stopListening()
      recognizer?.destroy()
      recognizer = null
      _state.update { it.copy(isListening = false) }
    }
  }

  override fun pauseListening() {
    if (isStopped || isPaused) return
    isPaused = true
    mainHandler.post {
      recognizer?.stopListening()
      _state.update { it.copy(isListening = false) }
    }
  }

  override fun resumeListening() {
    if (isStopped || !isPaused) return
    isPaused = false
    mainHandler.post { beginListening() }
  }

  private fun beginListening() {
    if (isStopped || isPaused) return
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      _state.update {
        it.copy(isListening = false, errorMessage = "Speech recognition is not available")
      }
      return
    }
    val active =
        recognizer
            ?: SpeechRecognizer.createSpeechRecognizer(context).also {
              it.setRecognitionListener(listener)
              recognizer = it
            }
    _state.update { it.copy(isListening = true, errorMessage = null) }
    active.startListening(recognizerIntent)
  }

  private fun scheduleRestart() {
    if (isStopped || isPaused) return
    mainHandler.postDelayed({ beginListening() }, RESTART_DELAY_MILLIS)
  }

  private fun Bundle?.bestMatch(): String? =
      this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

  private fun describeError(error: Int): String =
      when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Missing microphone permission"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        else -> "Unknown speech recognizer error ($error)"
      }
}
