package com.openlauncher.app.ui.map.here

import android.util.Log
import com.here.sdk.core.GeoCircle
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolygon
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.MapPolygon
import com.here.sdk.mapview.MapPolyline
import com.here.sdk.mapview.MapView
import com.here.sdk.mapview.RenderSize
import com.here.sdk.routing.Route
import com.here.sdk.core.Color as HereColor
import com.here.sdk.core.GeoPolyline

private const val TAG = "HereRouteRenderer"

class HereRouteRenderer(
    private val mapView: MapView
) {
    private var routePolyline: MapPolyline? = null
    private var destinationMarker: MapPolygon? = null

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

        mapView.mapScene.addMapPolygon(marker)
        destinationMarker = marker
    }

    fun clearRoute() {
        routePolyline?.let { polyline ->
            mapView.mapScene.removeMapPolyline(polyline)
        }

        destinationMarker?.let { marker ->
            mapView.mapScene.removeMapPolygon(marker)
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
        routePolyline?.let { polyline ->
            mapView.mapScene.removeMapPolyline(
                polyline
            )
        }

        routePolyline = null

        createRoutePolyline(
            geometry
        )?.let { polyline ->
            mapView.mapScene.addMapPolyline(
                polyline
            )

            routePolyline = polyline
        }
    }
}
