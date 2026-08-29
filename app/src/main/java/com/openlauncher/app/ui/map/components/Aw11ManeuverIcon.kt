package com.openlauncher.app.ui.map.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.openlauncher.app.R
import com.openlauncher.app.ui.theme.Aw11Primary

@Composable
fun Aw11ManeuverIcon(
    actionName: String,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(
            id = maneuverIconResource(
                actionName
            )
        ),
        contentDescription = null,
        colorFilter = ColorFilter.tint(
            Aw11Primary
        ),
        modifier = modifier
    )
}

@DrawableRes
private fun maneuverIconResource(
    actionName: String
): Int {
    return when {
        actionName.contains("ROUNDABOUT") ->
            R.drawable.ic_maneuver_roundabout

        actionName == "ARRIVE" ->
            R.drawable.ic_destination_checkered_128

        actionName == "LEFT_TURN" ->
            R.drawable.ic_maneuver_left

        actionName == "RIGHT_TURN" ->
            R.drawable.ic_maneuver_right

        actionName == "CONTINUE_ON" ->
            R.drawable.ic_maneuver_straight

        actionName.contains("LEFT") ->
            R.drawable.ic_maneuver_left

        actionName.contains("RIGHT") ->
            R.drawable.ic_maneuver_right

        else ->
            R.drawable.ic_maneuver_straight
    }
}