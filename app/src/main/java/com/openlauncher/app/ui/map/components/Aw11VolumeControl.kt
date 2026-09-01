package com.openlauncher.app.ui.components

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.theme.Aw11Border
import com.openlauncher.app.ui.theme.Aw11Dim
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import android.os.SystemClock
import android.view.KeyEvent

@Composable
internal fun Aw11VolumeControl(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val audioManager = remember(context) {
        context.getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager
    }

    val stream = AudioManager.STREAM_MUSIC

    var volume by remember {
        mutableIntStateOf(
            audioManager.getStreamVolume(stream)
        )
    }

    var maxVolume by remember {
        mutableIntStateOf(
            audioManager
                .getStreamMaxVolume(stream)
                .coerceAtLeast(1)
        )
    }

    var isMuted by remember {
        mutableStateOf(
            audioManager.isStreamMute(stream)
        )
    }

    fun refreshVolumeState() {
        volume =
            audioManager.getStreamVolume(stream)

        maxVolume =
            audioManager
                .getStreamMaxVolume(stream)
                .coerceAtLeast(1)

        isMuted =
            audioManager.isStreamMute(stream)
    }


    fun sendMediaKey(keyCode: Int) {
        val eventTime =
            SystemClock.uptimeMillis()

        audioManager.dispatchMediaKeyEvent(
            KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_DOWN,
                keyCode,
                0
            )
        )

        audioManager.dispatchMediaKeyEvent(
            KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_UP,
                keyCode,
                0
            )
        )
    }

    // Poll so hardware or external volume changes are reflected in the UI.
    LaunchedEffect(audioManager) {
        while (true) {
            refreshVolumeState()
            delay(500L)
        }
    }

    val activeSegments =
        if (isMuted) {
            0
        } else {
            (
                    volume.toFloat() /
                            maxVolume.toFloat() *
                            10f
                    )
                .roundToInt()
                .coerceIn(0, 10)
        }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = "VOL",
                color = Aw11Secondary,
                fontSize = 8.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(
                Modifier.weight(1f)
            )

            Text(
                text =
                    if (isMuted) {
                        "MUTE"
                    } else {
                        "%02d".format(volume)
                    },
                color =
                    if (isMuted) {
                        Aw11Secondary
                    } else {
                        Aw11Primary
                    },
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(
            Modifier.height(4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            horizontalArrangement =
                Arrangement.spacedBy(2.dp)
        ) {
            repeat(10) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(
                            if (
                                index <
                                activeSegments
                            ) {
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

        Spacer(
            Modifier.height(6.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(3.dp)
        ) {
            Aw11VolumeButton(
                label = "−",
                modifier = Modifier.weight(1f),
                onClick = {
                    audioManager.adjustStreamVolume(
                        stream,
                        AudioManager.ADJUST_LOWER,
                        0
                    )

                    refreshVolumeState()
                }
            )

            Aw11VolumeButton(
                label = "MUTE",
                selected = isMuted,
                modifier =
                    Modifier.weight(1.5f),
                onClick = {
                    val shouldMute = !isMuted

                    audioManager.adjustStreamVolume(
                        stream,
                        if (shouldMute) {
                            AudioManager.ADJUST_MUTE
                        } else {
                            AudioManager.ADJUST_UNMUTE
                        },
                        0
                    )

                    sendMediaKey(
                        if (shouldMute) {
                            KeyEvent.KEYCODE_MEDIA_PAUSE
                        } else {
                            KeyEvent.KEYCODE_MEDIA_PLAY
                        }
                    )

                    refreshVolumeState()
                }
            )

            Aw11VolumeButton(
                label = "+",
                modifier =
                    Modifier.weight(1f),
                onClick = {
                    audioManager.adjustStreamVolume(
                        stream,
                        AudioManager.ADJUST_RAISE,
                        0
                    )

                    refreshVolumeState()
                }
            )
        }
    }
}

@Composable
private fun Aw11VolumeButton(
    label: String,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(30.dp)
            .border(
                width = 1.dp,
                color =
                    if (selected) {
                        Aw11Primary
                    } else {
                        Aw11Border.copy(
                            alpha = 0.65f
                        )
                    }
            )
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 3.dp
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text = label,
            color =
                if (selected) {
                    Aw11Primary
                } else {
                    Aw11Secondary
                },
            fontSize =
                if (label == "MUTE") {
                    8.sp
                } else {
                    18.sp
                },
            letterSpacing =
                if (label == "MUTE") {
                    0.25.sp
                } else {
                    0.sp
                }
        )
    }
}