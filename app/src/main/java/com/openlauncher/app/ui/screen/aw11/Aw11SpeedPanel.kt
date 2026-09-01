package com.openlauncher.app.ui.screen.aw11

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.theme.Aw11Border
import com.openlauncher.app.ui.theme.Aw11Dim
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import com.openlauncher.app.ui.theme.DSEG14Classic
import kotlin.math.roundToInt

@Composable
internal fun Aw11SpeedPanel(
    speedDisplay: Float,
    speedProgress: Float,
    isMetric: Boolean,
    showTestValues: Boolean,
    selfTestProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Aw11Border.copy(
                    alpha = 0.45f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = "SPEED",
                color = Aw11Primary,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(
                Modifier.weight(1f)
            )

            Row(
                modifier =
                    Modifier.height(
                        IntrinsicSize.Min
                    ),
                verticalAlignment =
                    Alignment.Top
            ) {
                Text(
                    text =
                        if (showTestValues) {
                            "888"
                        } else {
                            "%03.0f".format(
                                speedDisplay
                            )
                        },
                    color = Aw11Primary,
                    fontFamily = DSEG14Classic,
                    fontSize = 44.sp
                )

                Spacer(
                    Modifier.width(10.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(42.dp)
                ) {
                    Text(
                        text = "MPH",
                        color =
                            if (!isMetric) {
                                Aw11Primary
                            } else {
                                Aw11Dim.copy(
                                    alpha = 0.45f
                                )
                            },
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .align(
                                Alignment.TopStart
                            )
                            .offset(
                                y = (-2).dp
                            )
                    )

                    Text(
                        text = "KM/H",
                        color =
                            if (isMetric) {
                                Aw11Primary
                            } else {
                                Aw11Dim.copy(
                                    alpha = 0.45f
                                )
                            },
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .align(
                                Alignment.BottomStart
                            )
                            .offset(
                                y = 2.dp
                            )
                    )
                }
            }

            Spacer(
                Modifier.height(8.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                repeat(6) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(4.dp)
                            .background(
                                Aw11Secondary.copy(
                                    alpha = 0.55f
                                )
                            )
                    )
                }
            }

            Spacer(
                Modifier.height(3.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(2.dp)
            ) {
                repeat(20) { index ->
                    val activeSegments =
                        if (showTestValues) {
                            (
                                    selfTestProgress *
                                            20f
                                    )
                                .roundToInt()
                                .coerceIn(
                                    0,
                                    20
                                )
                        } else {
                            (
                                    speedProgress *
                                            20f
                                    )
                                .roundToInt()
                                .coerceIn(
                                    0,
                                    20
                                )
                        }

                    val active =
                        index < activeSegments

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (active) {
                                    Aw11Primary
                                } else {
                                    Aw11Dim.copy(
                                        alpha = 0.35f
                                    )
                                }
                            )
                    )
                }
            }
        }
    }
}