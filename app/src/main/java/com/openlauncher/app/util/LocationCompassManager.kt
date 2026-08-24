package com.openlauncher.app.util

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val speedMps: Float = 0f,
    val bearingDegrees: Float? = null
)

class LocationCompassManager(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val sensorManager   = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _location  = MutableStateFlow<LocationData?>(null)
    private val _bearing   = MutableStateFlow(0f)
    val location: StateFlow<LocationData?> = _location
    val bearing: StateFlow<Float> = _bearing

    private val gravity      = FloatArray(3)
    private val geomagnetic  = FloatArray(3)
    // Circular low-pass filter for smooth bearing (avoids 0°/360° wrap artifacts)
    private var bearingSin   = 0f
    private var bearingCos   = 1f   // initial: pointing north
    private var lastLocationForBearing: Location? = null
    private var lastGpsFixMs = 0L
    private var filteredSpeedMps = 0f
    private var lowSpeedSinceMs: Long? = null
    private var lastSpeedUpdateMs = 0L
    private var lastRawSpeedMps = 0f
    private val handler = Handler(Looper.getMainLooper())

    private val speedTimeoutRunnable = object : Runnable {
        override fun run() {
            val now = SystemClock.elapsedRealtime()

            if (
                lastSpeedUpdateMs > 0L &&
                now - lastSpeedUpdateMs > 2500L &&
                filteredSpeedMps > 0f
            ) {
                filteredSpeedMps = 0f
                lastRawSpeedMps = 0f
                lowSpeedSinceMs = null

                _location.value?.let { current ->
                    _location.value = current.copy(
                        speedMps = 0f
                    )
                }
            }

            handler.postDelayed(this, 500L)
        }
    }
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravity, 0, 3)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                }
            }
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                val azimuthRad = orientation[0].toDouble()
                // Circular low-pass filter — correctly handles 0°/360° wrap-around
                val alpha = 0.10f
                bearingSin = alpha * sin(azimuthRad).toFloat() + (1f - alpha) * bearingSin
                bearingCos = alpha * cos(azimuthRad).toFloat() + (1f - alpha) * bearingCos
                _bearing.value = ((Math.toDegrees(atan2(bearingSin.toDouble(), bearingCos.toDouble())) + 360) % 360).toFloat()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            val now = SystemClock.elapsedRealtime()

            val isGps = loc.provider == LocationManager.GPS_PROVIDER
            val isNetwork = loc.provider == LocationManager.NETWORK_PROVIDER

            if (isGps) {
                lastGpsFixMs = now
            }

            // Do not let a network fix overwrite a recent GPS fix.
            if (isNetwork && now - lastGpsFixMs < 10_000L) {
                return
            }

            if (loc.hasSpeed()) {
                lastSpeedUpdateMs = now
            }

            val rawSpeed =
                if (loc.hasSpeed()) {
                    loc.speed
                } else {
                    0f
                }

            val speed = filterSpeed(
                rawSpeedMps = rawSpeed,
                nowMs = now
            )

            _location.value = LocationData(
                latitude = loc.latitude,
                longitude = loc.longitude,
                altitude = loc.altitude,
                accuracy = loc.accuracy,
                speedMps = speed,
                bearingDegrees =
                    if (loc.hasBearing()) {
                        (loc.bearing + 360f) % 360f
                    } else {
                        null
                    }
            )

            // 1. If GPS has a hardware-computed bearing, use it (works offline)
            if (loc.hasBearing()) {
                _bearing.value = loc.bearing
            } else {
                // 2. Math fallback: Calculate bearing between consecutive location points (works offline & sensor-less!)
                val lastLoc = lastLocationForBearing
                if (lastLoc != null) {
                    val distance = lastLoc.distanceTo(loc)
                    // Ensure the distance is enough to overcome GPS jitter (e.g. 3 meters)
                    if (distance > 3f) {
                        val computedBearing = lastLoc.bearingTo(loc)
                        // Normalize bearing to 0-360
                        _bearing.value = (computedBearing + 360f) % 360f
                        lastLocationForBearing = loc
                    }
                } else {
                    lastLocationForBearing = loc
                }
            }
        }
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        // Must be overridden explicitly: the interface only gained default
        // implementations in API 30, so omitting them throws AbstractMethodError
        // on older head units when the GPS provider is toggled.
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun start() {
        handler.removeCallbacks(speedTimeoutRunnable)
        handler.post(speedTimeoutRunnable)

        // Sensors
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }

        val hasFine   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED

        // Location — Robust offline-first registration
        // GPS Provider (Works 100% offline, sat-based) — Requires FINE
        if (hasFine) {
            try {
                if (locationManager.allProviders.contains(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        500L,
                        0f,
                        locationListener
                    )
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
                        locationListener.onLocationChanged(it)
                    }
                }
            } catch (_: Exception) {}
        }

        // Network Provider (Works online, cell/wifi-based) — Requires COARSE (or FINE)
        if (hasCoarse || hasFine) {
            try {
                if (locationManager.allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 1000L, 10f, locationListener
                    )
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
                        locationListener.onLocationChanged(it)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        handler.removeCallbacks(speedTimeoutRunnable)
        sensorManager.unregisterListener(sensorListener)
        locationManager.removeUpdates(locationListener)
        lastLocationForBearing = null
    }

    private fun filterSpeed(
        rawSpeedMps: Float,
        nowMs: Long
    ): Float {
        val raw = rawSpeedMps.coerceAtLeast(0f)

        val rawSpeedDelta = kotlin.math.abs(
            raw - lastRawSpeedMps
        )

        // Reject implausible single-sample GPS speed spikes.
        if (
            lastRawSpeedMps > 2.8f &&
            raw > lastRawSpeedMps &&
            rawSpeedDelta > 8.3f
        ) {
            return filteredSpeedMps
        }
        // Treat very small GPS speeds as stationary GPS drift.
        if (raw < 0.7f) {
            if (filteredSpeedMps > 2.8f) {
                val lowSpeedStart = lowSpeedSinceMs

                if (lowSpeedStart == null) {
                    lowSpeedSinceMs = nowMs
                    return filteredSpeedMps
                }

                // Ignore a short single-frame drop while the vehicle was moving.
                if (nowMs - lowSpeedStart < 1500L) {
                    return filteredSpeedMps
                }
            }

            filteredSpeedMps = 0f
            lowSpeedSinceMs = null
            lastRawSpeedMps = 0f
            return 0f
        }

        lowSpeedSinceMs = null

        val alpha =
            if (raw > filteredSpeedMps) {
                0.85f
            } else {
                0.75f
            }

        filteredSpeedMps =
            filteredSpeedMps * (1f - alpha) +
                    raw * alpha

        lastRawSpeedMps = raw
        return filteredSpeedMps
    }
}
