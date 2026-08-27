package com.openlauncher.app.ui.map.navigation

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.routing.Route
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

data class RouteProgress(
    val remainingDistanceMeters: Int,
    val remainingDurationSeconds: Long,
    val distanceFromRouteMeters: Double,
    val progressFraction: Double,
    val matchedSegmentIndex: Int,
    val matchedCoordinates: GeoCoordinates
)

class RouteProgressTracker {

    private var route: Route? = null

    private var vertices: List<GeoCoordinates> =
        emptyList()

    private var segmentLengthsMeters =
        DoubleArray(0)

    private var cumulativeDistancesMeters =
        DoubleArray(0)

    private var totalGeometryLengthMeters = 0.0

    fun setRoute(route: Route) {
        this.route = route

        vertices =
            route.geometry.vertices

        if (vertices.size < 2) {
            clearGeometry()
            return
        }

        segmentLengthsMeters =
            DoubleArray(vertices.size - 1)

        cumulativeDistancesMeters =
            DoubleArray(vertices.size)

        var cumulativeDistance = 0.0

        for (index in 0 until vertices.lastIndex) {
            val start = vertices[index]
            val end = vertices[index + 1]

            val segmentLength =
                distanceMeters(
                    start,
                    end
                )

            segmentLengthsMeters[index] =
                segmentLength

            cumulativeDistance +=
                segmentLength

            cumulativeDistancesMeters[index + 1] =
                cumulativeDistance
        }

        totalGeometryLengthMeters =
            cumulativeDistance
    }

    fun clear() {
        route = null
        vertices = emptyList()

        clearGeometry()
    }

    fun update(
        position: GeoCoordinates
    ): RouteProgress? {
        val activeRoute =
            route ?: return null

        if (
            vertices.size < 2 ||
            totalGeometryLengthMeters <= 0.0
        ) {
            return null
        }

        var bestMatch: SegmentMatch? = null

        for (index in 0 until vertices.lastIndex) {
            val match =
                projectOntoSegment(
                    position = position,
                    start = vertices[index],
                    end = vertices[index + 1],
                    segmentIndex = index
                )

            if (
                bestMatch == null ||
                match.distanceFromRouteMeters <
                bestMatch.distanceFromRouteMeters
            ) {
                bestMatch = match
            }
        }

        val match =
            bestMatch ?: return null

        val distanceAlongGeometry =
            cumulativeDistancesMeters[
                match.segmentIndex
            ] +
                    segmentLengthsMeters[
                        match.segmentIndex
                    ] * match.segmentFraction

        val progressFraction =
            (
                    distanceAlongGeometry /
                            totalGeometryLengthMeters
                    )
                .coerceIn(
                    0.0,
                    1.0
                )

        val remainingFraction =
            1.0 - progressFraction

        val remainingDistanceMeters =
            (
                    activeRoute.lengthInMeters *
                            remainingFraction
                    )
                .roundToInt()
                .coerceAtLeast(0)

        val remainingDurationSeconds =
            (
                    activeRoute.duration.seconds *
                            remainingFraction
                    )
                .toLong()
                .coerceAtLeast(0L)

        return RouteProgress(
            remainingDistanceMeters =
                remainingDistanceMeters,
            remainingDurationSeconds =
                remainingDurationSeconds,
            distanceFromRouteMeters =
                match.distanceFromRouteMeters,
            progressFraction =
                progressFraction,
            matchedSegmentIndex =
                match.segmentIndex,
            matchedCoordinates =
                match.coordinates
        )
    }

    private fun clearGeometry() {
        segmentLengthsMeters =
            DoubleArray(0)

        cumulativeDistancesMeters =
            DoubleArray(0)

        totalGeometryLengthMeters = 0.0
    }

    private fun projectOntoSegment(
        position: GeoCoordinates,
        start: GeoCoordinates,
        end: GeoCoordinates,
        segmentIndex: Int
    ): SegmentMatch {
        val referenceLatitudeRadians =
            Math.toRadians(
                position.latitude
            )

        val metersPerDegreeLatitude =
            111_320.0

        val metersPerDegreeLongitude =
            111_320.0 *
                    cos(referenceLatitudeRadians)

        val startX =
            (
                    start.longitude -
                            position.longitude
                    ) *
                    metersPerDegreeLongitude

        val startY =
            (
                    start.latitude -
                            position.latitude
                    ) *
                    metersPerDegreeLatitude

        val endX =
            (
                    end.longitude -
                            position.longitude
                    ) *
                    metersPerDegreeLongitude

        val endY =
            (
                    end.latitude -
                            position.latitude
                    ) *
                    metersPerDegreeLatitude

        val segmentX =
            endX - startX

        val segmentY =
            endY - startY

        val segmentLengthSquared =
            segmentX * segmentX +
                    segmentY * segmentY

        val fraction =
            if (segmentLengthSquared > 0.0) {
                (
                        -startX * segmentX -
                                startY * segmentY
                        ) /
                        segmentLengthSquared
            } else {
                0.0
            }
                .coerceIn(
                    0.0,
                    1.0
                )

        val projectedX =
            startX +
                    segmentX * fraction

        val projectedY =
            startY +
                    segmentY * fraction

        val distanceFromRoute =
            hypot(
                projectedX,
                projectedY
            )

        val matchedLatitude =
            start.latitude +
                    (
                            end.latitude -
                                    start.latitude
                            ) *
                    fraction

        val matchedLongitude =
            start.longitude +
                    (
                            end.longitude -
                                    start.longitude
                            ) *
                    fraction

        return SegmentMatch(
            segmentIndex =
                segmentIndex,
            segmentFraction =
                fraction,
            distanceFromRouteMeters =
                distanceFromRoute,
            coordinates =
                GeoCoordinates(
                    matchedLatitude,
                    matchedLongitude
                )
        )
    }

    private fun distanceMeters(
        first: GeoCoordinates,
        second: GeoCoordinates
    ): Double {
        val latitudeRadians =
            Math.toRadians(
                (
                        first.latitude +
                                second.latitude
                        ) / 2.0
            )

        val metersPerDegreeLatitude =
            111_320.0

        val metersPerDegreeLongitude =
            111_320.0 *
                    cos(latitudeRadians)

        val x =
            (
                    second.longitude -
                            first.longitude
                    ) *
                    metersPerDegreeLongitude

        val y =
            (
                    second.latitude -
                            first.latitude
                    ) *
                    metersPerDegreeLatitude

        return hypot(
            x,
            y
        )
    }

    private data class SegmentMatch(
        val segmentIndex: Int,
        val segmentFraction: Double,
        val distanceFromRouteMeters: Double,
        val coordinates: GeoCoordinates
    )
}