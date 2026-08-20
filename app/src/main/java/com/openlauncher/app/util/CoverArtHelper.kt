package com.openlauncher.app.util

import android.graphics.Bitmap
import android.graphics.Color

object CoverArtHelper {

    fun createRetroAlbumArt(
        source: Bitmap,
        downscaleSize: Int = 80,
        ditherStrength: Float = 14f,
        contrastBoost: Float = 1.3f,
        edgeBoost: Float = 0.5f
    ): Bitmap {
        // Downscale first to create the pixel-art base
        val small = Bitmap.createScaledBitmap(
            source,
            downscaleSize,
            downscaleSize,
            false
        )

        val working = small.copy(Bitmap.Config.ARGB_8888, true)

        // 4x4 Bayer matrix for ordered dithering
        val bayer4 = arrayOf(
            intArrayOf(0, 8, 2, 10),
            intArrayOf(12, 4, 14, 6),
            intArrayOf(3, 11, 1, 9),
            intArrayOf(15, 7, 13, 5)
        )

        // 6-level AW11-style LCD palette
        val palette = intArrayOf(
            Color.rgb(0, 0, 0),
            Color.rgb(24, 28, 0),
            Color.rgb(48, 56, 6),
            Color.rgb(86, 96, 16),
            Color.rgb(145, 160, 36),
            Color.rgb(220, 235, 90)
        )

        // Build a luminance buffer first
        val luminance = Array(working.height) {
            FloatArray(working.width)
        }

        for (y in 0 until working.height) {
            for (x in 0 until working.width) {
                val pixel = working.getPixel(x, y)

                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                luminance[y][x] =
                    0.299f * r +
                            0.587f * g +
                            0.114f * b
            }
        }

        for (y in 0 until working.height) {
            for (x in 0 until working.width) {
                val pixel = working.getPixel(x, y)
                val alpha = Color.alpha(pixel)

                val baseLuma = luminance[y][x]

                // Calculate a simple local blur from neighboring pixels.
                var neighborSum = 0f
                var neighborCount = 0

                for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (dx == 0 && dy == 0) continue

                        val nx = x + dx
                        val ny = y + dy

                        if (
                            nx in 0 until working.width &&
                            ny in 0 until working.height
                        ) {
                            neighborSum += luminance[ny][nx]
                            neighborCount++
                        }
                    }
                }

                val localAverage =
                    if (neighborCount > 0) {
                        neighborSum / neighborCount
                    } else {
                        baseLuma
                    }

                // Unsharp-mask style edge enhancement
                val sharpenedLuma =
                    baseLuma +
                            (baseLuma - localAverage) * edgeBoost

                // Increase contrast around mid-gray
                val contrastedLuma =
                    ((sharpenedLuma - 128f) * contrastBoost + 128f)
                        .coerceIn(0f, 255f)

                // Apply ordered dithering
                val threshold = bayer4[y % 4][x % 4]
                val normalizedThreshold =
                    (threshold / 15f) - 0.5f

                val ditheredLuma =
                    (
                            contrastedLuma +
                                    normalizedThreshold * ditherStrength
                            ).coerceIn(0f, 255f)

                val paletteIndex = when {
                    ditheredLuma < 32f  -> 0
                    ditheredLuma < 64f  -> 1
                    ditheredLuma < 96f  -> 2
                    ditheredLuma < 128f -> 3
                    ditheredLuma < 176f -> 4
                    else                -> 5
                }

                val mapped = palette[paletteIndex]

                working.setPixel(
                    x,
                    y,
                    Color.argb(
                        alpha,
                        Color.red(mapped),
                        Color.green(mapped),
                        Color.blue(mapped)
                    )
                )
            }
        }

        // Scale back up without interpolation
        return Bitmap.createScaledBitmap(
            working,
            source.width,
            source.height,
            false
        )
    }
}