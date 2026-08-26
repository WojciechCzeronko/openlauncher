package com.openlauncher.app.ui.map

import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.here.sdk.animation.AnimationState
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoCoordinatesUpdate
import com.here.sdk.core.GeoOrientationUpdate
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapCameraAnimationFactory
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.RenderSize
import com.here.sdk.routing.Route
import com.here.time.Duration
import com.openlauncher.app.R
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.util.LocationData
import com.here.sdk.core.Color as HereColor
import com.here.sdk.core.GeoCircle
import com.here.sdk.core.GeoPolygon
import com.here.sdk.mapview.MapPolygon
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import com.openlauncher.app.ui.theme.Aw11Secondary
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

private const val TAG = "Aw11HereMap"

private const val DEFAULT_LATITUDE = 53.1381
private const val DEFAULT_LONGITUDE = 18.0220
private const val DEFAULT_ZOOM_DISTANCE_METERS = 500.0
private const val TEST_DESTINATION_LATITUDE = 53.1325
private const val TEST_DESTINATION_LONGITUDE = 18.0120

private class MapAnimationState {
    var latitude = DEFAULT_LATITUDE
    var longitude = DEFAULT_LONGITUDE
    var bearing = 0f
    var lastLocationUpdateMs = 0L
}

@Composable
fun Aw11HereMap(
    location: LocationData?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val mapView = remember(context) {
        MapView(context)
    }

    val animationState = remember {
        MapAnimationState()
    }

    val animationProgress = remember {
        Animatable(1f)
    }

    val routingController = remember {
        HereRoutingController()
    }

    val searchController = remember {
        HereSearchController()
    }

    var isSearchOpen by remember {
        mutableStateOf(false)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var searchResults by remember {
        mutableStateOf<List<HereSearchResult>>(emptyList())
    }

    var isSearching by remember {
        mutableStateOf(false)
    }

    var searchError by remember {
        mutableStateOf<String?>(null)
    }

    val routePolyline = remember {
        mutableStateOf<MapPolyline?>(null)
    }

    var activeRoute by remember {
        mutableStateOf<Route?>(null)
    }

    val destinationMarker = remember {
        mutableStateOf<MapPolygon?>(null)
    }

    var routeRequested by remember {
        mutableStateOf(false)
    }

    var isFollowing by remember {
        mutableStateOf(true)
    }

    var isRecentering by remember {
        mutableStateOf(false)
    }

    var mapSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    val carMarker = remember {
        mutableStateOf<MapMarker?>(null)
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

                animationState.latitude = initialCoordinates.latitude
                animationState.longitude = initialCoordinates.longitude

                location?.bearingDegrees?.let {
                    animationState.bearing = it
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
            routePolyline.value?.let { polyline ->
                mapView.mapScene.removeMapPolyline(polyline)
            }

            routePolyline.value = null

            destinationMarker.value?.let { marker ->
                mapView.mapScene.removeMapPolygon(marker)
            }

            destinationMarker.value = null
            routingController.dispose()
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

        val marker =
            carMarker.value ?: return@LaunchedEffect

        val targetLatitude = currentLocation.latitude
        val targetLongitude = currentLocation.longitude

        val startLatitude = animationState.latitude
        val startLongitude = animationState.longitude
        val startBearing = animationState.bearing

        val targetBearing =
            currentLocation.bearingDegrees
                ?: startBearing

        val bearingDelta =
            shortestBearingDelta(
                startBearing,
                targetBearing
            )

        val now = SystemClock.elapsedRealtime()

        val updateIntervalMs =
            if (animationState.lastLocationUpdateMs > 0L) {
                now - animationState.lastLocationUpdateMs
            } else {
                500L
            }

        animationState.lastLocationUpdateMs = now

        val animationDurationMs =
            (updateIntervalMs * 1.05)
                .toLong()
                .coerceIn(
                    250L,
                    1500L
                )

        animationProgress.snapTo(0f)

        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animationDurationMs.toInt(),
                easing = LinearEasing
            )
        ) {
            val progress = value

            val latitude =
                startLatitude +
                        (targetLatitude - startLatitude) * progress

            val longitude =
                startLongitude +
                        (targetLongitude - startLongitude) * progress

            val bearing =
                normalizeBearing(
                    startBearing +
                            bearingDelta * progress
                )

            val coordinates = GeoCoordinates(
                latitude,
                longitude
            )

            marker.setCoordinates(
                coordinates
            )

            val orientation = GeoOrientationUpdate(
                bearing.toDouble(),
                0.0
            )

            val zoom = MapMeasure(
                MapMeasure.Kind.DISTANCE_IN_METERS,
                DEFAULT_ZOOM_DISTANCE_METERS
            )

            if (isFollowing && !isRecentering) {
                mapView.camera.lookAt(
                    coordinates,
                    orientation,
                    zoom
                )
            }

            animationState.latitude = latitude
            animationState.longitude = longitude
            animationState.bearing = bearing
        }
    }

    LaunchedEffect(
        location?.latitude,
        location?.longitude,
        carMarker.value
    ) {
        val currentLocation =
            location ?: return@LaunchedEffect

        if (
            routeRequested ||
            carMarker.value == null
        ) {
            return@LaunchedEffect
        }

        routeRequested = true

        val start = GeoCoordinates(
            currentLocation.latitude,
            currentLocation.longitude
        )

        val destination = GeoCoordinates(
            TEST_DESTINATION_LATITUDE,
            TEST_DESTINATION_LONGITUDE
        )

        routingController.calculateRoute(
            start = start,
            destination = destination,
            onSuccess = { route ->
                activeRoute = route

                val polyline =
                    createRoutePolyline(route)

                if (polyline != null) {
                    routePolyline.value?.let { previous ->
                        mapView.mapScene.removeMapPolyline(previous)
                    }

                    mapView.mapScene.addMapPolyline(polyline)
                    routePolyline.value = polyline
                }

                destinationMarker.value?.let { previous ->
                    mapView.mapScene.removeMapPolygon(previous)
                }

                val marker =
                    createDestinationMarker(destination)

                mapView.mapScene.addMapPolygon(marker)
                destinationMarker.value = marker

                Log.d(
                    TAG,
                    "Route: ${route.lengthInMeters} m, " +
                            "${route.duration.seconds} s"
                )
            },
            onError = { error ->
                Log.e(
                    TAG,
                    "Route calculation failed: ${error.name}"
                )
            }
        )
    }

    LaunchedEffect(
        mapSize.width,
        mapSize.height
    ) {
        if (
            mapSize.width > 0 &&
            mapSize.height > 0
        ) {
            val principalPoint = Point2D(
                mapSize.width / 2.0,
                mapSize.height * 0.72
            )

            mapView.camera.setPrincipalPoint(
                principalPoint
            )
        }
    }

    Box(
        modifier = modifier
    ) {
        AndroidView(
            factory = {
                mapView.apply {
                    setOnTouchListener { _, event ->
                        if (
                            event.actionMasked == MotionEvent.ACTION_MOVE &&
                            isFollowing
                        ) {
                            isFollowing = false
                        }

                        false
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    mapSize = size
                }
        )

        // Search
        if (!isSearchOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .background(
                        Aw11Background.copy(alpha = 0.90f)
                    )
                    .border(
                        width = 1.dp,
                        color = Aw11Primary.copy(alpha = 0.85f)
                    )
                    .clickable {
                        isSearchOpen = true
                    }
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
            ) {
                Text(
                    text = "SEARCH",
                    color = Aw11Primary,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Search box
        if (isSearchOpen) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = 10.dp,
                        top = 10.dp
                    )
                    .width(420.dp)
                    .background(
                        Aw11Background.copy(alpha = 0.96f)
                    )
                    .border(
                        width = 1.dp,
                        color = Aw11Primary.copy(alpha = 0.85f)
                    )
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                        },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                Text(
                                    text = "CLR",
                                    color = Aw11Secondary,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.sp,
                                    modifier = Modifier
                                        .clickable {
                                            searchQuery = ""
                                            searchResults = emptyList()
                                            searchError = null
                                        }
                                        .padding(8.dp)
                                )
                            }
                        },
                        placeholder = {
                            Text(
                                text = "DESTINATION...",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text(
                        text = "SEARCH",
                        color = Aw11Primary,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .border(
                                1.dp,
                                Aw11Primary.copy(alpha = 0.85f)
                            )
                            .clickable {
                                if (
                                    searchQuery.isBlank() ||
                                    isSearching
                                ) {
                                    return@clickable
                                }

                                focusManager.clearFocus()
                                keyboardController?.hide()

                                isSearching = true
                                searchError = null
                                searchResults = emptyList()

                                val center = GeoCoordinates(
                                    animationState.latitude,
                                    animationState.longitude
                                )

                                searchController.search(
                                    queryText = searchQuery,
                                    center = center,
                                    onSuccess = { results ->
                                        searchResults =
                                            results.take(5)

                                        isSearching = false
                                    },
                                    onError = { error ->
                                        searchError = error.name
                                        isSearching = false
                                    }
                                )
                            }
                            .padding(
                                horizontal = 12.dp,
                                vertical = 10.dp
                            )
                    )

                    Spacer(
                        Modifier.width(6.dp)
                    )

                    Text(
                        text = "X",
                        color = Aw11Secondary,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable {
                                focusManager.clearFocus()
                                keyboardController?.hide()

                                isSearchOpen = false
                                searchResults = emptyList()
                                searchError = null
                            }
                            .padding(8.dp)
                    )
                }

                if (isSearching) {
                    Text(
                        text = "SEARCHING...",
                        color = Aw11Secondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                searchError?.let { error ->
                    Text(
                        text = "SEARCH ERROR: $error",
                        color = Aw11Primary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                searchResults.forEach { result ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .border(
                                1.dp,
                                Aw11Primary.copy(alpha = 0.35f)
                            )
                            .clickable {
                                Log.d(
                                    TAG,
                                    "Selected search result: " +
                                            "${result.title}, " +
                                            "${result.coordinates.latitude}, " +
                                            "${result.coordinates.longitude}"
                                )
                            }
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = result.title.uppercase(),
                                color = Aw11Primary,
                                fontSize = 12.sp,
                                letterSpacing = 0.25.sp
                            )

                            Text(
                                text = result.address.uppercase(),
                                color = Aw11Secondary,
                                fontSize = 10.sp,
                                letterSpacing = 0.sp
                            )
                        }
                    }
                }
            }
        }

        //Eta widget
        activeRoute?.let { route ->
            val distanceKm =
                route.lengthInMeters / 1000.0

            val durationSeconds =
                route.duration.seconds

            val durationMinutes =
                (durationSeconds + 59) / 60

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 10.dp,
                        bottom = 10.dp
                    )
                    .background(
                        Aw11Background.copy(alpha = 0.90f)
                    )
                    .border(
                        width = 1.dp,
                        color = Aw11Primary.copy(alpha = 0.85f)
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    )
            ) {
                Text(
                    text = String.format(
                        "%.1f KM  •  %d MIN",
                        distanceKm,
                        durationMinutes
                    ),
                    color = Aw11Primary,
                    fontSize = 14.sp
                )
            }
        }

        if (!isFollowing) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                    .size(36.dp)
                    .background(
                        Aw11Background.copy(alpha = 0.90f)
                    )
                    .border(
                        width = 1.dp,
                        color = Aw11Primary.copy(alpha = 0.85f)
                    )
                    .clickable {
                        if (isRecentering) {
                            return@clickable
                        }

                        isRecentering = true

                        val targetCoordinates = GeoCoordinates(
                            animationState.latitude,
                            animationState.longitude
                        )

                        val targetOrientation = GeoOrientationUpdate(
                            animationState.bearing.toDouble(),
                            0.0
                        )

                        val targetZoom = MapMeasure(
                            MapMeasure.Kind.DISTANCE_IN_METERS,
                            DEFAULT_ZOOM_DISTANCE_METERS
                        )

                        val cameraAnimation =
                            MapCameraAnimationFactory.flyTo(
                                GeoCoordinatesUpdate(targetCoordinates),
                                targetOrientation,
                                targetZoom,
                                0.0,
                                Duration.ofMillis(1000)
                            )

                        mapView.camera.startAnimation(
                            cameraAnimation
                        ) { animationStateResult ->

                            if (
                                animationStateResult == AnimationState.COMPLETED ||
                                animationStateResult == AnimationState.CANCELLED
                            ) {
                                isRecentering = false
                                isFollowing = true
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⌖",
                    color = Aw11Primary,
                    fontSize = 20.sp
                )
            }
        }
    }
}

private fun shortestBearingDelta(
    from: Float,
    to: Float
): Float {
    return ((to - from + 540f) % 360f) - 180f
}

private fun normalizeBearing(
    bearing: Float
): Float {
    return ((bearing % 360f) + 360f) % 360f
}

private fun createRoutePolyline(
    route: Route
): MapPolyline? {
    return try {
        val lineWidth =
            MapMeasureDependentRenderSize(
                RenderSize.Unit.PIXELS,
                14.0
            )

        val lineColor = HereColor(
            0.84f,
            0.91f,
            0.0f,
            0.95f
        )

        val representation =
            MapPolyline.SolidRepresentation(
                lineWidth,
                lineColor,
                LineCap.ROUND
            )

        MapPolyline(
            route.geometry,
            representation
        )
    } catch (e: Exception) {
        Log.e(
            TAG,
            "Failed to create route polyline.",
            e
        )

        null
    }
}

private fun createDestinationMarker(
    coordinates: GeoCoordinates
): MapPolygon {
    val circle = GeoCircle(
        coordinates,
        10.0
    )

    val polygon = GeoPolygon(
        circle
    )

    val color = HereColor(
        0.84f,
        0.91f,
        0.0f,
        1.0f
    )

    return MapPolygon(
        polygon,
        color
    )
}