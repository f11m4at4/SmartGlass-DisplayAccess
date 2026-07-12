package com.meta.wearable.dat.externalsampleapps.displayaccess.maps

import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.MiniMapState
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.NavigationTarget

interface StaticMapRepository {
  suspend fun createMap(
      currentLatitude: Double,
      currentLongitude: Double,
      currentHeadingDegrees: Double,
      target: NavigationTarget,
  ): Result<MiniMapState>
}
