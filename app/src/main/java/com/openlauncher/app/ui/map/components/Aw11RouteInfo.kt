package com.openlauncher.app.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Primary
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import com.openlauncher.app.ui.theme.Aw11Warning

@Composable
fun Aw11RouteInfo(
    distanceMeters: Int,
    durationSeconds: Long,
    onEndGuidance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distanceKm =
        distanceMeters / 1000.0

    val durationMinutes =
        (durationSeconds + 59) / 60

    Box(
        modifier = modifier
            .background(
                Aw11Background.copy(alpha = 0.90f)
            )
            .border(
                width = 1.dp,
                color = Aw11Primary.copy(alpha = 0.85f)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format(
                    "%.1f KM  •  %d MIN",
                    distanceKm,
                    durationMinutes
                ),
                color = Aw11Primary,
                fontSize = 14.sp
            )

            Spacer(
                Modifier.width(14.dp)
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
                        horizontal = 8.dp,
                        vertical = 3.dp
                    )
            ) {
                Text(
                    text = "END",
                    color = Aw11Warning,
                    fontSize = 11.sp
                )
            }
        }
    }
}