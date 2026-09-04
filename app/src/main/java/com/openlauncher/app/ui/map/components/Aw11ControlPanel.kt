package com.openlauncher.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.theme.Aw11Border
import com.openlauncher.app.ui.theme.Aw11DisplayGlow
import com.openlauncher.app.ui.theme.Aw11Dim
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import com.openlauncher.app.ui.theme.Handjet
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun Aw11ControlPanel(
    hasGps: Boolean,
    mediaAvailable: Boolean,
    onNav: () -> Unit,
    onMedia: () -> Unit,
    onApps: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var now by remember {
        mutableStateOf(
            LocalDateTime.now()
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000L)
        }
    }

    val time =
        now.format(
            DateTimeFormatter.ofPattern(
                "HH:mm",
                Locale.US
            )
        )

    val date =
        now.format(
            DateTimeFormatter.ofPattern(
                "EEE dd MMM",
                Locale.US
            )
        ).uppercase()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "SYSTEM",
            color = Aw11Secondary,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp
        )

        Spacer(
            Modifier.height(8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = time,
                color = Aw11Primary,
                fontFamily = Handjet,
                fontSize = 28.sp,
                style = TextStyle(
                    shadow = Aw11DisplayGlow,
                    textGeometricTransform = TextGeometricTransform(
                        scaleX = 1.12f
                    )
                )
            )
        }

        Spacer(
            Modifier.height(3.dp)
        )

        Text(
            text = date,
            color = Aw11Secondary,
            fontSize = 9.sp,
            letterSpacing = 0.25.sp
        )

        Spacer(
            Modifier.height(7.dp)
        )

        Text(
            text =
                if (hasGps) {
                    "GPS LOCK"
                } else {
                    "GPS SEARCH"
                },
            color =
                if (hasGps) {
                    Aw11Primary
                } else {
                    Aw11Dim
                },
            fontSize = 8.sp
        )

        Spacer(
            Modifier.height(14.dp)
        )

        Column(
            verticalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {
            Aw11ControlButton(
                label = "NAV",
                onClick = onNav
            )

            Aw11ControlButton(
                label = "MEDIA",
                enabled = mediaAvailable,
                onClick = onMedia
            )

            Spacer(
                Modifier.height(6.dp)
            )

            Aw11ControlButton(
                label = "APPS",
                onClick = onApps
            )

            Aw11ControlButton(
                label = "SETTINGS",
                onClick = onSettings
            )
        }

        Spacer(
            Modifier.weight(1f)
        )

        Aw11VolumeControl()

        Spacer(
            Modifier.height(8.dp)
        )

        Text(
            text = "AW11",
            color =
                Aw11Secondary.copy(
                    alpha = 0.55f
                ),
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            modifier =
                Modifier.align(
                    Alignment.CenterHorizontally
                )
        )
    }
}

@Composable
private fun Aw11ControlButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(
                width = 1.dp,
                color =
                    Aw11Border.copy(
                        alpha =
                            if (enabled) {
                                0.65f
                            } else {
                                0.30f
                            }
                    )
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(
                horizontal = 10.dp
            ),
        contentAlignment =
            Alignment.CenterStart
    ) {
        Text(
            text = "> $label",
            color =
                if (enabled) {
                    Aw11Primary
                } else {
                    Aw11Dim
                },
            fontSize = 11.sp,
            letterSpacing = 0.5.sp
        )
    }
}