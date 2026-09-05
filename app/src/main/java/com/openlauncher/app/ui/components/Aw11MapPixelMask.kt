package com.openlauncher.app.ui.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun Aw11MapPixelMask(
    modifier: Modifier = Modifier,
    cellSize: Dp = 5.dp,
    gutterWidth: Dp = 0.8.dp,
    alpha: Float = 0.16f
) {
    Canvas(
        modifier = modifier
    ) {
        val step =
            cellSize.toPx()

        val gutter =
            gutterWidth.toPx()

        val maskColor =
            Color.Black.copy(
                alpha = alpha
            )

        var x = 0f

        while (x < size.width) {
            drawRect(
                color = maskColor,
                topLeft = Offset(
                    x = x,
                    y = 0f
                ),
                size = Size(
                    width = gutter,
                    height = size.height
                )
            )

            x += step
        }

        var y = 0f

        while (y < size.height) {
            drawRect(
                color = maskColor,
                topLeft = Offset(
                    x = 0f,
                    y = y
                ),
                size = Size(
                    width = size.width,
                    height = gutter
                )
            )

            y += step
        }
    }
}