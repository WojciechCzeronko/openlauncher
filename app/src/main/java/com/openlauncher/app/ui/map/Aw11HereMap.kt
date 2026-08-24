package com.openlauncher.app.ui.map

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoOrientationUpdate
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import com.openlauncher.app.R
import com.openlauncher.app.util.LocationData

private const val TAG = "Aw11HereMap"

private const val DEFAULT_LATITUDE = 53.1381
private const val DEFAULT_LONGITUDE = 18.0220
private const val DEFAULT_ZOOM_DISTANCE_METERS = 500.0

@Composable
fun Aw11HereMap(
    location: LocationData?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember(context) {
        MapView(context)
    }

    val carMarker = remember {
        mutableStateOf<MapMarker?>(null)
    }
    var lastMapBearing by remember {
        mutableFloatStateOf(0f)
    }

    DisposableEffect(mapView, lifecycleOwner) {
        mapView.onCreate(null)

        mapView.mapScene.loadScene(
            MapScheme.LITE_NIGHT
        ) { mapError ->
            if (mapError == null) {
                Log.d(TAG, "HERE map scene loaded.")

                val initialCoordinates =
                    if (location != null) {
                        GeoCoordinates(
                            location.latitude,
                            location.longitude
                        )
                    } else {
                        GeoCoordinates(
                            DEFAULT_LATITUDE,
                            DEFAULT_LONGITUDE
                        )
                    }

                val zoom = MapMeasure(
                    MapMeasure.Kind.DISTANCE_IN_METERS,
                    DEFAULT_ZOOM_DISTANCE_METERS
                )

                mapView.camera.lookAt(
                    initialCoordinates,
                    zoom
                )
                val markerImage =
                    MapImageFactory.fromResource(
                        context.resources,
                        R.drawable.small
                    )

                val marker = MapMarker(
                    initialCoordinates,
                    markerImage
                )

                mapView.mapScene.addMapMarker(marker)

                carMarker.value = marker
            } else {
                Log.e(
                    TAG,
                    "HERE map scene failed: ${mapError.name}"
                )
            }
        }

        val lifecycleObserver =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        mapView.onResume()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        mapView.onPause()
                    }

                    else -> Unit
                }
            }

        lifecycleOwner.lifecycle.addObserver(
            lifecycleObserver
        )

        // The composable can enter composition while the Activity is already resumed.
        if (
            lifecycleOwner.lifecycle.currentState
                .isAtLeast(Lifecycle.State.RESUMED)
        ) {
            mapView.onResume()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(
                lifecycleObserver
            )

            carMarker.value?.let { marker ->
                mapView.mapScene.removeMapMarker(marker)
            }

            carMarker.value = null

            mapView.onPause()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(
        location?.latitude,
        location?.longitude,
        location?.bearingDegrees,
        carMarker.value
    ) {
        val currentLocation =
            location ?: return@LaunchedEffect

        val coordinates = GeoCoordinates(
            currentLocation.latitude,
            currentLocation.longitude
        )

        carMarker.value?.setCoordinates(
            coordinates
        )

        currentLocation.bearingDegrees?.let { newBearing ->
            lastMapBearing = newBearing
        }

        val zoom = MapMeasure(
            MapMeasure.Kind.DISTANCE_IN_METERS,
            DEFAULT_ZOOM_DISTANCE_METERS
        )

        val cameraBearing = lastMapBearing

        val orientation = GeoOrientationUpdate(
            cameraBearing.toDouble(),
            0.0
        )

        mapView.camera.lookAt(
            coordinates,
            orientation,
            zoom
        )
    }
    AndroidView(
        factory = {
            mapView
        },
        modifier = modifier
    )
}