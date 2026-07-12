package com.meta.wearable.dat.externalsampleapps.displayaccess.maps

import android.location.Location
import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.MiniMapState

data class StaticMapCachePolicy(
    val minDistanceMeters: Double = 20.0,
    val minHeadingDeltaDegrees: Double = 20.0,
    val ttlMillis: Long = 15_000L,
) {

  /** Decides whether a static map re-fetch is warranted, or the cached [previous] state still applies. */
  fun shouldRefresh(
      previous: MiniMapState?,
      currentLatitude: Double,
      currentLongitude: Double,
      currentHeadingDegrees: Double,
      nowMillis: Long,
  ): Boolean {
    if (previous == null || previous.imageUrl.isBlank()) return true
    if (nowMillis - previous.lastUpdatedAtMillis >= ttlMillis) return true

    val lastLatitude = previous.lastRenderLatitude
    val lastLongitude = previous.lastRenderLongitude
    if (lastLatitude == null || lastLongitude == null) return true

    val distanceResults = FloatArray(1)
    Location.distanceBetween(lastLatitude, lastLongitude, currentLatitude, currentLongitude, distanceResults)
    if (distanceResults[0] >= minDistanceMeters) return true

    val lastHeading = previous.lastRenderHeadingDegrees ?: return true
    val headingDelta = kotlin.math.abs(normalizeAngleDelta(currentHeadingDegrees - lastHeading))
    return headingDelta >= minHeadingDeltaDegrees
  }

  private fun normalizeAngleDelta(deltaDegrees: Double): Double {
    var delta = deltaDegrees % 360.0
    if (delta > 180.0) delta -= 360.0
    if (delta < -180.0) delta += 360.0
    return delta
  }
}
