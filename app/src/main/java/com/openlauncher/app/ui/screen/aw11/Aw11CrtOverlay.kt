package com.openlauncher.app.ui.screen.aw11

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.openlauncher.app.ui.theme.Aw11Primary
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
internal fun Aw11CrtOverlay(
    modifier: Modifier = Modifier,
    scanlineAlpha: Float = 0.20f,
    vignetteAlpha: Float = 0.24f,
    flickerAlpha: Float = 0.06f,
    noiseAlpha: Float = 0.05f,
    pixelMaskAlpha: Float = 0.085f
) {
    val infiniteTransition =
        rememberInfiniteTransition()

    val flicker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = flickerAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 110,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )

    var noiseY1 by remember {
        mutableFloatStateOf(0.25f)
    }

    var noiseY2 by remember {
        mutableFloatStateOf(0.65f)
    }

    var noiseY3 by remember {
        mutableFloatStateOf(0.85f)
    }

    var noiseStrength by remember {
        mutableFloatStateOf(0.5f)
    }

    LaunchedEffect(Unit) {
        while (true) {
            noiseY1 = Random.nextFloat()
            noiseY2 = Random.nextFloat()
            noiseY3 = Random.nextFloat()

            noiseStrength =
                0.35f +
                        Random.nextFloat() * 0.65f

            delay(75L)
        }
    }

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

        val pixelMaskSpacing =
            4.dp.toPx()

        val pixelMaskWidth =
            0.5.dp.toPx()

        var x = 0f

        while (x < size.width) {
            drawRect(
                color = Color.Black.copy(
                    alpha = pixelMaskAlpha
                ),
                topLeft = Offset(
                    x = x,
                    y = 0f
                ),
                size = Size(
                    width = pixelMaskWidth,
                    height = size.height
                )
            )

            x += pixelMaskSpacing
        }

        drawRect(
            color = Color.Black.copy(
                alpha = flicker
            )
        )

        // CRT horizontal noise / interference

        drawRect(
            color = Aw11Primary.copy(
                alpha =
                    noiseAlpha *
                            noiseStrength
            ),
            topLeft = Offset(
                x = 0f,
                y = size.height * noiseY1
            ),
            size = Size(
                width = size.width,
                height = 1.dp.toPx()
            )
        )

        drawRect(
            color = Color.Black.copy(
                alpha =
                    noiseAlpha *
                            noiseStrength *
                            1.2f
            ),
            topLeft = Offset(
                x = 0f,
                y = size.height * noiseY2
            ),
            size = Size(
                width = size.width,
                height = 1.dp.toPx()
            )
        )

        drawRect(
            color = Aw11Primary.copy(
                alpha =
                    noiseAlpha *
                            noiseStrength *
                            0.45f
            ),
            topLeft = Offset(
                x = 0f,
                y = size.height * noiseY3
            ),
            size = Size(
                width = size.width,
                height = 4.dp.toPx()
            )
        )

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