package com.meta.wearable.dat.externalsampleapps.displayaccess.audio

import kotlinx.coroutines.flow.StateFlow

interface TextToSpeechCoordinator {
  val isSpeaking: StateFlow<Boolean>

  fun speak(text: String)

  fun stop()
}
