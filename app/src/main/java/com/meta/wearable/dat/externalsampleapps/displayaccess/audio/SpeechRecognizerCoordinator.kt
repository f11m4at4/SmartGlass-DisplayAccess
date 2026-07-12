package com.meta.wearable.dat.externalsampleapps.displayaccess.audio

import com.meta.wearable.dat.externalsampleapps.displayaccess.audio.model.SpeechRecognizerState
import kotlinx.coroutines.flow.StateFlow

interface SpeechRecognizerCoordinator {
  val state: StateFlow<SpeechRecognizerState>

  fun startListening()

  fun stopListening()

  fun pauseListening()

  fun resumeListening()
}
