package com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model

data class NavigationSnapshot(
    val currentLatitude: Double,
    val currentLongitude: Double,
    val headingDegrees: Double,
    val targetBearingDegrees: Double,
    val distanceMeters: Double,
    val turnAngleDegrees: Double,
)
