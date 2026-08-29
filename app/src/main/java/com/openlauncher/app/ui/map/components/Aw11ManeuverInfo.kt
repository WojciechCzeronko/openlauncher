package com.openlauncher.app.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
        when {
            guidance.distanceMeters <= 8 ->
                "NOW"

            guidance.distanceMeters < 1000 ->
                "${guidance.distanceMeters} M"

            else ->
                String.format(
                    "%.1f KM",
                    guidance.distanceMeters / 1000.0
                )
        }

    val actionLabel =
        maneuverActionLabel(
            guidance.actionName
        )

    val actionSymbol =
        maneuverActionSymbol(
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
            Text(
                text = actionSymbol,
                color = Aw11Primary,
                fontSize = 26.sp,
                modifier = Modifier
                    .width(54.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = actionLabel,
                    color = Aw11Secondary,
                    fontSize = 10.sp,
                    letterSpacing = 0.25.sp
                )

                Text(
                    text = distanceText,
                    color = Aw11Primary,
                    fontSize = 18.sp
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
        actionName.contains("U_TURN") ->
            "U-TURN"

        actionName.contains("SLIGHT_LEFT") ->
            "SLIGHT LEFT"

        actionName.contains("SLIGHT_RIGHT") ->
            "SLIGHT RIGHT"

        actionName.contains("SHARP_LEFT") ->
            "SHARP LEFT"

        actionName.contains("SHARP_RIGHT") ->
            "SHARP RIGHT"

        actionName.contains("LEFT_TURN") ->
            "TURN LEFT"

        actionName.contains("RIGHT_TURN") ->
            "TURN RIGHT"

        actionName.contains("KEEP_LEFT") ->
            "KEEP LEFT"

        actionName.contains("KEEP_RIGHT") ->
            "KEEP RIGHT"

        actionName.contains("ROUNDABOUT") ->
            "ROUNDABOUT"

        actionName.contains("STRAIGHT") ->
            "STRAIGHT"

        actionName.contains("ARRIVE") ->
            "ARRIVE"

        else ->
            actionName
                .replace("_", " ")
    }
}

private fun maneuverActionSymbol(
    actionName: String
): String {
    return when {
        actionName.contains("U_TURN_LEFT") ->
            "<U"

        actionName.contains("U_TURN_RIGHT") ->
            "U>"

        actionName.contains("LEFT") ->
            "<-"

        actionName.contains("RIGHT") ->
            "->"

        actionName.contains("ROUNDABOUT") ->
            "(O)"

        actionName.contains("ARRIVE") ->
            "X"

        else ->
            "^"
    }
}