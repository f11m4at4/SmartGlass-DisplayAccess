package com.meta.wearable.dat.externalsampleapps.displayaccess.location.model

data class LocationSample(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val timestampMillis: Long = 0L,
)
