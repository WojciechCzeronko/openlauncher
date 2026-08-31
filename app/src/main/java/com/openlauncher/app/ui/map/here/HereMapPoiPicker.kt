package com.openlauncher.app.ui.map.here

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Point2D
import com.here.sdk.core.Rectangle2D
import com.here.sdk.core.Size2D
import com.here.sdk.mapview.MapScene.MapPickFilter
import com.here.sdk.mapview.MapView

data class HerePickedPoi(
    val name: String?,
    val coordinates: GeoCoordinates
)

class HereMapPoiPicker(
    private val mapView: MapView
) {

    fun pick(
        touchPoint: Point2D,
        pickAreaSizePx: Double,
        onResult: (HerePickedPoi?) -> Unit
    ) {
        val halfSize =
            pickAreaSizePx / 2.0

        val origin =
            Point2D(
                (touchPoint.x - halfSize)
                    .coerceAtLeast(0.0),
                (touchPoint.y - halfSize)
                    .coerceAtLeast(0.0)
            )

        val rectangle =
            Rectangle2D(
                origin,
                Size2D(
                    pickAreaSizePx,
                    pickAreaSizePx
                )
            )

        val contentTypes =
            arrayListOf(
                MapPickFilter.ContentType.MAP_CONTENT
            )

        val filter =
            MapPickFilter(
                contentTypes
            )

        mapView.pick(
            filter,
            rectangle
        ) { mapPickResult ->
            val pickedPlace =
                mapPickResult
                    ?.mapContent
                    ?.pickedPlaces
                    ?.firstOrNull()

            if (pickedPlace == null) {
                onResult(null)
                return@pick
            }

            onResult(
                HerePickedPoi(
                    name =
                        pickedPlace.name,
                    coordinates =
                        pickedPlace.coordinates
                )
            )
        }
    }
}