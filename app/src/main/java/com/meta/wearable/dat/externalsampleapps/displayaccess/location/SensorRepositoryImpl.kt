package com.meta.wearable.dat.externalsampleapps.displayaccess.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Prefers [Sensor.TYPE_ROTATION_VECTOR] for heading; falls back to combining the accelerometer
 * and magnetic field sensors on devices without a rotation vector sensor.
 */
class SensorRepositoryImpl(context: Context) : SensorRepository {

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
  private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  private val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

  private val _headingDegrees = MutableStateFlow<Double?>(null)
  override val headingDegrees: StateFlow<Double?> = _headingDegrees.asStateFlow()

  private var isStarted = false
  private var hasAccelerometer = false
  private var hasMagnetometer = false
  private val lastAccelerometer = FloatArray(3)
  private val lastMagnetometer = FloatArray(3)
  private val rotationMatrix = FloatArray(9)
  private val orientationValues = FloatArray(3)

  private val listener =
      object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
          when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
              SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
              SensorManager.getOrientation(rotationMatrix, orientationValues)
              publishHeading(orientationValues[0])
            }
            Sensor.TYPE_ACCELEROMETER -> {
              System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
              hasAccelerometer = true
              updateHeadingFromAccelerometerAndMagnetometer()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
              System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
              hasMagnetometer = true
              updateHeadingFromAccelerometerAndMagnetometer()
            }
          }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
      }

  private fun updateHeadingFromAccelerometerAndMagnetometer() {
    if (!hasAccelerometer || !hasMagnetometer) return
    if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
      SensorManager.getOrientation(rotationMatrix, orientationValues)
      publishHeading(orientationValues[0])
    }
  }

  private fun publishHeading(azimuthRadians: Float) {
    val degrees = Math.toDegrees(azimuthRadians.toDouble())
    _headingDegrees.value = (degrees + 360.0) % 360.0
  }

  override fun start() {
    if (isStarted) return
    isStarted = true
    val rotationVector = rotationVectorSensor
    if (rotationVector != null) {
      sensorManager.registerListener(listener, rotationVector, SensorManager.SENSOR_DELAY_UI)
    } else {
      accelerometerSensor?.let {
        sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
      }
      magneticFieldSensor?.let {
        sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
      }
    }
  }

  override fun stop() {
    if (!isStarted) return
    isStarted = false
    sensorManager.unregisterListener(listener)
  }
}
