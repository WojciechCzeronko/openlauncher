package com.openlauncher.app.ui.screen.aw11

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun Aw11CrtOverlay(
    modifier: Modifier = Modifier,
    scanlineAlpha: Float = 0.20f,
    vignetteAlpha: Float = 0.24f
) {
    Canvas(
        modifier = modifier
    ) {
        val scanlineSpacing =
            4.dp.toPx()

        val scanlineHeight =
            1.dp.toPx()

        var y = 0f

        while (y < size.height) {
            drawRect(
                color = Color.Black.copy(
                    alpha = scanlineAlpha
                ),
                topLeft = Offset(
                    x = 0f,
                    y = y
                ),
                size = Size(
                    width = size.width,
                    height = scanlineHeight
                )
            )

            y += scanlineSpacing
        }

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color.Black.copy(
                        alpha = vignetteAlpha
                    )
                ),
                center = Offset(
                    x = size.width / 2f,
                    y = size.height / 2f
                ),
                radius =
                    size.maxDimension * 0.68f
            )
        )
    }
}