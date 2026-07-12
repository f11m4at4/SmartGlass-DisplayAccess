package com.meta.wearable.dat.externalsampleapps.displayaccess.location

import kotlinx.coroutines.flow.StateFlow

interface SensorRepository {
  val headingDegrees: StateFlow<Double?>

  fun start()

  fun stop()
}
