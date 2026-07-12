package com.meta.wearable.dat.externalsampleapps.displayaccess.audio.model

data class RecognizedSpeech(
    val partialText: String = "",
    val finalText: String = "",
    val timestampMillis: Long = 0L,
)
