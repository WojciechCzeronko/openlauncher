package com.openlauncher.app.ui.map.navigation

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.routing.Route
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.roundToLong

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
    private var timedSpans: List<TimedSpan> =
        emptyList()

    private var totalTimedGeometryLengthMeters =
        0.0
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
        buildTimingProfile(route)
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
            calculateRemainingDurationSeconds(
                progressFraction =
                    progressFraction,
                fallbackDurationSeconds =
                    activeRoute.duration.seconds
            )

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

        timedSpans =
            emptyList()

        totalTimedGeometryLengthMeters =
            0.0
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

    private fun buildTimingProfile(
        route: Route
    ) {
        val profile =
            mutableListOf<TimedSpan>()

        var cumulativeDistance = 0.0

        for (section in route.sections) {
            for (span in section.spans) {

                val spanVertices =
                    span.geometry.vertices

                val spanLength =
                    polylineLengthMeters(
                        spanVertices
                    )

                if (spanLength <= 0.0) {
                    continue
                }

                val startDistance =
                    cumulativeDistance

                cumulativeDistance +=
                    spanLength

                profile.add(
                    TimedSpan(
                        startDistanceMeters =
                            startDistance,
                        endDistanceMeters =
                            cumulativeDistance,
                        durationSeconds =
                            span.duration.seconds.toDouble()
                    )
                )
            }
        }

        timedSpans =
            profile

        totalTimedGeometryLengthMeters =
            cumulativeDistance
    }



    private fun calculateRemainingDurationSeconds(
        progressFraction: Double,
        fallbackDurationSeconds: Long
    ): Long {
        if (
            timedSpans.isEmpty() ||
            totalTimedGeometryLengthMeters <= 0.0
        ) {
            return (
                    fallbackDurationSeconds *
                            (1.0 - progressFraction)
                    )
                .roundToLong()
                .coerceAtLeast(0L)
        }

        val progressDistance =
            totalTimedGeometryLengthMeters *
                    progressFraction
                        .coerceIn(
                            0.0,
                            1.0
                        )

        val currentSpanIndex =
            timedSpans.indexOfFirst { span ->
                progressDistance <=
                        span.endDistanceMeters
            }

        if (currentSpanIndex < 0) {
            return 0L
        }

        val currentSpan =
            timedSpans[currentSpanIndex]

        val currentSpanLength =
            currentSpan.endDistanceMeters -
                    currentSpan.startDistanceMeters

        val remainingCurrentSpanFraction =
            if (currentSpanLength > 0.0) {
                (
                        (
                                currentSpan.endDistanceMeters -
                                        progressDistance
                                ) /
                                currentSpanLength
                        )
                    .coerceIn(
                        0.0,
                        1.0
                    )
            } else {
                0.0
            }

        var remainingSeconds =
            currentSpan.durationSeconds *
                    remainingCurrentSpanFraction

        for (
        index in currentSpanIndex + 1
                until timedSpans.size
        ) {
            remainingSeconds +=
                timedSpans[index]
                    .durationSeconds
        }

        return remainingSeconds
            .roundToLong()
            .coerceAtLeast(0L)
    }

    private fun polylineLengthMeters(
        vertices: List<GeoCoordinates>
    ): Double {
        if (vertices.size < 2) {
            return 0.0
        }

        var totalDistance = 0.0

        for (index in 0 until vertices.lastIndex) {
            totalDistance +=
                distanceMeters(
                    vertices[index],
                    vertices[index + 1]
                )
        }

        return totalDistance
    }

    private data class SegmentMatch(
        val segmentIndex: Int,
        val segmentFraction: Double,
        val distanceFromRouteMeters: Double,
        val coordinates: GeoCoordinates
    )

    private data class TimedSpan(
        val startDistanceMeters: Double,
        val endDistanceMeters: Double,
        val durationSeconds: Double
    )
}

