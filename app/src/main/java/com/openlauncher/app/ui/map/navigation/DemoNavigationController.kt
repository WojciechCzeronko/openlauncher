package com.openlauncher.app.ui.map.navigation

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.routing.Route
import com.openlauncher.app.util.LocationData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private const val DEFAULT_DEMO_SPEED_MPS = 13.89
private const val DEMO_ACCURACY_METERS = 3f

class DemoNavigationController {

    private var segments: List<DemoSegment> =
        emptyList()

    private var currentSegmentIndex = 0

    private var distanceOnSegmentMeters = 0.0

    var isRunning: Boolean = false
        private set

    var isPaused: Boolean = false
        private set
    var speedMultiplier: Double = 1.0
        private set

    var currentLocation: LocationData? = null
        private set

    fun start(
        route: Route,
        speedMultiplier: Double = 1.0
    ): LocationData? {
        segments =
            buildDemoSegments(route)

        if (segments.isEmpty()) {
            stop()
            return null
        }

        this.speedMultiplier =
            speedMultiplier.coerceIn(
                0.25,
                8.0
            )

        currentSegmentIndex = 0
        distanceOnSegmentMeters = 0.0
        isRunning = true
        isPaused = false
        currentLocation =
            createCurrentLocation()

        return currentLocation
    }

    fun stop() {
        isRunning = false
        isPaused = false
        currentLocation =
            currentLocation?.copy(
                speedMps = 0f
            )
    }

    fun pause(): LocationData? {
        if (
            !isRunning ||
            isPaused
        ) {
            return currentLocation
        }

        isPaused = true

        currentLocation =
            currentLocation?.copy(
                speedMps = 0f
            )

        return currentLocation
    }

    fun resume(): LocationData? {
        if (
            !isRunning ||
            !isPaused
        ) {
            return currentLocation
        }

        isPaused = false

        currentLocation =
            createCurrentLocation()

        return currentLocation
    }
    fun setSpeedMultiplier(
        multiplier: Double
    ) {
        speedMultiplier =
            multiplier.coerceIn(
                0.25,
                8.0
            )

        if (isRunning) {
            currentLocation =
                createCurrentLocation()
        }
    }

    fun update(
        deltaSeconds: Double
    ): LocationData? {
        if (
            !isRunning ||
            segments.isEmpty()
        ) {
            return currentLocation
        }

        if (isPaused) {
            return currentLocation
        }

        var remainingTimeSeconds =
            deltaSeconds.coerceAtLeast(0.0)

        while (
            remainingTimeSeconds > 0.0 &&
            isRunning
        ) {
            val segment =
                segments[currentSegmentIndex]

            val speedMps =
                segment.averageSpeedMps *
                        speedMultiplier

            if (speedMps <= 0.0) {
                advanceToNextSegment()
                continue
            }

            val remainingDistanceMeters =
                (
                        segment.lengthMeters -
                                distanceOnSegmentMeters
                        )
                    .coerceAtLeast(0.0)

            val timeToSegmentEndSeconds =
                remainingDistanceMeters /
                        speedMps

            if (
                remainingTimeSeconds <
                timeToSegmentEndSeconds
            ) {
                distanceOnSegmentMeters +=
                    speedMps *
                            remainingTimeSeconds

                remainingTimeSeconds = 0.0
            } else {
                distanceOnSegmentMeters =
                    segment.lengthMeters

                remainingTimeSeconds -=
                    timeToSegmentEndSeconds

                if (
                    currentSegmentIndex <
                    segments.lastIndex
                ) {
                    currentSegmentIndex++
                    distanceOnSegmentMeters = 0.0
                } else {
                    isRunning = false
                    remainingTimeSeconds = 0.0
                }
            }
        }

        currentLocation =
            createCurrentLocation()

        return currentLocation
    }

    private fun createCurrentLocation(): LocationData? {
        if (segments.isEmpty()) {
            return null
        }

        val segment =
            segments[
                currentSegmentIndex.coerceIn(
                    0,
                    segments.lastIndex
                )
            ]

        val sample =
            samplePolyline(
                vertices = segment.vertices,
                distanceMeters =
                    distanceOnSegmentMeters
            ) ?: return null

        val speedMps =
            if (
                isRunning &&
                !isPaused
            ) {
                segment.averageSpeedMps *
                        speedMultiplier
            } else {
                0.0
            }

        return LocationData(
            latitude =
                sample.coordinates.latitude,
            longitude =
                sample.coordinates.longitude,
            altitude = 0.0,
            accuracy =
                DEMO_ACCURACY_METERS,
            speedMps =
                speedMps.toFloat(),
            bearingDegrees =
                sample.bearingDegrees
        )
    }

    private fun advanceToNextSegment() {
        if (
            currentSegmentIndex <
            segments.lastIndex
        ) {
            currentSegmentIndex++
            distanceOnSegmentMeters = 0.0
        } else {
            isRunning = false
        }
    }

    private fun buildDemoSegments(
        route: Route
    ): List<DemoSegment> {
        val result =
            mutableListOf<DemoSegment>()

        for (section in route.sections) {
            for (span in section.spans) {
                val vertices =
                    span.geometry.vertices

                val lengthMeters =
                    polylineLengthMeters(
                        vertices
                    )

                if (
                    vertices.size < 2 ||
                    lengthMeters <= 0.0
                ) {
                    continue
                }

                val durationSeconds =
                    span.duration.seconds
                        .toDouble()

                val averageSpeedMps =
                    if (durationSeconds > 0.0) {
                        lengthMeters /
                                durationSeconds
                    } else {
                        DEFAULT_DEMO_SPEED_MPS
                    }

                result.add(
                    DemoSegment(
                        vertices = vertices,
                        lengthMeters =
                            lengthMeters,
                        averageSpeedMps =
                            averageSpeedMps
                    )
                )
            }
        }

        if (result.isNotEmpty()) {
            return result
        }

        // Fallback for routes without usable span geometry.
        val vertices =
            route.geometry.vertices

        val lengthMeters =
            polylineLengthMeters(
                vertices
            )

        if (
            vertices.size < 2 ||
            lengthMeters <= 0.0
        ) {
            return emptyList()
        }

        val durationSeconds =
            route.duration.seconds
                .toDouble()

        val averageSpeedMps =
            if (durationSeconds > 0.0) {
                lengthMeters /
                        durationSeconds
            } else {
                DEFAULT_DEMO_SPEED_MPS
            }

        return listOf(
            DemoSegment(
                vertices = vertices,
                lengthMeters =
                    lengthMeters,
                averageSpeedMps =
                    averageSpeedMps
            )
        )
    }

    private fun samplePolyline(
        vertices: List<GeoCoordinates>,
        distanceMeters: Double
    ): PositionSample? {
        if (vertices.size < 2) {
            return null
        }

        var remainingDistance =
            distanceMeters.coerceAtLeast(0.0)

        for (
        index in 0
                until vertices.lastIndex
        ) {
            val start =
                vertices[index]

            val end =
                vertices[index + 1]

            val segmentLength =
                distanceMeters(
                    start,
                    end
                )

            if (segmentLength <= 0.0) {
                continue
            }

            if (
                remainingDistance <=
                segmentLength
            ) {
                val fraction =
                    (
                            remainingDistance /
                                    segmentLength
                            )
                        .coerceIn(
                            0.0,
                            1.0
                        )

                val coordinates =
                    interpolateCoordinates(
                        start = start,
                        end = end,
                        fraction = fraction
                    )

                return PositionSample(
                    coordinates = coordinates,
                    bearingDegrees =
                        calculateBearingDegrees(
                            start,
                            end
                        )
                )
            }

            remainingDistance -=
                segmentLength
        }

        val start =
            vertices[
                vertices.lastIndex - 1
            ]

        val end =
            vertices.last()

        return PositionSample(
            coordinates = end,
            bearingDegrees =
                calculateBearingDegrees(
                    start,
                    end
                )
        )
    }

    private fun interpolateCoordinates(
        start: GeoCoordinates,
        end: GeoCoordinates,
        fraction: Double
    ): GeoCoordinates {
        val safeFraction =
            fraction.coerceIn(
                0.0,
                1.0
            )

        return GeoCoordinates(
            start.latitude +
                    (
                            end.latitude -
                                    start.latitude
                            ) *
                    safeFraction,
            start.longitude +
                    (
                            end.longitude -
                                    start.longitude
                            ) *
                    safeFraction
        )
    }

    private fun polylineLengthMeters(
        vertices: List<GeoCoordinates>
    ): Double {
        if (vertices.size < 2) {
            return 0.0
        }

        var totalDistance = 0.0

        for (
        index in 0
                until vertices.lastIndex
        ) {
            totalDistance +=
                distanceMeters(
                    vertices[index],
                    vertices[index + 1]
                )
        }

        return totalDistance
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

    private fun calculateBearingDegrees(
        start: GeoCoordinates,
        end: GeoCoordinates
    ): Float {
        val startLatitude =
            Math.toRadians(
                start.latitude
            )

        val endLatitude =
            Math.toRadians(
                end.latitude
            )

        val longitudeDelta =
            Math.toRadians(
                end.longitude -
                        start.longitude
            )

        val y =
            sin(longitudeDelta) *
                    cos(endLatitude)

        val x =
            cos(startLatitude) *
                    sin(endLatitude) -
                    sin(startLatitude) *
                    cos(endLatitude) *
                    cos(longitudeDelta)

        return (
                (
                        Math.toDegrees(
                            atan2(y, x)
                        ) +
                                360.0
                        ) %
                        360.0
                )
            .toFloat()
    }

    private data class DemoSegment(
        val vertices: List<GeoCoordinates>,
        val lengthMeters: Double,
        val averageSpeedMps: Double
    )

    private data class PositionSample(
        val coordinates: GeoCoordinates,
        val bearingDegrees: Float
    )
}