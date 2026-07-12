package com.meta.wearable.dat.externalsampleapps.displayaccess.location

import com.meta.wearable.dat.externalsampleapps.displayaccess.location.model.LocationSample
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
  val location: StateFlow<LocationSample?>

  fun startUpdates()

  fun stopUpdates()
}
