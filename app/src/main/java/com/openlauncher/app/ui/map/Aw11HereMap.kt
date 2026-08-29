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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.openlauncher.app.data.AppSettings
import com.openlauncher.app.ui.map.components.Aw11DemoControls
import com.openlauncher.app.ui.map.components.Aw11RecenterButton
import com.openlauncher.app.ui.map.components.Aw11RouteInfo
import com.openlauncher.app.ui.map.components.Aw11SearchPanel
import com.openlauncher.app.ui.map.here.HereCameraController
import com.openlauncher.app.ui.map.here.HereRouteRenderer
import com.openlauncher.app.ui.map.navigation.RouteProgressTracker
import com.openlauncher.app.util.LocationData
import com.openlauncher.app.ui.map.navigation.DemoNavigationController
import com.openlauncher.app.ui.map.navigation.DemoTripData
import com.openlauncher.app.ui.map.navigation.ManeuverGuidance
import com.openlauncher.app.ui.map.navigation.ManeuverProgressTracker
import kotlinx.coroutines.delay

private const val TAG = "Aw11HereMap"

private const val DEFAULT_LATITUDE = 53.1381
private const val DEFAULT_LONGITUDE = 18.0220
private const val MAX_REROUTE_GPS_ACCURACY_METERS = 50f
private const val ROUTE_SNAP_ENTER_METERS = 15.0
private const val ROUTE_SNAP_EXIT_METERS = 20.0
private const val AUTO_REROUTE_COOLDOWN_MS = 30_000L
private const val MAX_AUTO_REROUTES_PER_GUIDANCE = 5
private const val LOOK_AHEAD_SECONDS = 1.5
private const val LOOK_AHEAD_MIN_METERS = 10.0
private const val LOOK_AHEAD_MAX_METERS = 30.0
private const val LOOK_AHEAD_MIN_SPEED_MPS = 1.0f

private const val DEFAULT_CAMERA_DISTANCE_METERS = 500.0
private const val ZOOM_RESPONSE_FACTOR = 0.25
private const val DEMO_UPDATE_INTERVAL_MS = 250L
private const val GUIDANCE_RESERVED_LEFT_DP = 216

private class MapAnimationState {
    var latitude = DEFAULT_LATITUDE
    var longitude = DEFAULT_LONGITUDE
    var cameraLatitude = DEFAULT_LATITUDE
    var cameraLongitude = DEFAULT_LONGITUDE
    var cameraDistanceMeters =
        DEFAULT_CAMERA_DISTANCE_METERS
    var bearing = 0f
    var lastLocationUpdateMs = 0L
    var isSnappedToRoute = false
}

@Composable
fun Aw11HereMap(
    location: LocationData?,
    settings: AppSettings,
    onDemoDataChanged: (
        LocationData?,
        DemoTripData?
    ) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
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

    val maneuverProgressTracker = remember {
        ManeuverProgressTracker()
    }

    val maneuverGuidance = remember {
        mutableStateOf<ManeuverGuidance?>(null)
    }
    val demoNavigationController = remember {
        DemoNavigationController()
    }

    val demoLocation = remember {
        mutableStateOf<LocationData?>(null)
    }

    val isDemoMode = remember {
        mutableStateOf(false)
    }

    val isDemoRunning = remember {
        mutableStateOf(false)
    }
    val isDemoPaused = remember {
        mutableStateOf(false)
    }
    val demoSpeedMultiplier = remember {
        mutableStateOf(1.0)
    }

    val navigationLocation =
        if (isDemoMode.value) {
            demoLocation.value
        } else {
            location
        }

    val carMarker = remember {
        mutableStateOf<MapMarker?>(null)
    }

    val lastRouteRefreshMs = remember {
        mutableLongStateOf(0L)
    }

    val offRouteSinceMs = remember {
        mutableLongStateOf(0L)
    }

    val lastAutoRerouteRequestMs = remember {
        mutableLongStateOf(0L)
    }

    val autoRerouteCount = remember {
        mutableIntStateOf(0)
    }

    val isRouteRequestInProgress = remember {
        mutableStateOf(false)
    }

    val routeRefreshIntervalMs =
        settings.routeRefreshIntervalSeconds * 1000L

    LaunchedEffect(
        isDemoRunning.value,
        state.activeRoute,
        demoSpeedMultiplier.value
    ) {
        if (!isDemoRunning.value) {
            return@LaunchedEffect
        }

        val route =
            state.activeRoute ?: run {
                isDemoRunning.value = false
                return@LaunchedEffect
            }

        if (!demoNavigationController.isRunning) {
            demoLocation.value =
                demoNavigationController.start(
                    route = route,
                    speedMultiplier =
                        demoSpeedMultiplier.value
                )
        } else {
            demoNavigationController.setSpeedMultiplier(
                demoSpeedMultiplier.value
            )

            demoLocation.value =
                demoNavigationController.currentLocation
        }

        isDemoMode.value = true

        var lastUpdateMs =
            SystemClock.elapsedRealtime()

        while (
            isDemoRunning.value &&
            demoNavigationController.isRunning
        ) {
            delay(
                DEMO_UPDATE_INTERVAL_MS
            )

            val now =
                SystemClock.elapsedRealtime()

            val deltaSeconds =
                (
                        now - lastUpdateMs
                        ) / 1000.0

            lastUpdateMs = now

            val updatedLocation =
                demoNavigationController.update(
                    deltaSeconds
                )

            demoLocation.value =
                updatedLocation

            onDemoDataChanged(
                updatedLocation,
                demoNavigationController.tripData
            )
        }

        if (
            isDemoRunning.value &&
            !demoNavigationController.isRunning
        ) {
            // Keep the final demo position at the destination.
            demoLocation.value =
                demoNavigationController.currentLocation

            isDemoRunning.value = false
        }
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
                animationState.cameraLatitude = initialCoordinates.latitude
                animationState.cameraLongitude = initialCoordinates.longitude

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
        navigationLocation?.latitude,
        navigationLocation?.longitude,
        navigationLocation?.bearingDegrees,
        navigationLocation?.speedMps,
        carMarker.value,
        state.activeRoute,
        state.destination,
        settings.autoReroute,
        settings.offRouteThresholdMeters,
        settings.rerouteDelaySeconds
    ) {
        val currentLocation =
            navigationLocation ?: return@LaunchedEffect

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

            maneuverGuidance.value =
                maneuverProgressTracker.update(
                    routeProgress
                )

            maneuverGuidance.value?.let { guidance ->
                Log.d(
                    TAG,
                    "Next maneuver: " +
                            "${guidance.actionName}, " +
                            "${guidance.distanceMeters} m, " +
                            guidance.instruction
                )
            }

            routeRenderer.updateRouteProgress(
                route = activeRoute,
                matchedSegmentIndex =
                    routeProgress.matchedSegmentIndex,
                matchedCoordinates =
                    routeProgress.matchedCoordinates
            )
        }
        routeProgress?.let { progress ->
            Log.d(
                TAG,
                "Route progress: " +
                        "distance=${progress.remainingDistanceMeters} m, " +
                        "eta=${progress.remainingDurationSeconds} s, " +
                        "offRoute=${progress.distanceFromRouteMeters.toInt()} m, " +
                        "progress=${"%.3f".format(progress.progressFraction)}"
            )
            if (
                !isDemoMode.value &&
                settings.autoReroute &&
                autoRerouteCount.intValue <
                MAX_AUTO_REROUTES_PER_GUIDANCE &&
                routeProgress != null &&
                state.destination != null
            ) {
                val gpsAccuracyIsGood =
                    currentLocation.accuracy <=
                            MAX_REROUTE_GPS_ACCURACY_METERS

                val isOffRoute =
                    routeProgress.distanceFromRouteMeters >
                            settings.offRouteThresholdMeters

                val now =
                    SystemClock.elapsedRealtime()

                if (
                    !gpsAccuracyIsGood ||
                    !isOffRoute
                ) {
                    offRouteSinceMs.longValue = 0L
                } else if (
                    offRouteSinceMs.longValue == 0L
                ) {
                    offRouteSinceMs.longValue = now

                    Log.d(
                        TAG,
                        "Off route detected: " +
                                "${routeProgress.distanceFromRouteMeters.toInt()} m"
                    )
                } else {
                    val offRouteDurationMs =
                        now - offRouteSinceMs.longValue

                    val rerouteDelayMs =
                        settings.rerouteDelaySeconds * 1000L

                    val cooldownFinished =
                        lastAutoRerouteRequestMs.longValue == 0L ||
                                now - lastAutoRerouteRequestMs.longValue >=
                                AUTO_REROUTE_COOLDOWN_MS

                    val rerouteLimitNotReached =
                        autoRerouteCount.intValue <
                                MAX_AUTO_REROUTES_PER_GUIDANCE

                    if (
                        offRouteDurationMs >= rerouteDelayMs &&
                        cooldownFinished &&
                        rerouteLimitNotReached &&
                        !isRouteRequestInProgress.value
                    ) {
                        val destination =
                            state.destination

                        if (destination != null) {
                            isRouteRequestInProgress.value = true

                            // Reset now so a failed request does not immediately fire again.
                            offRouteSinceMs.longValue = 0L

                            lastAutoRerouteRequestMs.longValue =
                                SystemClock.elapsedRealtime()

                            Log.d(
                                TAG,
                                "Auto reroute started: " +
                                        "offRoute=${routeProgress.distanceFromRouteMeters.toInt()} m"
                            )

                            routingController.calculateRoute(
                                start = rawCoordinates,
                                destination = destination,
                                startHeadingDegrees =
                                    currentLocation.bearingDegrees,
                                onSuccess = { newRoute ->
                                    routeProgressTracker.setRoute(
                                        newRoute
                                    )
                                    maneuverProgressTracker.setRoute(
                                        newRoute
                                    )
                                    val newProgress =
                                        routeProgressTracker.update(
                                            rawCoordinates
                                        )

                                    state.activeRoute =
                                        newRoute

                                    state.routeProgress =
                                        newProgress

                                    maneuverGuidance.value =
                                        maneuverProgressTracker.update(
                                            state.routeProgress
                                        )

                                    routeRenderer.showRoute(
                                        route = newRoute,
                                        destination = destination
                                    )

                                    lastRouteRefreshMs.longValue =
                                        SystemClock.elapsedRealtime()

                                    isRouteRequestInProgress.value =
                                        false

                                    autoRerouteCount.intValue++
                                    Log.d(
                                        TAG,
                                        "Auto reroute completed: " +
                                                "${newRoute.lengthInMeters} m, " +
                                                "${newRoute.duration.seconds} s, " +
                                                "count=${autoRerouteCount.intValue}/$MAX_AUTO_REROUTES_PER_GUIDANCE"
                                    )
                                    if (
                                        autoRerouteCount.intValue >=
                                        MAX_AUTO_REROUTES_PER_GUIDANCE
                                    ) {
                                        Log.d(
                                            TAG,
                                            "Auto reroute limit reached. " +
                                                    "Further reroutes disabled for this guidance session."
                                        )
                                    }
                                },
                                onError = { error ->
                                    isRouteRequestInProgress.value =
                                        false

                                    Log.e(
                                        TAG,
                                        "Auto reroute failed: ${error.name}"
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                offRouteSinceMs.longValue = 0L
            }
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
        val lookAheadDistanceMeters =
            calculateLookAheadDistanceMeters(
                currentLocation.speedMps
            )

        val cameraTargetCoordinates =
            if (
                shouldSnapToRoute &&
                routeProgress != null &&
                lookAheadDistanceMeters > 0.0
            ) {
                routeProgressTracker
                    .getLookAheadCoordinates(
                        progress = routeProgress,
                        distanceMeters =
                            lookAheadDistanceMeters
                    )
                    ?: displayCoordinates
            } else {
                displayCoordinates
            }
        val targetLatitude =
            displayCoordinates.latitude

        val targetLongitude =
            displayCoordinates.longitude

        val startLatitude = animationState.latitude
        val startLongitude = animationState.longitude
        val startBearing = animationState.bearing

        val startCameraLatitude =
            animationState.cameraLatitude

        val startCameraLongitude =
            animationState.cameraLongitude

        val targetCameraLatitude =
            cameraTargetCoordinates.latitude

        val targetCameraLongitude =
            cameraTargetCoordinates.longitude

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

        val requestedCameraDistanceMeters =
            calculateCameraDistanceMeters(
                currentLocation.speedMps
            )

        val targetCameraDistanceMeters =
            animationState.cameraDistanceMeters +
                    (
                            requestedCameraDistanceMeters -
                                    animationState.cameraDistanceMeters
                            ) *
                    ZOOM_RESPONSE_FACTOR

        val startCameraDistanceMeters =
            animationState.cameraDistanceMeters

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

            val cameraDistanceMeters =
                startCameraDistanceMeters +
                        (
                                targetCameraDistanceMeters -
                                        startCameraDistanceMeters
                                ) *
                        progress

            val cameraLatitude =
                startCameraLatitude +
                        (
                                targetCameraLatitude -
                                        startCameraLatitude
                                ) *
                        progress

            val cameraLongitude =
                startCameraLongitude +
                        (
                                targetCameraLongitude -
                                        startCameraLongitude
                                ) *
                        progress

            val coordinates = GeoCoordinates(
                latitude,
                longitude
            )

            marker.setCoordinates(
                coordinates
            )

            val cameraCoordinates =
                GeoCoordinates(
                    cameraLatitude,
                    cameraLongitude
                )

            if (
                state.isFollowing &&
                !state.isRecentering
            ) {
                cameraController.follow(
                    coordinates = cameraCoordinates,
                    bearingDegrees = bearing,
                    zoomDistanceMeters =
                        cameraDistanceMeters
                )
            }

            animationState.latitude = latitude
            animationState.longitude = longitude
            animationState.bearing = bearing
            animationState.cameraLatitude =
                cameraLatitude

            animationState.cameraLongitude =
                cameraLongitude
            animationState.cameraDistanceMeters =
                cameraDistanceMeters
        }
    }


    // Periodic route refresh ETA
    LaunchedEffect(
        navigationLocation?.latitude,
        navigationLocation?.longitude,
        state.destination,
        isDemoMode.value
    ) {
        if (isDemoMode.value) {
            return@LaunchedEffect
        }

        val currentLocation =
            navigationLocation ?: return@LaunchedEffect

        val destination =
            state.destination ?: return@LaunchedEffect

        val currentRoute =
            state.activeRoute ?: return@LaunchedEffect

        val now =
            SystemClock.elapsedRealtime()

        if (
            now - lastRouteRefreshMs.longValue <
            routeRefreshIntervalMs
        ) {
            return@LaunchedEffect
        }


        val isCurrentlyOffRoute =
            state.routeProgress
                ?.distanceFromRouteMeters
                ?.let { distance ->
                    distance >
                            settings.offRouteThresholdMeters
                }
                ?: false

        if (isCurrentlyOffRoute) {
            // Traffic refresh must not act as an implicit reroute.
            lastRouteRefreshMs.longValue = now

            Log.d(
                TAG,
                "Route refresh skipped: vehicle is off route."
            )

            return@LaunchedEffect
        }

        if (isRouteRequestInProgress.value) {
            return@LaunchedEffect
        }

        val currentRemainingDurationSeconds =
            state.routeProgress
                ?.remainingDurationSeconds
                ?: currentRoute.duration.seconds

        lastRouteRefreshMs.longValue = now
        isRouteRequestInProgress.value = true

        val start = GeoCoordinates(
            currentLocation.latitude,
            currentLocation.longitude
        )

        routingController.calculateRoute(
            start = start,
            destination = destination,
            startHeadingDegrees =
                currentLocation.bearingDegrees,
            onSuccess = { candidateRoute ->
                val candidateDurationSeconds =
                    candidateRoute.duration.seconds

                val routeGainSeconds =
                    currentRemainingDurationSeconds -
                            candidateDurationSeconds

                val minimumGainSeconds =
                    settings.minimumRouteGainSeconds.toLong()

                if (
                    routeGainSeconds >=
                    minimumGainSeconds
                ) {
                    state.activeRoute =
                        candidateRoute

                    routeProgressTracker.setRoute(
                        candidateRoute
                    )
                    maneuverProgressTracker.setRoute(
                        candidateRoute
                    )
                    state.routeProgress =
                        routeProgressTracker.update(
                            start
                        )
                    maneuverGuidance.value =
                        maneuverProgressTracker.update(
                            state.routeProgress
                        )
                    routeRenderer.showRoute(
                        route = candidateRoute,
                        destination = destination
                    )

                    Log.d(
                        TAG,
                        "Traffic route accepted: " +
                                "currentEta=$currentRemainingDurationSeconds s, " +
                                "candidateEta=$candidateDurationSeconds s, " +
                                "gain=$routeGainSeconds s"
                    )
                } else {
                    Log.d(
                        TAG,
                        "Traffic route ignored: " +
                                "currentEta=$currentRemainingDurationSeconds s, " +
                                "candidateEta=$candidateDurationSeconds s, " +
                                "gain=$routeGainSeconds s, " +
                                "required=$minimumGainSeconds s"
                    )
                }

                isRouteRequestInProgress.value =
                    false
            },
            onError = { error ->
                isRouteRequestInProgress.value = false
                Log.e(
                    TAG,
                    "Route refresh failed: ${error.name}"
                )
            }
        )
    }

    val guidanceReservedLeftPx =
        if (state.activeRoute != null) {
            with(density) {
                GUIDANCE_RESERVED_LEFT_DP
                    .dp
                    .toPx()
                    .toDouble()
            }
        } else {
            0.0
        }
    // Set principal point
    LaunchedEffect(
        state.mapSize.width,
        state.mapSize.height,
        guidanceReservedLeftPx
    ) {
        cameraController.setNavigationPrincipalPoint(
            width = state.mapSize.width,
            height = state.mapSize.height,
            reservedLeftPx =
                guidanceReservedLeftPx
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
                state.destinationTitle =
                    result.title

                offRouteSinceMs.longValue = 0L
                lastAutoRerouteRequestMs.longValue = 0L
                autoRerouteCount.intValue = 0

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
                        navigationLocation?.bearingDegrees,
                    onSuccess = { route ->
                        state.activeRoute = route

                        routeProgressTracker.setRoute(
                            route
                        )
                        maneuverProgressTracker.setRoute(
                            route
                        )

                        state.routeProgress =
                            routeProgressTracker.update(
                                start
                            )
                        maneuverGuidance.value =
                            maneuverProgressTracker.update(
                                state.routeProgress
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

        state.activeRoute?.let { route ->
            Aw11DemoControls(
                isDemoMode =
                    isDemoMode.value,
                speedMultiplier =
                    demoSpeedMultiplier.value,
                isPaused =
                    isDemoPaused.value,
                onStart = {
                    demoSpeedMultiplier.value =
                        1.0

                    val initialLocation =
                        demoNavigationController.start(
                            route = route,
                            speedMultiplier =
                                demoSpeedMultiplier.value
                        )

                    if (initialLocation != null) {
                        demoLocation.value =
                            initialLocation

                        isDemoMode.value = true

                        isDemoRunning.value = true
                        isDemoPaused.value = false
                        onDemoDataChanged(
                            initialLocation,
                            demoNavigationController.tripData
                        )
                        Log.d(
                            TAG,
                            "Demo started: 1X"
                        )
                    }
                },
                onMultiplierChange = { multiplier ->
                    demoSpeedMultiplier.value =
                        multiplier

                    Log.d(
                        TAG,
                        "Demo speed: ${multiplier}X"
                    )
                },
                onPause = {
                    demoLocation.value =
                        demoNavigationController.pause()
                    onDemoDataChanged(
                        demoLocation.value,
                        demoNavigationController.tripData
                    )
                    isDemoPaused.value =
                        true

                    Log.d(
                        TAG,
                        "Demo paused."
                    )
                },
                onResume = {
                    demoLocation.value =
                        demoNavigationController.resume()
                    onDemoDataChanged(
                        demoLocation.value,
                        demoNavigationController.tripData
                    )
                    isDemoPaused.value =
                        false

                    Log.d(
                        TAG,
                        "Demo resumed."
                    )
                },
                onStop = {
                    demoNavigationController.stop()

                    isDemoRunning.value = false
                    isDemoPaused.value = false
                    isDemoMode.value = false

                    demoLocation.value = null
                    onDemoDataChanged(
                        null,
                        null
                    )
                    Log.d(
                        TAG,
                        "Demo stopped."
                    )
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )
        }


        //Eta widget
        state.activeRoute?.let { route ->
            val progress =
                state.routeProgress

            Aw11RouteInfo(
                destinationTitle =
                    state.destinationTitle,
                distanceMeters =
                    progress?.remainingDistanceMeters
                        ?: route.lengthInMeters,
                durationSeconds =
                    progress?.remainingDurationSeconds
                        ?: route.duration.seconds,
                onEndGuidance = {
                    demoNavigationController.stop()

                    isDemoRunning.value = false
                    isDemoMode.value = false
                    demoLocation.value = null
                    onDemoDataChanged(
                        null,
                        null
                    )
                    routeRenderer.clearRoute()
                    routeProgressTracker.clear()
                    maneuverProgressTracker.clear()
                    maneuverGuidance.value = null

                    state.activeRoute = null
                    state.routeProgress = null
                    state.destination = null
                    state.destinationTitle = null

                    animationState.isSnappedToRoute = false

                    lastRouteRefreshMs.longValue = 0L

                    offRouteSinceMs.longValue = 0L
                    lastAutoRerouteRequestMs.longValue = 0L
                    autoRerouteCount.intValue = 0

                    Log.d(
                        TAG,
                        "Guidance session ended."
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 10.dp, bottom = 10.dp
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
                    zoomDistanceMeters =
                        animationState.cameraDistanceMeters,
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

private fun calculateLookAheadDistanceMeters(
    speedMps: Float
): Double {
    if (speedMps <= LOOK_AHEAD_MIN_SPEED_MPS) {
        return 0.0
    }

    return (
            speedMps *
                    LOOK_AHEAD_SECONDS
            )
        .coerceIn(
            LOOK_AHEAD_MIN_METERS,
            LOOK_AHEAD_MAX_METERS
        )
}

private fun calculateCameraDistanceMeters(
    speedMps: Float
): Double {
    val speedKmh =
        speedMps * 3.6

    return when {
        speedKmh <= 30.0 ->
            500.0

        speedKmh <= 50.0 ->
            interpolateZoom(
                speedKmh,
                30.0,
                50.0,
                500.0,
                575.0
            )

        speedKmh <= 70.0 ->
            interpolateZoom(
                speedKmh,
                50.0,
                70.0,
                575.0,
                650.0
            )

        speedKmh <= 90.0 ->
            interpolateZoom(
                speedKmh,
                70.0,
                90.0,
                650.0,
                725.0
            )

        speedKmh <= 110.0 ->
            interpolateZoom(
                speedKmh,
                90.0,
                110.0,
                725.0,
                825.0
            )

        speedKmh <= 130.0 ->
            interpolateZoom(
                speedKmh,
                110.0,
                130.0,
                825.0,
                950.0
            )

        else ->
            950.0
    }
}

private fun interpolateZoom(
    value: Double,
    startValue: Double,
    endValue: Double,
    startZoom: Double,
    endZoom: Double
): Double {
    val fraction =
        (
                (value - startValue) /
                        (endValue - startValue)
                )
            .coerceIn(
                0.0,
                1.0
            )

    return startZoom +
            (endZoom - startZoom) *
            fraction
}