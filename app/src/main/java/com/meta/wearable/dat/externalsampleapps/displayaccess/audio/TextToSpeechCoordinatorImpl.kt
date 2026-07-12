package com.meta.wearable.dat.externalsampleapps.displayaccess.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "TextToSpeechCoordinator"

class TextToSpeechCoordinatorImpl(context: Context) : TextToSpeechCoordinator {

  private val _isSpeaking = MutableStateFlow(false)
  override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private var isReady = false
  private var pendingText: String? = null
  private lateinit var tts: TextToSpeech

  init {
    tts =
        TextToSpeech(context.applicationContext) { status ->
          isReady = status == TextToSpeech.SUCCESS
          if (isReady) {
            tts.language = Locale.getDefault()
            pendingText?.let(::speak)
            pendingText = null
          } else {
            Log.e(TAG, "TextToSpeech initialization failed: $status")
          }
        }
    tts.setOnUtteranceProgressListener(
        object : UtteranceProgressListener() {
          override fun onStart(utteranceId: String?) {
            _isSpeaking.value = true
          }

          override fun onDone(utteranceId: String?) {
            _isSpeaking.value = false
          }

          @Suppress("OVERRIDE_DEPRECATION")
          override fun onError(utteranceId: String?) {
            _isSpeaking.value = false
          }

          override fun onError(utteranceId: String?, errorCode: Int) {
            _isSpeaking.value = false
          }
        },
    )
  }

  override fun speak(text: String) {
    if (!isReady) {
      pendingText = text
      return
    }
    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
  }

  override fun stop() {
    if (::tts.isInitialized) {
      tts.stop()
    }
    _isSpeaking.value = false
  }

  fun shutdown() {
    if (::tts.isInitialized) {
      tts.stop()
      tts.shutdown()
    }
  }
}
