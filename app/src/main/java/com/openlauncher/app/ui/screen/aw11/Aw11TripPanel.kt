package com.openlauncher.app.ui.screen.aw11

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.theme.Aw11Border
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import com.openlauncher.app.ui.theme.DSEG14Classic

@Composable
internal fun Aw11TripPanel(
    distance: String,
    driveTime: String,
    averageSpeed: String,
    maxSpeed: String,
    onResetTrip: () -> Unit,
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
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = "TRIP DATA",
                    color = Aw11Primary,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(
                    Modifier.weight(1f)
                )

                Text(
                    text = "RESET",
                    color = Aw11Secondary,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .clickable {
                            onResetTrip()
                        }
                        .padding(6.dp)
                )
            }

            Spacer(
                Modifier.height(8.dp)
            )

            Row(
                modifier =
                    Modifier.weight(1f),
                horizontalArrangement =
                    Arrangement.spacedBy(3.dp)
            ) {
                Aw11DataCell(
                    label = "DISTANCE",
                    value = distance,
                    modifier =
                        Modifier.weight(1f)
                )

                Aw11DataCell(
                    label = "DRIVE TIME",
                    value = driveTime,
                    modifier =
                        Modifier.weight(1f)
                )
            }

            Spacer(
                Modifier.height(3.dp)
            )

            Row(
                modifier =
                    Modifier.weight(1f),
                horizontalArrangement =
                    Arrangement.spacedBy(3.dp)
            ) {
                Aw11DataCell(
                    label = "AVG SPEED",
                    value = averageSpeed,
                    modifier =
                        Modifier.weight(1f)
                )

                Aw11DataCell(
                    label = "MAX SPEED",
                    value = maxSpeed,
                    modifier =
                        Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Aw11DataCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(
                1.dp,
                Aw11Border.copy(
                    alpha = 0.45f
                )
            )
            .padding(6.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Text(
            text = label,
            color = Aw11Secondary,
            fontSize = 8.sp,
            letterSpacing = 0.25.sp
        )

        Spacer(
            Modifier.height(5.dp)
        )

        Text(
            text = value,
            color = Aw11Primary,
            fontFamily = DSEG14Classic,
            fontSize = 20.sp
        )
    }
}