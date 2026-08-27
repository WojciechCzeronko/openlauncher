package com.openlauncher.app.ui.map

import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView
import com.openlauncher.app.R
import com.openlauncher.app.ui.map.components.Aw11RecenterButton
import com.openlauncher.app.ui.map.components.Aw11RouteInfo
import com.openlauncher.app.ui.map.components.Aw11SearchPanel
import com.openlauncher.app.ui.map.here.HereCameraController
import com.openlauncher.app.ui.map.here.HereRouteRenderer
import com.openlauncher.app.ui.map.navigation.RouteProgressTracker
import com.openlauncher.app.util.LocationData

private const val TAG = "Aw11HereMap"

private const val DEFAULT_LATITUDE = 53.1381
private const val DEFAULT_LONGITUDE = 18.0220
private const val ROUTE_REFRESH_INTERVAL_MS = 180_000L
private const val ROUTE_SNAP_ENTER_METERS = 15.0
private const val ROUTE_SNAP_EXIT_METERS = 20.0

private class MapAnimationState {
    var latitude = DEFAULT_LATITUDE
    var longitude = DEFAULT_LONGITUDE
    var bearing = 0f
    var lastLocationUpdateMs = 0L
    var isSnappedToRoute = false
}

@Composable
fun Aw11HereMap(
    location: LocationData?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = rememberAw11HereMapState()
    val mapView = remember(context) {
        MapView(context)
    }

    val routeRenderer = remember(mapView) {
        HereRouteRenderer(mapView)
    }

    val cameraController = remember(mapView) {
        HereCameraController(mapView)
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

    val routeProgressTracker = remember {
        RouteProgressTracker()
    }

    val carMarker = remember {
        mutableStateOf<MapMarker?>(null)
    }

    val lastRouteRefreshMs = remember {
        mutableLongStateOf(0L)
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

                cameraController.showInitialPosition(
                    initialCoordinates
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

            routeRenderer.clearRoute()

            mapView.onPause()
            routingController.dispose()
            mapView.onDestroy()
        }
    }

    // Car movement
    LaunchedEffect(
        location?.latitude,
        location?.longitude,
        location?.bearingDegrees,
        carMarker.value,
        state.activeRoute
    ) {
        val currentLocation =
            location ?: return@LaunchedEffect

        val marker =
            carMarker.value ?: return@LaunchedEffect

        val rawCoordinates = GeoCoordinates(
            currentLocation.latitude,
            currentLocation.longitude
        )

        val activeRoute =
            state.activeRoute

        val routeProgress =
            if (activeRoute != null) {
                routeProgressTracker.update(
                    rawCoordinates
                )
            } else {
                null
            }

        if (
            routeProgress != null &&
            activeRoute != null
        ) {
            state.routeProgress =
                routeProgress

            routeRenderer.updateRouteProgress(
                route = activeRoute,
                matchedSegmentIndex =
                    routeProgress.matchedSegmentIndex,
                matchedCoordinates =
                    routeProgress.matchedCoordinates
            )
        }
        val shouldSnapToRoute =
            routeProgress?.let { progress ->

                if (animationState.isSnappedToRoute) {
                    progress.distanceFromRouteMeters <=
                            ROUTE_SNAP_EXIT_METERS
                } else {
                    progress.distanceFromRouteMeters <=
                            ROUTE_SNAP_ENTER_METERS
                }

            } ?: false

        animationState.isSnappedToRoute =
            shouldSnapToRoute

        val displayCoordinates =
            if (
                shouldSnapToRoute &&
                routeProgress != null
            ) {
                routeProgress.matchedCoordinates
            } else {
                rawCoordinates
            }

        val targetLatitude =
            displayCoordinates.latitude

        val targetLongitude =
            displayCoordinates.longitude

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

            if (state.isFollowing && !state.isRecentering) {
                cameraController.follow(
                    coordinates = coordinates,
                    bearingDegrees = bearing
                )
            }

            animationState.latitude = latitude
            animationState.longitude = longitude
            animationState.bearing = bearing
        }
    }



    // ETA update
    LaunchedEffect(
        location?.latitude,
        location?.longitude,
        state.destination
    ) {
        val currentLocation =
            location ?: return@LaunchedEffect

        val destination =
            state.destination ?: return@LaunchedEffect

        if (state.activeRoute == null) {
            return@LaunchedEffect
        }

        val now =
            SystemClock.elapsedRealtime()

        if (
            now - lastRouteRefreshMs.longValue <
            ROUTE_REFRESH_INTERVAL_MS
        ) {
            return@LaunchedEffect
        }

        lastRouteRefreshMs.longValue = now

        val start = GeoCoordinates(
            currentLocation.latitude,
            currentLocation.longitude
        )

        routingController.calculateRoute(
            start = start,
            destination = destination,
            onSuccess = { route ->
                state.activeRoute = route

                routeProgressTracker.setRoute(
                    route
                )

                state.routeProgress =
                    routeProgressTracker.update(
                        start
                    )

                routeRenderer.showRoute(
                    route = route,
                    destination = destination
                )

                Log.d(
                    TAG,
                    "Route refreshed: " +
                            "${route.lengthInMeters} m, " +
                            "${route.duration.seconds} s"
                )
            },
            onError = { error ->
                Log.e(
                    TAG,
                    "Route refresh failed: ${error.name}"
                )
            }
        )
    }

    // Set principal point
    LaunchedEffect(
        state.mapSize.width,
        state.mapSize.height
    ) {
        cameraController.setNavigationPrincipalPoint(
            width = state.mapSize.width,
            height = state.mapSize.height
        )
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
                            state.isFollowing
                        ) {
                            state.isFollowing = false
                        }
                        false
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    state.mapSize = size
                }
        )

        Aw11SearchPanel(
            isOpen = state.isSearchOpen,
            query = state.searchQuery,
            results = state.searchResults,
            isSearching = state.isSearching,
            error = state.searchError,
            onOpen = {
                state.openSearch()
            },
            onQueryChange = { query ->
                state.searchQuery = query
            },
            onSearch = {
                state.startSearch()

                val center = GeoCoordinates(
                    animationState.latitude,
                    animationState.longitude
                )

                searchController.search(
                    queryText = state.searchQuery,
                    center = center,
                    onSuccess = { results ->
                        state.completeSearch(
                            results.take(5)
                        )
                    },
                    onError = { error ->
                        state.failSearch(
                            error.name
                        )
                    }
                )
            },
            onClear = {
                state.clearSearch()
            },
            onClose = {
                state.closeSearch()
            },
            onResultSelected = { result ->
                val start = GeoCoordinates(
                    animationState.latitude,
                    animationState.longitude
                )

                val destination =
                    result.coordinates
                state.destination = destination

                lastRouteRefreshMs.longValue =
                    SystemClock.elapsedRealtime()

                Log.d(
                    TAG,
                    "Calculating route to: " +
                            "${result.title}, " +
                            "${destination.latitude}, " +
                            "${destination.longitude}"
                )

                state.closeSearch()

                routingController.calculateRoute(
                    start = start,
                    destination = destination,
                    startHeadingDegrees =
                        location?.bearingDegrees,
                    onSuccess = { route ->
                        state.activeRoute = route

                        routeProgressTracker.setRoute(
                            route
                        )

                        state.routeProgress =
                            routeProgressTracker.update(
                                start
                            )

                        routeRenderer.showRoute(
                            route = route,
                            destination = destination
                        )

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
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        )

        //Eta widget
        state.activeRoute?.let { route ->
            val progress =
                state.routeProgress

            Aw11RouteInfo(
                distanceMeters =
                    progress?.remainingDistanceMeters
                        ?: route.lengthInMeters,
                durationSeconds =
                    progress?.remainingDurationSeconds
                        ?: route.duration.seconds,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 10.dp,
                        bottom = 10.dp
                    )
            )
        }

        Aw11RecenterButton(
            visible = !state.isFollowing,
            onClick = {
                if (state.isRecentering) {
                    return@Aw11RecenterButton
                }

                state.isRecentering = true

                val targetCoordinates = GeoCoordinates(
                    animationState.latitude,
                    animationState.longitude
                )

                cameraController.recenter(
                    coordinates = targetCoordinates,
                    bearingDegrees = animationState.bearing,
                    onFinished = {
                        state.isRecentering = false
                        state.isFollowing = true
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
        )
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
