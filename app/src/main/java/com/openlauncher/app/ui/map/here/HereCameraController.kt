package com.openlauncher.app.ui.map.here

import com.here.sdk.animation.AnimationState
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoCoordinatesUpdate
import com.here.sdk.core.GeoOrientationUpdate
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.MapCameraAnimationFactory
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapView
import com.here.time.Duration

private const val DEFAULT_ZOOM_DISTANCE_METERS = 500.0
private const val PRINCIPAL_POINT_VERTICAL_RATIO = 0.72
private const val RECENTER_DURATION_MS = 1000L


class HereCameraController(
    private val mapView: MapView
) {

    fun showInitialPosition(
        coordinates: GeoCoordinates
    ) {
        mapView.camera.lookAt(
            coordinates,
            createZoom()
        )
    }

    fun follow(
        coordinates: GeoCoordinates,
        bearingDegrees: Float,
        zoomDistanceMeters: Double
    ) {
        val orientation = GeoOrientationUpdate(
            bearingDegrees.toDouble(),
            0.0
        )

        mapView.camera.lookAt(
            coordinates,
            orientation,
            createZoom(
                zoomDistanceMeters
            )
        )
    }

    fun setNavigationPrincipalPoint(
        width: Int,
        height: Int
    ) {
        if (width <= 0 || height <= 0) {
            return
        }

        val principalPoint = Point2D(
            width / 2.0,
            height * PRINCIPAL_POINT_VERTICAL_RATIO
        )

        mapView.camera.setPrincipalPoint(
            principalPoint
        )
    }

    fun recenter(
        coordinates: GeoCoordinates,
        bearingDegrees: Float,
        zoomDistanceMeters: Double,
        onFinished: () -> Unit
    ) {
        val orientation = GeoOrientationUpdate(
            bearingDegrees.toDouble(),
            0.0
        )

        val cameraAnimation =
            MapCameraAnimationFactory.flyTo(
                GeoCoordinatesUpdate(coordinates),
                orientation,
                createZoom(
                    zoomDistanceMeters
                ),
                0.0,
                Duration.ofMillis(
                    RECENTER_DURATION_MS
                )
            )

        mapView.camera.startAnimation(
            cameraAnimation
        ) { animationState ->
            if (
                animationState == AnimationState.COMPLETED ||
                animationState == AnimationState.CANCELLED
            ) {
                onFinished()
            }
        }
    }

    private fun createZoom(
        distanceMeters: Double =
            DEFAULT_ZOOM_DISTANCE_METERS
    ): MapMeasure {
        return MapMeasure(
            MapMeasure.Kind.DISTANCE_IN_METERS,
            distanceMeters
        )
    }
}