package com.openlauncher.app.ui.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.map.HereSelectedLocation
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun Aw11SelectedLocationPanel(
    location: HereSelectedLocation,
    onGoThere: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .background(
                Aw11Background.copy(
                    alpha = 0.96f
                )
            )
            .border(
                width = 1.dp,
                color = Aw11Primary.copy(
                    alpha = 0.85f
                )
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event =
                            awaitPointerEvent(
                                PointerEventPass.Main
                            )

                        event.changes.forEach { change ->
                            change.consume()
                        }
                    }
                }
            }
            .padding(10.dp)
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = "SELECTED LOCATION",
                color = Aw11Primary,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                modifier =
                    Modifier.weight(1f)
            )

            Spacer(
                Modifier.width(6.dp)
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(
                        width = 1.dp,
                        color =
                            Aw11Primary.copy(
                                alpha = 0.85f
                            )
                    )
                    .clickable(
                        onClick = onClose
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Canvas(
                    modifier =
                        Modifier.size(18.dp)
                ) {
                    val strokeWidth =
                        2.dp.toPx()

                    drawLine(
                        color = Aw11Secondary,
                        start = Offset(
                            size.width * 0.2f,
                            size.height * 0.2f
                        ),
                        end = Offset(
                            size.width * 0.8f,
                            size.height * 0.8f
                        ),
                        strokeWidth =
                            strokeWidth,
                        cap =
                            StrokeCap.Square
                    )

                    drawLine(
                        color = Aw11Secondary,
                        start = Offset(
                            size.width * 0.8f,
                            size.height * 0.2f
                        ),
                        end = Offset(
                            size.width * 0.2f,
                            size.height * 0.8f
                        ),
                        strokeWidth =
                            strokeWidth,
                        cap =
                            StrokeCap.Square
                    )
                }
            }
        }

        Text(
            text =
                location.title
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.uppercase()
                    ?: "SELECTED POINT",
            color = Aw11Primary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis,
            modifier =
                Modifier.padding(top = 8.dp)
        )

        location.address
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { address ->
                Text(
                    text =
                        address.uppercase(),
                    color = Aw11Secondary,
                    fontSize = 9.sp,
                    maxLines = 2,
                    overflow =
                        TextOverflow.Ellipsis,
                    modifier =
                        Modifier.padding(
                            top = 3.dp
                        )
                )
            }

        Text(
            text =
                String.format(
                    "%.5f, %.5f",
                    location.coordinates.latitude,
                    location.coordinates.longitude
                ),
            color = Aw11Secondary,
            fontSize = 8.sp,
            modifier =
                Modifier.padding(top = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .border(
                    width = 1.dp,
                    color = Aw11Primary
                )
                .clickable(
                    onClick = onGoThere
                )
                .padding(
                    vertical = 8.dp
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text = "GO THERE",
                color = Aw11Primary,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}