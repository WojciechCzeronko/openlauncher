package com.openlauncher.app.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import com.openlauncher.app.ui.theme.Aw11Warning
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun Aw11RouteInfo(
    distanceMeters: Int,
    durationSeconds: Long,
    onEndGuidance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distanceText =
        if (distanceMeters < 1000) {
            "$distanceMeters M"
        } else {
            String.format(
                "%.1f KM",
                distanceMeters / 1000.0
            )
        }

    val durationText =
        when {
            durationSeconds <= 0L ->
                "0 MIN"

            durationSeconds < 60L ->
                "<1 MIN"

            else ->
                "${(durationSeconds + 59) / 60} MIN"
        }

    val arrivalTime =
        LocalTime.now()
            .plusSeconds(durationSeconds)
            .format(
                DateTimeFormatter.ofPattern(
                    "HH:mm"
                )
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
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = "ROUTE DATA",
                color = Aw11Primary,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )

            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Aw11Warning
                    )
                    .clickable(
                        onClick = onEndGuidance
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 4.dp
                    )
            ) {
                Text(
                    text = "END",
                    color = Aw11Warning,
                    fontSize = 12.sp
                )
            }
        }

        RouteDataRow(
            label = "DIST",
            value = distanceText
        )

        RouteDataRow(
            label = "TIME",
            value = durationText
        )

        RouteDataRow(
            label = "ARRIVAL",
            value = arrivalTime
        )
    }
}

@Composable
private fun RouteDataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 4.dp
            ),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Aw11Secondary,
            fontSize = 11.sp,
            letterSpacing = 0.25.sp
        )

        Text(
            text = value,
            color = Aw11Primary,
            fontSize = 12.sp
        )
    }
}