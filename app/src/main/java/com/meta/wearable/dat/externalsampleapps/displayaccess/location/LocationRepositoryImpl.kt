package com.meta.wearable.dat.externalsampleapps.displayaccess.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.meta.wearable.dat.externalsampleapps.displayaccess.location.model.LocationSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val UPDATE_INTERVAL_MILLIS = 3_000L

class LocationRepositoryImpl(context: Context) : LocationRepository {

  private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

  private val _location = MutableStateFlow<LocationSample?>(null)
  override val location: StateFlow<LocationSample?> = _location.asStateFlow()

  private var isStarted = false

  private val callback =
      object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
          val lastLocation = result.lastLocation ?: return
          _location.value =
              LocationSample(
                  latitude = lastLocation.latitude,
                  longitude = lastLocation.longitude,
                  accuracyMeters = lastLocation.accuracy,
                  timestampMillis = lastLocation.time,
              )
        }
      }

  @SuppressLint("MissingPermission")
  override fun startUpdates() {
    if (isStarted) return
    isStarted = true
    val request =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MILLIS).build()
    fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
  }

  override fun stopUpdates() {
    if (!isStarted) return
    isStarted = false
    fusedLocationClient.removeLocationUpdates(callback)
  }
}
