package com.meta.wearable.dat.externalsampleapps.displayaccess.audio.model

data class SpeechRecognizerState(
    val speech: RecognizedSpeech = RecognizedSpeech(),
    val isListening: Boolean = false,
    val errorMessage: String? = null,
)
