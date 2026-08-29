package com.openlauncher.app.ui.map.navigation

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.routing.Route
import kotlin.math.roundToInt

private const val MANEUVER_PASS_TOLERANCE_METERS = 8.0

data class ManeuverGuidance(
    val actionName: String,
    val distanceMeters: Int,
    val instruction: String
)

class ManeuverProgressTracker {

    private var maneuvers:
            List<RouteManeuverPoint> =
        emptyList()

    private var totalGeometryLengthMeters =
        0.0

    fun setRoute(
        route: Route
    ) {
        val vertices =
            route.geometry.vertices

        if (vertices.size < 2) {
            clear()
            return
        }

        val cumulativeDistances =
            DoubleArray(
                vertices.size
            )

        var cumulativeDistance =
            0.0

        for (
        index in
        0 until vertices.lastIndex
        ) {
            cumulativeDistance +=
                vertices[index]
                    .distanceTo(
                        vertices[index + 1]
                    )

            cumulativeDistances[
                index + 1
            ] = cumulativeDistance
        }

        totalGeometryLengthMeters =
            cumulativeDistance

        maneuvers =
            route.sections
                .flatMap { section ->
                    section.maneuvers
                }
                .filter { maneuver ->
                    maneuver.action.name !=
                            "DEPART"
                }
                .map { maneuver ->
                    val closestVertexIndex =
                        findClosestVertexIndex(
                            coordinates =
                                maneuver.coordinates,
                            vertices =
                                vertices
                        )

                    RouteManeuverPoint(
                        actionName =
                            maneuver.action.name,
                        instruction =
                            maneuver.text
                                .trim(),
                        distanceAlongGeometryMeters =
                            cumulativeDistances[
                                closestVertexIndex
                            ]
                    )
                }
                .sortedBy { maneuver ->
                    maneuver
                        .distanceAlongGeometryMeters
                }
    }

    fun update(
        progress: RouteProgress?
    ): ManeuverGuidance? {
        if (
            progress == null ||
            maneuvers.isEmpty() ||
            totalGeometryLengthMeters <= 0.0
        ) {
            return null
        }

        val progressDistanceMeters =
            totalGeometryLengthMeters *
                    progress.progressFraction

        val nextManeuver =
            maneuvers.firstOrNull {
                    maneuver ->

                maneuver
                    .distanceAlongGeometryMeters >=
                        progressDistanceMeters -
                        MANEUVER_PASS_TOLERANCE_METERS
            }
                ?: return null

        val remainingDistanceMeters =
            (
                    nextManeuver
                        .distanceAlongGeometryMeters -
                            progressDistanceMeters
                    )
                .coerceAtLeast(0.0)
                .roundToInt()

        return ManeuverGuidance(
            actionName =
                nextManeuver.actionName,
            distanceMeters =
                remainingDistanceMeters,
            instruction =
                nextManeuver.instruction
        )
    }

    fun clear() {
        maneuvers =
            emptyList()

        totalGeometryLengthMeters =
            0.0
    }

    private fun findClosestVertexIndex(
        coordinates: GeoCoordinates,
        vertices: List<GeoCoordinates>
    ): Int {
        return vertices.indices
            .minByOrNull { index ->
                coordinates.distanceTo(
                    vertices[index]
                )
            }
            ?: 0
    }

    private data class RouteManeuverPoint(
        val actionName: String,
        val instruction: String,
        val distanceAlongGeometryMeters:
        Double
    )
}