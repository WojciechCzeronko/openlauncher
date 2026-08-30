package com.openlauncher.app.ui.map.here

import android.util.Log
import com.here.sdk.core.Anchor2D
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.RenderSize
import com.here.sdk.routing.Route
import com.openlauncher.app.R
import com.here.sdk.core.Color as HereColor

private const val TAG = "HereRouteRenderer"

class HereRouteRenderer(
    private val mapView: MapView
) {
    private var routePolyline: MapPolyline? = null
    private var destinationMarker: MapMarker? = null

    fun showRoute(
        route: Route,
        destination: GeoCoordinates
    ) {
        clearRoute()

        createRoutePolyline(
            route.geometry
        )?.let { polyline ->
            mapView.mapScene.addMapPolyline(polyline)
            routePolyline = polyline
        }

        val marker =
            createDestinationMarker(destination)

        mapView.mapScene.addMapMarker(marker)
        destinationMarker = marker
    }

    fun clearRoute() {
        routePolyline?.let { polyline ->
            mapView.mapScene.removeMapPolyline(polyline)
        }

        destinationMarker?.let { marker ->
            mapView.mapScene.removeMapMarker(marker)
        }

        routePolyline = null
        destinationMarker = null
    }

    private fun createRoutePolyline(
        geometry: GeoPolyline
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
                geometry,
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
    ): MapMarker {
        val markerImage =
            MapImageFactory.fromResource(
                mapView.context.resources,
                R.drawable.ic_destination_checkered_48
            )

        val anchor =
            Anchor2D(
                0.5,
                1.0
            )

        return MapMarker(
            coordinates,
            markerImage,
            anchor
        )
    }

    fun updateRouteProgress(
        route: Route,
        matchedSegmentIndex: Int,
        matchedCoordinates: GeoCoordinates
    ) {
        val routeVertices =
            route.geometry.vertices

        if (routeVertices.size < 2) {
            return
        }

        val nextVertexIndex =
            (matchedSegmentIndex + 1)
                .coerceIn(
                    1,
                    routeVertices.lastIndex
                )

        val remainingVertices =
            ArrayList<GeoCoordinates>()

        remainingVertices.add(
            matchedCoordinates
        )

        for (
        index in nextVertexIndex..routeVertices.lastIndex
        ) {
            remainingVertices.add(
                routeVertices[index]
            )
        }

        if (remainingVertices.size < 2) {
            return
        }

        try {
            val remainingGeometry =
                GeoPolyline(
                    remainingVertices
                )

            replaceRoutePolyline(
                remainingGeometry
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to update route progress.",
                e
            )
        }
    }
    private fun replaceRoutePolyline(
        geometry: GeoPolyline
    ) {
        val newPolyline =
            createRoutePolyline(
                geometry
            ) ?: return

        // Add the new route before removing the previous one.
        // This prevents a frame where no route is rendered.
        mapView.mapScene.addMapPolyline(
            newPolyline
        )

        val oldPolyline =
            routePolyline

        routePolyline =
            newPolyline

        oldPolyline?.let { polyline ->
            mapView.mapScene.removeMapPolyline(
                polyline
            )
        }
    }
}
