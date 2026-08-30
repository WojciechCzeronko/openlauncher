package com.openlauncher.app.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.map.navigation.ManeuverGuidance
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary

@Composable
fun Aw11ManeuverInfo(
    guidance: ManeuverGuidance,
    modifier: Modifier = Modifier
) {
    val distanceText =
        formatManeuverDistance(
            guidance.distanceMeters
        )

    val actionLabel =
        maneuverActionLabel(
            guidance.actionName
        )

    Column(
        modifier = modifier
            .width(190.dp)
            .background(
                Aw11Background.copy(
                    alpha = 0.92f
                )
            )
            .border(
                width = 1.dp,
                color = Aw11Primary.copy(
                    alpha = 0.85f
                )
            )
            .padding(
                horizontal = 10.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = "NEXT MANEUVER",
            color = Aw11Primary,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Aw11ManeuverIcon(
                actionName =
                    guidance.actionName,
                modifier = Modifier
                    .width(54.dp)
                    .height(54.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = actionLabel,
                    color = Aw11Secondary,
                    fontSize = 12.sp,
                    letterSpacing = 0.25.sp
                )

                Text(
                    text = distanceText,
                    color = Aw11Primary,
                    fontSize = 22.sp
                )
            }
        }

        if (guidance.roadName.isNotBlank()) {
            Text(
                text =
                    guidance.roadName
                        .uppercase(),
                color = Aw11Primary,
                fontSize = 11.sp,
                letterSpacing = 0.25.sp,
                maxLines = 2,
                overflow =
                    TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
        }
    }
}

private fun maneuverActionLabel(
    actionName: String
): String {
    return when {
        actionName == "ARRIVE" ->
            "ARRIVE"

        actionName == "CONTINUE_ON" ->
            "CONTINUE"

        actionName ==
                "ENTER_HIGHWAY_FROM_LEFT" ||
                actionName ==
                "ENTER_HIGHWAY_FROM_RIGHT" ->
            "ENTER HIGHWAY"

        actionName == "LEFT_EXIT" ->
            "LEFT EXIT"

        actionName == "RIGHT_EXIT" ->
            "RIGHT EXIT"

        actionName == "LEFT_FORK" ->
            "KEEP LEFT"

        actionName == "RIGHT_FORK" ->
            "KEEP RIGHT"

        actionName == "MIDDLE_FORK" ->
            "KEEP CENTER"

        actionName == "LEFT_RAMP" ->
            "LEFT RAMP"

        actionName == "RIGHT_RAMP" ->
            "RIGHT RAMP"

        actionName == "LEFT_U_TURN" ||
                actionName == "RIGHT_U_TURN" ->
            "U-TURN"

        actionName == "SHARP_LEFT_TURN" ->
            "SHARP LEFT"

        actionName == "SHARP_RIGHT_TURN" ->
            "SHARP RIGHT"

        actionName == "SLIGHT_LEFT_TURN" ->
            "SLIGHT LEFT"

        actionName == "SLIGHT_RIGHT_TURN" ->
            "SLIGHT RIGHT"

        actionName == "LEFT_TURN" ->
            "TURN LEFT"

        actionName == "RIGHT_TURN" ->
            "TURN RIGHT"

        actionName.endsWith(
            "_ROUNDABOUT_ENTER"
        ) ->
            "ENTER ROUNDABOUT"

        actionName.endsWith(
            "_ROUNDABOUT_PASS"
        ) ->
            "PASS ROUNDABOUT"

        actionName.contains(
            "_ROUNDABOUT_EXIT"
        ) ->
            roundaboutExitLabel(
                actionName
            )

        else ->
            actionName
                .replace(
                    "_",
                    " "
                )
    }
}

private fun roundaboutExitLabel(
    actionName: String
): String {
    val exitNumber =
        actionName
            .substringAfter(
                "_ROUNDABOUT_EXIT",
                ""
            )
            .toIntOrNull()

    return if (exitNumber != null) {
        "ROUNDABOUT EXIT $exitNumber"
    } else {
        "ROUNDABOUT EXIT"
    }
}

private fun formatManeuverDistance(
    distanceMeters: Int
): String {
    val distance =
        distanceMeters.coerceAtLeast(0)

    if (distance <= 8) {
        return "NOW"
    }

    if (distance >= 1000) {
        return String.format(
            "%.1f KM",
            distance / 1000.0
        )
    }

    val roundedDistance =
        when {
            distance >= 500 ->
                roundToNearestStep(
                    distance,
                    50
                )

            distance >= 100 ->
                roundToNearestStep(
                    distance,
                    10
                )

            distance >= 50 ->
                roundToNearestStep(
                    distance,
                    5
                )

            else ->
                distance
        }

    if (roundedDistance >= 1000) {
        return "1.0 KM"
    }

    return "$roundedDistance M"
}

private fun roundToNearestStep(
    value: Int,
    step: Int
): Int {
    return (
            (value + step / 2) /
                    step
            ) * step
}