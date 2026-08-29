package com.openlauncher.app.ui.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.openlauncher.app.ui.theme.Aw11Primary

@Composable
fun Aw11ManeuverIcon(
    actionName: String,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val strokeWidth =
            size.minDimension * 0.075f

        val stroke = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Square,
            join = StrokeJoin.Miter
        )

        when {
            actionName == "ARRIVE" ->
                drawArrivalIcon(
                    strokeWidth
                )

            actionName == "CONTINUE_ON" ->
                drawStraightIcon(
                    stroke,
                    strokeWidth
                )

            actionName == "LEFT_U_TURN" ->
                drawUTurnIcon(
                    turnLeft = true,
                    stroke = stroke,
                    strokeWidth = strokeWidth
                )

            actionName == "RIGHT_U_TURN" ->
                drawUTurnIcon(
                    turnLeft = false,
                    stroke = stroke,
                    strokeWidth = strokeWidth
                )

            actionName ==
                    "SHARP_LEFT_TURN" ->
                drawTurnIcon(
                    direction = -1f,
                    turnType =
                        TurnType.SHARP,
                    stroke = stroke,
                    strokeWidth =
                        strokeWidth
                )

            actionName ==
                    "SHARP_RIGHT_TURN" ->
                drawTurnIcon(
                    direction = 1f,
                    turnType =
                        TurnType.SHARP,
                    stroke = stroke,
                    strokeWidth =
                        strokeWidth
                )

            actionName ==
                    "SLIGHT_LEFT_TURN" ->
                drawTurnIcon(
                    direction = -1f,
                    turnType =
                        TurnType.SLIGHT,
                    stroke = stroke,
                    strokeWidth =
                        strokeWidth
                )

            actionName ==
                    "SLIGHT_RIGHT_TURN" ->
                drawTurnIcon(
                    direction = 1f,
                    turnType =
                        TurnType.SLIGHT,
                    stroke = stroke,
                    strokeWidth =
                        strokeWidth
                )

            actionName == "LEFT_TURN" ->
                drawTurnIcon(
                    direction = -1f,
                    turnType =
                        TurnType.NORMAL,
                    stroke = stroke,
                    strokeWidth =
                        strokeWidth
                )

            actionName == "RIGHT_TURN" ->
                drawTurnIcon(
                    direction = 1f,
                    turnType =
                        TurnType.NORMAL,
                    stroke = stroke,
                    strokeWidth =
                        strokeWidth
                )

            actionName == "LEFT_FORK" ||
                    actionName ==
                    "ENTER_HIGHWAY_FROM_LEFT" ||
                    actionName == "LEFT_RAMP" ||
                    actionName == "LEFT_EXIT" ->
                drawForkIcon(
                    direction = -1f,
                    stroke = stroke,
                    strokeWidth =
                        strokeWidth
                )

            actionName == "RIGHT_FORK" ||
                    actionName ==
                    "ENTER_HIGHWAY_FROM_RIGHT" ||
                    actionName == "RIGHT_RAMP" ||
                    actionName == "RIGHT_EXIT" ->
                drawForkIcon(
                    direction = 1f,
                    stroke = stroke,
                    strokeWidth =
                        strokeWidth
                )

            actionName == "MIDDLE_FORK" ->
                drawStraightIcon(
                    stroke,
                    strokeWidth
                )

            actionName.contains(
                "ROUNDABOUT"
            ) ->
                drawRoundaboutIcon(
                    actionName = actionName,
                    stroke = stroke,
                    strokeWidth =
                        strokeWidth
                )

            else ->
                drawStraightIcon(
                    stroke,
                    strokeWidth
                )
        }
    }
}

private enum class TurnType {
    NORMAL,
    SLIGHT,
    SHARP
}

private fun DrawScope.drawStraightIcon(
    stroke: Stroke,
    strokeWidth: Float
) {
    val x =
        size.width * 0.5f

    drawLine(
        color = Aw11Primary,
        start = Offset(
            x,
            size.height * 0.82f
        ),
        end = Offset(
            x,
            size.height * 0.20f
        ),
        strokeWidth = stroke.width,
        cap = StrokeCap.Square
    )

    drawArrowHead(
        tip = Offset(
            x,
            size.height * 0.12f
        ),
        direction = Offset(
            0f,
            -1f
        ),
        strokeWidth = strokeWidth
    )
}

private fun DrawScope.drawTurnIcon(
    direction: Float,
    turnType: TurnType,
    stroke: Stroke,
    strokeWidth: Float
) {
    val centerX =
        size.width * 0.5f

    val bottomY =
        size.height * 0.84f

    val turnY =
        when (turnType) {
            TurnType.SHARP ->
                size.height * 0.48f

            TurnType.NORMAL ->
                size.height * 0.48f

            TurnType.SLIGHT ->
                size.height * 0.54f
        }

    val targetX =
        when (turnType) {
            TurnType.SHARP ->
                centerX +
                        direction *
                        size.width * 0.35f

            TurnType.NORMAL ->
                centerX +
                        direction *
                        size.width * 0.36f

            TurnType.SLIGHT ->
                centerX +
                        direction *
                        size.width * 0.27f
        }

    val targetY =
        when (turnType) {
            TurnType.SHARP ->
                size.height * 0.32f

            TurnType.NORMAL ->
                turnY

            TurnType.SLIGHT ->
                size.height * 0.22f
        }

    val path = Path()

    path.moveTo(
        centerX,
        bottomY
    )

    path.lineTo(
        centerX,
        turnY
    )

    when (turnType) {
        TurnType.NORMAL -> {
            path.lineTo(
                targetX,
                turnY
            )
        }

        TurnType.SLIGHT -> {
            path.lineTo(
                targetX,
                targetY
            )
        }

        TurnType.SHARP -> {
            path.lineTo(
                centerX +
                        direction *
                        size.width * 0.13f,
                size.height * 0.36f
            )

            path.lineTo(
                targetX,
                targetY
            )
        }
    }

    drawPath(
        path = path,
        color = Aw11Primary,
        style = stroke
    )

    val arrowDirection =
        when (turnType) {
            TurnType.NORMAL ->
                Offset(
                    direction,
                    0f
                )

            TurnType.SLIGHT ->
                Offset(
                    direction,
                    -1f
                )

            TurnType.SHARP ->
                Offset(
                    direction,
                    -0.35f
                )
        }

    drawArrowHead(
        tip = Offset(
            targetX,
            targetY
        ),
        direction = arrowDirection,
        strokeWidth = strokeWidth
    )
}

private fun DrawScope.drawForkIcon(
    direction: Float,
    stroke: Stroke,
    strokeWidth: Float
) {
    val centerX =
        size.width * 0.5f

    val splitY =
        size.height * 0.52f

    val path = Path()

    path.moveTo(
        centerX,
        size.height * 0.86f
    )

    path.lineTo(
        centerX,
        splitY
    )

    path.lineTo(
        centerX +
                direction *
                size.width * 0.30f,
        size.height * 0.22f
    )

    drawPath(
        path = path,
        color = Aw11Primary,
        style = stroke
    )

    drawLine(
        color =
            Aw11Primary.copy(
                alpha = 0.35f
            ),
        start = Offset(
            centerX,
            splitY
        ),
        end = Offset(
            centerX -
                    direction *
                    size.width * 0.22f,
            size.height * 0.28f
        ),
        strokeWidth =
            strokeWidth * 0.65f,
        cap = StrokeCap.Square
    )

    drawArrowHead(
        tip = Offset(
            centerX +
                    direction *
                    size.width * 0.30f,
            size.height * 0.22f
        ),
        direction = Offset(
            direction,
            -1f
        ),
        strokeWidth = strokeWidth
    )
}

private fun DrawScope.drawUTurnIcon(
    turnLeft: Boolean,
    stroke: Stroke,
    strokeWidth: Float
) {
    val direction =
        if (turnLeft) {
            -1f
        } else {
            1f
        }

    val centerX =
        size.width * 0.5f

    val outerX =
        centerX +
                direction *
                size.width * 0.27f

    val path = Path()

    path.moveTo(
        centerX,
        size.height * 0.86f
    )

    path.lineTo(
        centerX,
        size.height * 0.36f
    )

    path.lineTo(
        outerX,
        size.height * 0.20f
    )

    path.lineTo(
        outerX,
        size.height * 0.47f
    )

    drawPath(
        path = path,
        color = Aw11Primary,
        style = stroke
    )

    drawArrowHead(
        tip = Offset(
            outerX,
            size.height * 0.55f
        ),
        direction = Offset(
            0f,
            1f
        ),
        strokeWidth = strokeWidth
    )
}

private fun DrawScope.drawRoundaboutIcon(
    actionName: String,
    stroke: Stroke,
    strokeWidth: Float
) {
    val center = Offset(
        size.width * 0.46f,
        size.height * 0.43f
    )

    val radius =
        size.minDimension * 0.21f

    // Roundabout ring.
    drawCircle(
        color = Aw11Primary,
        radius = radius,
        center = center,
        style = stroke
    )

    // Entrance road.
    drawLine(
        color = Aw11Primary,
        start = Offset(
            center.x,
            size.height * 0.88f
        ),
        end = Offset(
            center.x,
            center.y + radius
        ),
        strokeWidth = stroke.width,
        cap = StrokeCap.Square
    )

    if (
        actionName.contains(
            "_ROUNDABOUT_EXIT"
        )
    ) {
        val exitDirection =
            if (
                actionName.startsWith(
                    "LEFT_"
                )
            ) {
                -1f
            } else {
                1f
            }

        val exitStart = Offset(
            center.x +
                    exitDirection * radius,
            center.y
        )

        val exitEnd = Offset(
            center.x +
                    exitDirection *
                    size.width * 0.40f,
            center.y
        )

        drawLine(
            color = Aw11Primary,
            start = exitStart,
            end = exitEnd,
            strokeWidth = stroke.width,
            cap = StrokeCap.Square
        )

        drawArrowHead(
            tip = exitEnd,
            direction = Offset(
                exitDirection,
                0f
            ),
            strokeWidth = strokeWidth
        )
    }
}
private fun DrawScope.drawArrivalIcon(
    strokeWidth: Float
) {
    val center =
        Offset(
            size.width * 0.5f,
            size.height * 0.5f
        )

    val half =
        size.minDimension * 0.20f

    drawLine(
        color = Aw11Primary,
        start = Offset(
            center.x - half,
            center.y - half
        ),
        end = Offset(
            center.x + half,
            center.y + half
        ),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Square
    )

    drawLine(
        color = Aw11Primary,
        start = Offset(
            center.x + half,
            center.y - half
        ),
        end = Offset(
            center.x - half,
            center.y + half
        ),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Square
    )
}

private fun DrawScope.drawArrowHead(
    tip: Offset,
    direction: Offset,
    strokeWidth: Float
) {
    val length =
        size.minDimension * 0.15f

    val vectorLength =
        kotlin.math.sqrt(
            direction.x *
                    direction.x +
                    direction.y *
                    direction.y
        )

    if (vectorLength <= 0f) {
        return
    }

    val dx =
        direction.x /
                vectorLength

    val dy =
        direction.y /
                vectorLength

    val perpendicularX =
        -dy

    val perpendicularY =
        dx

    val baseX =
        tip.x -
                dx * length

    val baseY =
        tip.y -
                dy * length

    val halfWidth =
        length * 0.58f

    drawLine(
        color = Aw11Primary,
        start = tip,
        end = Offset(
            baseX +
                    perpendicularX *
                    halfWidth,
            baseY +
                    perpendicularY *
                    halfWidth
        ),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Square
    )

    drawLine(
        color = Aw11Primary,
        start = tip,
        end = Offset(
            baseX -
                    perpendicularX *
                    halfWidth,
            baseY -
                    perpendicularY *
                    halfWidth
        ),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Square
    )
}