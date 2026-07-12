package com.meta.wearable.dat.externalsampleapps.displayaccess.location

interface DirectionCalculator {
  fun bearingDegrees(
      fromLatitude: Double,
      fromLongitude: Double,
      toLatitude: Double,
      toLongitude: Double,
  ): Double

  fun distanceMeters(
      fromLatitude: Double,
      fromLongitude: Double,
      toLatitude: Double,
      toLongitude: Double,
  ): Double

  fun normalizeTurnAngle(targetBearingDegrees: Double, headingDegrees: Double): Double
}
