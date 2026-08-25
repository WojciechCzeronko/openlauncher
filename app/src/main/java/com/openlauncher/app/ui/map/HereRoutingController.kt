package com.openlauncher.app.ui.map

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.routing.CalculateRouteCallback
import com.here.sdk.routing.Route
import com.here.sdk.routing.RoutingEngine
import com.here.sdk.routing.RoutingError
import com.here.sdk.routing.RoutingOptions
import com.here.sdk.routing.Waypoint

class HereRoutingController {

    private val routingEngine: RoutingEngine =
        try {
            RoutingEngine()
        } catch (e: InstantiationErrorException) {
            throw RuntimeException(
                "Failed to initialize RoutingEngine: ${e.error.name}",
                e
            )
        }

    fun calculateRoute(
        start: GeoCoordinates,
        destination: GeoCoordinates,
        onSuccess: (Route) -> Unit,
        onError: (RoutingError) -> Unit
    ) {
        val startWaypoint = Waypoint(start)
        val destinationWaypoint = Waypoint(destination)

        val waypoints = listOf(
            startWaypoint,
            destinationWaypoint
        )

        routingEngine.calculateRoute(
            waypoints,
            RoutingOptions(),
            object : CalculateRouteCallback {

                override fun onRouteCalculated(
                    routingError: RoutingError?,
                    routes: List<Route>?
                ) {
                    if (
                        routingError == null &&
                        !routes.isNullOrEmpty()
                    ) {
                        onSuccess(routes.first())
                    } else {
                        onError(
                            routingError
                                ?: RoutingError.INTERNAL_ERROR
                        )
                    }
                }
            }
        )
    }

    fun dispose() {
        routingEngine.dispose()
    }
}