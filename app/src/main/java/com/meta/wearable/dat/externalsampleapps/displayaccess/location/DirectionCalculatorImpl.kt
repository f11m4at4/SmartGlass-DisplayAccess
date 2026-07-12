package com.meta.wearable.dat.externalsampleapps.displayaccess.location

import android.location.Location

class DirectionCalculatorImpl : DirectionCalculator {

  override fun bearingDegrees(
      fromLatitude: Double,
      fromLongitude: Double,
      toLatitude: Double,
      toLongitude: Double,
  ): Double {
    val results = FloatArray(3)
    Location.distanceBetween(fromLatitude, fromLongitude, toLatitude, toLongitude, results)
    val bearing = results[1].toDouble()
    return (bearing + 360.0) % 360.0
  }

  override fun distanceMeters(
      fromLatitude: Double,
      fromLongitude: Double,
      toLatitude: Double,
      toLongitude: Double,
  ): Double {
    val results = FloatArray(3)
    Location.distanceBetween(fromLatitude, fromLongitude, toLatitude, toLongitude, results)
    return results[0].toDouble()
  }

  override fun normalizeTurnAngle(targetBearingDegrees: Double, headingDegrees: Double): Double {
    var diff = (targetBearingDegrees - headingDegrees) % 360.0
    if (diff > 180.0) diff -= 360.0
    if (diff < -180.0) diff += 360.0
    return diff
  }
}
