package com.openlauncher.app.ui.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.openlauncher.app.data.AppSettings
import com.openlauncher.app.data.ClockStyle
import com.openlauncher.app.data.GRID_COLS
import com.openlauncher.app.data.GRID_ROWS
import com.openlauncher.app.data.UnitSystem
import com.openlauncher.app.data.WidgetConfig
import com.openlauncher.app.data.computeWidgetMove
import com.openlauncher.app.model.NowPlayingState
import com.openlauncher.app.model.WeatherState
import com.openlauncher.app.service.MediaListenerService
import com.openlauncher.app.ui.components.Aw11ControlPanel
import com.openlauncher.app.ui.theme.Aw11Border
import com.openlauncher.app.ui.theme.Aw11Dim
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import com.openlauncher.app.ui.theme.DSEG14Classic
import com.openlauncher.app.ui.widget.AltimeterWidget
import com.openlauncher.app.ui.widget.ClockWidget
import com.openlauncher.app.ui.widget.NowPlayingWidget
import com.openlauncher.app.ui.widget.SoundboardWidget
import com.openlauncher.app.ui.widget.SpeedometerWidget
import com.openlauncher.app.ui.widget.TelemetryWidget
import com.openlauncher.app.ui.widget.TripTrackerWidget
import com.openlauncher.app.ui.widget.VitalsWidget
import com.openlauncher.app.ui.widget.WeatherWidget
import com.openlauncher.app.ui.widget.clockTimeLabel
import com.openlauncher.app.util.CoverArtHelper.createRetroAlbumArt
import com.openlauncher.app.util.LocationData
import com.openlauncher.app.viewmodel.TripData
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.roundToInt
import com.openlauncher.app.ui.map.Aw11HereMap
import com.openlauncher.app.ui.map.navigation.DemoTripData

private val WIDGET_RADIUS = RoundedCornerShape(0.dp)

private data class WidgetTypeInfo(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val description: String
)

private val ALL_WIDGET_TYPES = listOf(
    WidgetTypeInfo("CLOCK", "CLOCK", Icons.Default.AccessTime, "Time & date"),
    WidgetTypeInfo("WEATHER", "WEATHER", Icons.Default.Cloud, "Current conditions"),
    WidgetTypeInfo("NOW_PLAYING", "NOW PLAYING", Icons.Default.MusicNote, "Media controls"),
    WidgetTypeInfo("TELEMETRY", "COMPASS", Icons.Default.Explore, "Speed & heading"),
    WidgetTypeInfo("ALTIMETER", "ALTIMETER", Icons.Default.FlightTakeoff, "Roll, pitch & altitude"),
    WidgetTypeInfo("SPEEDOMETER", "SPEED", Icons.Default.Speed, "GPS speed"),
    WidgetTypeInfo("VITALS", "VITALS", Icons.Default.Dns, "Head Unit Health / Vitals"),
    WidgetTypeInfo("TRIP_TRACKER", "TRIP TRACKER", Icons.Default.Map, "Trip logs & stats"),
    WidgetTypeInfo("SOUNDBOARD", "SOUNDBOARD", Icons.Default.Piano, "Custom sound pads")
)

private fun canAddWidget(settings: com.openlauncher.app.data.AppSettings): Boolean {
    val visibleIds = buildSet {
        if (settings.showClock) add("CLOCK")
        if (settings.showWeather) add("WEATHER")
        if (settings.showNowPlaying) add("NOW_PLAYING")
        if (settings.showTelemetry) add("TELEMETRY")
        if (settings.showAltimeter) add("ALTIMETER")
        if (settings.showSpeedometer) add("SPEEDOMETER")
        if (settings.showVitals) add("VITALS")
        if (settings.showTripTracker) add("TRIP_TRACKER")
        if (settings.showSoundboard) add("SOUNDBOARD")
    }
    val activeWidgets = settings.widgetLayout.filter { it.enabled && it.id in visibleIds }
    val occupied = buildSet<Pair<Int, Int>> {
        activeWidgets.forEach { w ->
            for (dx in 0 until w.spanX) for (dy in 0 until w.spanY) add(w.gridX + dx to w.gridY + dy)
        }
    }
    val hasFreeCell = (0 until com.openlauncher.app.data.GRID_ROWS).any { r ->
        (0 until com.openlauncher.app.data.GRID_COLS).any { c -> (c to r) !in occupied }
    }
    // Also true if any active widget spans >1 cell and can be shrunk to make room
    val hasShrinkable = activeWidgets.any { it.spanX * it.spanY > 1 }
    return hasFreeCell || hasShrinkable
}

private enum class StartupPhase {
    SEGMENT_TEST,
    FADE_OUT,
    LIVE
}

@Composable
private fun Aw11HomeLayout(
    settings: AppSettings,
    location: LocationData?,
    tripData: TripData,
    onResetTrip: () -> Unit,
    bearing: Float,
    isMetric: Boolean,
    nowPlaying: NowPlayingState?,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchOpenRequestId by remember {
        mutableIntStateOf(0)
    }
    var startupPhase by remember {
        mutableStateOf(StartupPhase.SEGMENT_TEST)
    }
    var demoDisplayLocation by remember {
        mutableStateOf<LocationData?>(null)
    }

    var demoDisplayTripData by remember {
        mutableStateOf<DemoTripData?>(null)
    }

    val displayLocation =
        demoDisplayLocation ?: location
    val selfTestProgress = remember {
        Animatable(0f)
    }

    LaunchedEffect(Unit) {
        startupPhase = StartupPhase.SEGMENT_TEST

        // Fill all display bars
        selfTestProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = LinearEasing
            )
        )

        // Keep all segments illuminated for a moment
        delay(300)

        // Empty all display bars
        selfTestProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 600,
                easing = LinearEasing
            )
        )

        startupPhase = StartupPhase.FADE_OUT
        delay(150)

        startupPhase = StartupPhase.LIVE
    }

    val showTestValues = startupPhase != StartupPhase.LIVE
    val isLive = startupPhase == StartupPhase.LIVE
    val speedDisplay =
        ((displayLocation?.speedMps ?: 0f) *
                if (isMetric) 3.6f else 2.237f)
            .coerceAtLeast(0f)

    val speedBarMax = if (isMetric) 150f else 93f
    val speedProgress = (speedDisplay / speedBarMax).coerceIn(0f, 1f)
    val displayedTripDistanceMeters =
        demoDisplayTripData?.distanceMeters
            ?: tripData.distanceMeters

    val displayedTripDriveTimeMs =
        demoDisplayTripData?.driveTimeMs
            ?: tripData.driveTimeMs

    val displayedTripAverageSpeedMps =
        demoDisplayTripData?.averageSpeedMps
            ?: tripData.averageSpeedMps

    val displayedTripMaxSpeedMps =
        demoDisplayTripData?.maxSpeedMps
            ?: tripData.maxSpeedMps
    val distanceDisplay =
        if (isMetric) {
            "%.1f KM".format(displayedTripDistanceMeters/ 1000.0)
        } else {
            "%.1f MI".format(displayedTripDistanceMeters / 1609.344)
        }

    val avgSpeed =
        displayedTripAverageSpeedMps *
                if (isMetric) 3.6f else 2.237f

    val maxSpeed =
        displayedTripMaxSpeedMps *
                if (isMetric) 3.6f else 2.237f

    val totalMinutes =
        displayedTripDriveTimeMs / 60_000L
    val driveTimeDisplay =
        "%02d:%02d".format(
            totalMinutes / 60,
            totalMinutes % 60
        )

    val displayedDistance =
        if (showTestValues) "88.8 KM" else distanceDisplay

    val displayedDriveTime =
        if (showTestValues) "88:88" else driveTimeDisplay

    val displayedAvgSpeed =
        if (showTestValues) "888" else "%03.0f".format(avgSpeed)

    val displayedMaxSpeed =
        if (showTestValues) "888" else "%03.0f".format(maxSpeed)

    val mediaServiceConnected by MediaListenerService.isConnected.collectAsState()
    var mediaLinkDots by remember {
        mutableIntStateOf(0)
    }
    LaunchedEffect(
        mediaServiceConnected,
        nowPlaying,
        showTestValues
    ) {
        if (
            !showTestValues &&
            !mediaServiceConnected &&
            nowPlaying == null
        ) {
            while (true) {
                mediaLinkDots = (mediaLinkDots + 1) % 4
                delay(350)
            }
        } else {
            mediaLinkDots = 0
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(3.dp)
    ) {
        // LEFT — NAV / MUSIC / CAR / APPS / SETTINGS
        Box(
            modifier = Modifier
                .weight(0.13f)
                .fillMaxHeight()
                .border(
                    1.dp,
                    Aw11Border.copy(
                        alpha = 0.45f
                    )
                )
        ) {
            Aw11ControlPanel(
                hasGps = location != null,
                onNav = {
                    searchOpenRequestId++
                },
                onApps = onOpenApps,
                onSettings = onOpenSettings
            )
        }

        Spacer(Modifier.width(3.dp))

        // CENTER — NAVIGATION / MAP
        Box(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .border(1.dp, Aw11Border.copy(alpha = 0.45f))
        ) {
            Aw11HereMap(
                location = location,
                settings = settings,
                openSearchRequestId =
                    searchOpenRequestId,
                onDemoDataChanged = {
                        demoLocation,
                        demoTripData ->

                    demoDisplayLocation =
                        demoLocation

                    demoDisplayTripData =
                        demoTripData
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
            )
        }

        Spacer(Modifier.width(3.dp))

        // RIGHT — SPEED / MUSIC / CAR DATA
        Column(
            modifier = Modifier
                .weight(0.32f)
                .fillMaxHeight()
        ) {
            // SPEED
            Box(
                modifier = Modifier
                    .weight(0.26f)
                    .fillMaxWidth()
                    .border(1.dp, Aw11Border.copy(alpha = 0.45f))
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

                    Spacer(Modifier.weight(1f))

                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (showTestValues) {
                                "888"
                            } else {
                                "%03.0f".format(speedDisplay)
                            },
                            color = Aw11Primary,
                            fontFamily = DSEG14Classic,
                            fontSize = 44.sp
                        )

                        Spacer(Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(42.dp)
                        ) {
                            Text(
                                text = "MPH",
                                color = if (!isMetric) {
                                    Aw11Primary
                                } else {
                                    Aw11Dim.copy(alpha = 0.45f)
                                },
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(y = (-2).dp)
                            )

                            Text(
                                text = "KM/H",
                                color = if (isMetric) {
                                    Aw11Primary
                                } else {
                                    Aw11Dim.copy(alpha = 0.45f)
                                },
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(y = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(6) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(4.dp)
                                    .background(Aw11Secondary.copy(alpha = 0.55f))
                            )
                        }
                    }

                    Spacer(Modifier.height(3.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(20) { index ->
                            val activeSegments =
                                if (showTestValues) {
                                    (selfTestProgress.value * 20f)
                                        .roundToInt()
                                        .coerceIn(0, 20)
                                } else {
                                    (speedProgress * 20f)
                                        .roundToInt()
                                        .coerceIn(0, 20)
                                }

                            val active = index < activeSegments

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        if (active) {
                                            Aw11Primary
                                        } else {
                                            Aw11Dim.copy(alpha = 0.35f)
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(3.dp))

            // MUSIC
            Box(
                modifier = Modifier
                    .weight(0.36f)
                    .fillMaxWidth()
                    .border(1.dp, Aw11Border.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = mediaSourceName(nowPlaying),
                        color = Aw11Primary,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(Modifier.weight(1f))

                    when {
                        showTestValues -> {
                            Aw11MediaSelfTest(
                                progress = selfTestProgress.value
                            )
                        }

                        nowPlaying != null -> {
                            val controller = nowPlaying.controller
                            val playbackState = controller?.playbackState
                            val canSkipPrevious =
                                playbackState != null &&
                                        (playbackState.actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L

                            val canSkipNext =
                                playbackState != null &&
                                        (playbackState.actions and PlaybackState.ACTION_SKIP_TO_NEXT) != 0L
                            val actions = playbackState?.actions ?: 0L

                            val canPlay =
                                (actions and PlaybackState.ACTION_PLAY) != 0L ||
                                        (actions and PlaybackState.ACTION_PLAY_PAUSE) != 0L

                            val canPause =
                                (actions and PlaybackState.ACTION_PAUSE) != 0L ||
                                        (actions and PlaybackState.ACTION_PLAY_PAUSE) != 0L

                            val canPlayPause =
                                if (nowPlaying.isPlaying) canPause else canPlay
                            val canSeek =
                                playbackState != null &&
                                        (playbackState.actions and PlaybackState.ACTION_SEEK_TO) != 0L
                            val progressColor =
                                if (canSeek) Aw11Primary
                                else Aw11Secondary
                            val durationMs =
                                controller?.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)
                                    ?: 0L

                            var currentPositionMs by remember(
                                nowPlaying.title,
                                nowPlaying.artist,
                                nowPlaying.isPlaying
                            ) {
                                mutableLongStateOf(playbackState?.position ?: 0L)
                            }

                            LaunchedEffect(
                                nowPlaying.title,
                                nowPlaying.artist,
                                nowPlaying.isPlaying
                            ) {
                                while (true) {
                                    val state = controller?.playbackState

                                    currentPositionMs = if (
                                        state != null &&
                                        state.state == PlaybackState.STATE_PLAYING
                                    ) {
                                        val elapsedSinceUpdate =
                                            SystemClock.elapsedRealtime() - state.lastPositionUpdateTime

                                        (
                                                state.position +
                                                        elapsedSinceUpdate * state.playbackSpeed
                                                ).toLong()
                                            .coerceAtLeast(0L)
                                            .coerceAtMost(durationMs)
                                    } else {
                                        state?.position ?: 0L
                                    }

                                    delay(500)
                                }
                            }
                            val progress = if (durationMs > 0L) {
                                (currentPositionMs.toFloat() / durationMs.toFloat())
                                    .coerceIn(0f, 1f)
                            } else {
                                0f
                            }

                            fun formatTime(ms: Long): String {
                                val totalSeconds = ms / 1000
                                val minutes = totalSeconds / 60
                                val seconds = totalSeconds % 60

                                return "%02d:%02d".format(minutes, seconds)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    //artist
                                    Text(
                                        text = nowPlaying.artist.uppercase(),
                                        color = Aw11Secondary,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.25.sp,
                                        maxLines = 1
                                    )

                                    //title
                                    Text(
                                        text = nowPlaying.title.uppercase(),
                                        color = Aw11Primary,
                                        fontSize = 15.sp,
                                        letterSpacing = 0.sp,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(1.dp))

                                    //controls
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        //previous
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable(enabled = canSkipPrevious) { onPrev() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "⏮",
                                                color = if (canSkipPrevious) Aw11Secondary else Aw11Dim,
                                                fontSize = 17.sp
                                            )
                                        }
                                        //play
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable(enabled = canPlayPause) { onPlayPause() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (nowPlaying.isPlaying) "Ⅱ" else "▶",
                                                color = if (canPlayPause) Aw11Primary else Aw11Dim,
                                                fontSize = 18.sp,
                                                modifier = Modifier.offset(y = 2.dp)
                                            )
                                        }
                                        //next
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clickable(enabled = canSkipNext) { onNext() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "⏭",
                                                color = if (canSkipNext) Aw11Secondary else Aw11Dim,
                                                fontSize = 17.sp
                                            )
                                        }
                                    }
                                }

                                val retroAlbumArt = remember(nowPlaying.albumArt) {
                                    nowPlaying.albumArt?.let { createRetroAlbumArt(it) }
                                }
                                if (retroAlbumArt != null) {
                                    Image(
                                        bitmap = retroAlbumArt.asImageBitmap(),
                                        contentDescription = "Album art",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .border(
                                                width = 1.dp,
                                                color = Aw11Border.copy(alpha = 0.45f)
                                            )
                                    )
                                }
                            }
                            Spacer(Modifier.height(1.dp))

                            //Whole progress bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                //elapsed time
                                Text(
                                    text = if (showTestValues) {
                                        "88:88"
                                    } else {
                                        formatTime(currentPositionMs)
                                    },
                                    color = Aw11Secondary,
                                    fontSize = 9.sp
                                )

                                Spacer(Modifier.width(8.dp))

                                //bar
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(12.dp)
                                        .pointerInput(durationMs, canSeek) {
                                            detectTapGestures { offset ->
                                                if (canSeek && durationMs > 0L && size.width > 0) {
                                                    val progress =
                                                        (offset.x / size.width.toFloat())
                                                            .coerceIn(0f, 1f)

                                                    val newPosition =
                                                        (durationMs * progress).toLong()

                                                    controller
                                                        ?.transportControls
                                                        ?.seekTo(newPosition)
                                                }
                                            }
                                        },
                                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(24) { index ->
                                        val activeSegments =
                                            if (showTestValues) {
                                                (selfTestProgress.value * 24f)
                                                    .roundToInt()
                                                    .coerceIn(0, 24)
                                            } else {
                                                (progress * 24f)
                                                    .roundToInt()
                                                    .coerceIn(0, 24)
                                            }

                                        val active = index < activeSegments

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(5.dp)
                                                .background(
                                                    if (active) {
                                                        if (showTestValues) {
                                                            Aw11Primary
                                                        } else {
                                                            progressColor
                                                        }
                                                    } else {
                                                        Aw11Dim.copy(alpha = 0.35f)
                                                    }
                                                )
                                        )
                                    }
                                }

                                Spacer(Modifier.width(8.dp))

                                //total time
                                Text(
                                    text = if (showTestValues) {
                                        "88:88"
                                    } else {
                                        formatTime(durationMs)
                                    },
                                    color = Aw11Secondary,
                                    fontSize = 9.sp
                                )
                            }
                            Spacer(Modifier.height(2.dp))

                            //equalizer
                            RetroEqualizer(
                                isPlaying = nowPlaying?.isPlaying == true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                            )


                        }

                        !mediaServiceConnected -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MEDIA LINK",
                                    color = Aw11Secondary,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )

                                Spacer(Modifier.width(2.dp))

                                Row(
                                    modifier = Modifier.width(18.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    repeat(3) { index ->
                                        Text(
                                            text = ".",
                                            color = if (index < mediaLinkDots) {
                                                Aw11Secondary
                                            } else {
                                                Color.Transparent
                                            },
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            Text(
                                text = "NO MEDIA",
                                color = Aw11Dim,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(3.dp))

            // CAR DATA
            Box(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxWidth()
                    .border(1.dp, Aw11Border.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRIP DATA",
                            color = Aw11Primary,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.weight(1f))

                        Text(
                            text = "RESET",
                            color = Aw11Secondary,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .clickable { onResetTrip() }
                                .padding(6.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Aw11DataCell(
                            label = "DISTANCE",
                            value = displayedDistance,
                            modifier = Modifier.weight(1f)
                        )

                        Aw11DataCell(
                            label = "DRIVE TIME",
                            value = displayedDriveTime,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(3.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Aw11DataCell(
                            label = "AVG SPEED",
                            value = displayedAvgSpeed,
                            modifier = Modifier.weight(1f)
                        )

                        Aw11DataCell(
                            label = "MAX SPEED",
                            value = displayedMaxSpeed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RetroEqualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(
        label = "retro_equalizer"
    )

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "equalizer_phase"
    )

    val levels = if (isPlaying) {
        listOf(
            2 + ((phase * 5).toInt() % 5),
            4 + ((phase * 7).toInt() % 3),
            3 + ((phase * 9).toInt() % 4),
            5 + ((phase * 6).toInt() % 2),
            2 + ((phase * 8).toInt() % 5),
            4 + ((phase * 11).toInt() % 3),
            3 + ((phase * 5).toInt() % 4),
            5 + ((phase * 9).toInt() % 2),
            3 + ((phase * 7).toInt() % 4),
            2 + ((phase * 10).toInt() % 5),
            4 + ((phase * 6).toInt() % 3),
            3 + ((phase * 8).toInt() % 4)
        )
    } else {
        List(12) { 1 }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        levels.forEach { level ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom
            ) {
                repeat(7) { segment ->
                    val active = segment < level

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 1.dp)
                            .background(
                                if (active) {
                                    Aw11Primary.copy(alpha = 0.85f)
                                } else {
                                    Aw11Dim.copy(alpha = 0.25f)
                                }
                            )
                    )
                }
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
            .border(1.dp, Aw11Border.copy(alpha = 0.45f))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            color = Aw11Secondary,
            fontSize = 8.sp,
            letterSpacing = 0.25.sp
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = value,
            color = Aw11Primary,
            fontFamily = DSEG14Classic,
            fontSize = 20.sp
        )
    }
}

private fun mediaSourceName(nowPlaying: NowPlayingState?): String {
    val packageName = nowPlaying?.controller?.packageName ?: return "MUSIC"

    return when (packageName) {
        "com.spotify.music" -> "SPOTIFY"
        "com.google.android.apps.youtube.music" -> "YOUTUBE MUSIC"
        else -> packageName
            .substringAfterLast('.')
            .replace('_', ' ')
            .uppercase()
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HomeScreen(
    settings: AppSettings,
    weather: WeatherState?,
    nowPlaying: NowPlayingState?,
    location: LocationData?,
    tripData: TripData,
    onResetTrip: () -> Unit,
    bearing: Float,
    isWifi: Boolean,
    isData: Boolean,
    isDayMode: Boolean = false,
    onOpenApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onLaunchCarPlay: () -> Unit,
    onLaunchAndroidAuto: () -> Unit,
    onAssignCarPlay: () -> Unit,
    onAssignAndroidAuto: () -> Unit,
    onClearCarPlay: () -> Unit,
    onClearAndroidAuto: () -> Unit,
    onAssignPip: () -> Unit,
    onClearPip: () -> Unit,
    onLaunchPip: () -> Unit,
    onTapNowPlaying: () -> Unit,
    onUpdateWidget: (id: String, spanX: Int, spanY: Int) -> Unit,
    onMoveWidget: (id: String, gridX: Int, gridY: Int) -> Unit,
    onAddWidget: (id: String) -> Unit,
    onRemoveWidget: (id: String) -> Unit,
    onSetClockStyle: (ClockStyle) -> Unit,
    onSetVitalsAsBars: (Boolean) -> Unit = {},
    onSetSpeedometerDigitalOnly: (Boolean) -> Unit = {},
    onUpdateSoundPad: (index: Int, pad: com.openlauncher.app.data.SoundPadConfig) -> Unit = { _, _ -> },
    hardwareRadio: com.openlauncher.app.viewmodel.LauncherViewModel.HardwareRadioState? = null,
    onLaunchHardwareRadio: () -> Unit = {},
    onStopHardwareRadio: () -> Unit = {},
    onRadioSeekUp: () -> Unit = {},
    onRadioSeekDown: () -> Unit = {},
    onRadioCycleFm: () -> Unit = {},
    onRadioSwitchAm: () -> Unit = {},
    onRadioTune: (band: String, freq: Float) -> Unit = { _, _ -> },
    onAssignRadio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accent = Color(settings.accentColor)
    val gap = 3.dp
    val hasWallpaper = settings.wallpaperUri.isNotEmpty()
    val widgetBg = when {
        isDayMode -> Color(0xFFFFFFFF)
        hasWallpaper -> Color(0xCC000000)
        else -> Color.Black.copy(alpha = 0.35f)
    }

    val widgetBorder = when {
        isDayMode -> Color(0xFFCCCCCC)
        hasWallpaper -> Color(0x22FFFFFF)
        else -> Aw11Border.copy(alpha = 0.45f)
    }
    val headerTextColor = if (isDayMode) Color(0xFF111111) else accent
    val statusIconColor = if (isDayMode) Color(0xFF444444) else Color(0xFF666666)
    val controlIconColor = if (isDayMode) Color(0xFF666666) else Color(0xFF444444)

    var resizingId by remember { mutableStateOf<String?>(null) }
    var contextMenuId by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var editMode by remember { mutableStateOf(false) }
    var widgetLibraryOpen by remember { mutableStateOf(false) }

    Aw11HomeLayout(
        settings = settings,
        location = location,
        tripData = tripData,
        onResetTrip = onResetTrip,
        bearing = bearing,
        isMetric = settings.unitSystem == UnitSystem.METRIC,
        nowPlaying = nowPlaying,
        onPrev = onPrev,
        onPlayPause = onPlayPause,
        onNext = onNext,
        onOpenApps = onOpenApps,
        onOpenSettings = onOpenSettings,
        modifier = modifier
    )
    return

    Column(modifier = modifier.fillMaxSize()) {

        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = settings.vehicleName.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = headerTextColor,
                letterSpacing = 3.sp,
                fontSize = 14.sp
            )
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(visible = isWifi, enter = fadeIn(), exit = fadeOut()) {
                Icon(
                    Icons.Default.Wifi,
                    "WiFi",
                    tint = statusIconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (isWifi) Spacer(Modifier.width(6.dp))
            AnimatedVisibility(visible = isData, enter = fadeIn(), exit = fadeOut()) {
                Icon(
                    Icons.Default.SignalCellularAlt,
                    "Data",
                    tint = statusIconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (isLandscape) {
                Spacer(Modifier.width(8.dp))
                if (editMode) {
                    IconButton(
                        onClick = { widgetLibraryOpen = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Widget library",
                            tint = controlIconColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                }
                IconButton(
                    onClick = { editMode = !editMode },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit widgets",
                        tint = if (editMode) accent else controlIconColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = if (isDayMode) Color(0xFFCCCCCC) else Color(0xFF141414))

        // ── Widget Grid ─────────────────────────────────────────────────────
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(gap)
        ) {
            val cellW = (maxWidth - gap * (GRID_COLS - 1)) / GRID_COLS
            val cellH = (maxHeight - gap * (GRID_ROWS - 1)) / GRID_ROWS
            val density = LocalDensity.current
            val cellStepXPx = with(density) { (cellW + gap).toPx() }
            val cellStepYPx = with(density) { (cellH + gap).toPx() }

            // WEATHER stays in the set even with no data: the commit path
            // (LauncherViewModel.moveWidgetConfig) computes against settings flags
            // only, so dropping it here would make the drop ghost and the committed
            // layout disagree. With no data the cell renders fully transparent.
            val visibleIds = buildSet {
                if (settings.showClock) add("CLOCK")
                if (settings.showWeather) add("WEATHER")
                if (settings.showNowPlaying) add("NOW_PLAYING")
                if (settings.showTelemetry) add("TELEMETRY")
                if (settings.showAltimeter) add("ALTIMETER")
                if (settings.showSpeedometer) add("SPEEDOMETER")
                if (settings.showVitals) add("VITALS")
                if (settings.showTripTracker) add("TRIP_TRACKER")
                if (settings.showSoundboard) add("SOUNDBOARD")
            }

            // Keep only visible widgets exactly as configured in settings, allowing explicit resizing to dictate layout
            val visible = settings.widgetLayout.filter { it.enabled && it.id in visibleIds }
            val rendered = visible

            // ── Drag state ───────────────────────────────────────────────────
            var draggingId by remember { mutableStateOf<String?>(null) }
            var dragOffsetPx by remember { mutableStateOf(Offset.Zero) }

            // Compute snap target for the widget being dragged (uses original spanX)
            val draggingOriginal =
                if (draggingId != null) visible.find { it.id == draggingId } else null
            val targetGridX = draggingOriginal?.let {
                (it.gridX + (dragOffsetPx.x / cellStepXPx).roundToInt()).coerceIn(
                    0,
                    GRID_COLS - it.spanX
                )
            }
            val targetGridY = draggingOriginal?.let {
                (it.gridY + (dragOffsetPx.y / cellStepYPx).roundToInt()).coerceIn(
                    0,
                    GRID_ROWS - it.spanY
                )
            }

            // Compute proposed layout (push preview) while dragging
            val proposedLayout =
                if (draggingOriginal != null && targetGridX != null && targetGridY != null)
                    computeWidgetMove(visible, draggingOriginal.id, targetGridX, targetGridY)
                else null

            // Drop ghost — rendered before widgets so it appears beneath them
            if (draggingOriginal != null && targetGridX != null && targetGridY != null) {
                val gX = (cellW + gap) * targetGridX
                val gY = (cellH + gap) * targetGridY
                val gW = cellW * draggingOriginal.spanX + gap * (draggingOriginal.spanX - 1)
                val gH = cellH * draggingOriginal.spanY + gap * (draggingOriginal.spanY - 1)
                Box(
                    modifier = Modifier
                        .absoluteOffset(x = gX, y = gY)
                        .size(gW, gH)
                        .background(accent.copy(alpha = 0.08f))
                        .border(1.dp, accent.copy(alpha = 0.5f), WIDGET_RADIUS)
                )
            }

            // Displacement ghosts — show where pushed widgets will land
            if (proposedLayout != null && draggingOriginal != null) {
                proposedLayout
                    .filter { it.id != draggingOriginal.id }
                    .forEach { proposed ->
                        val original = visible.find { it.id == proposed.id } ?: return@forEach
                        if (proposed.gridX != original.gridX || proposed.gridY != original.gridY) {
                            val dX = (cellW + gap) * proposed.gridX
                            val dY = (cellH + gap) * proposed.gridY
                            val dW = cellW * proposed.spanX + gap * (proposed.spanX - 1)
                            val dH = cellH * proposed.spanY + gap * (proposed.spanY - 1)
                            Box(
                                modifier = Modifier
                                    .absoluteOffset(x = dX, y = dY)
                                    .size(dW, dH)
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), WIDGET_RADIUS)
                            )
                        }
                    }
            }

            rendered.forEach { w ->
                val xOff = (cellW + gap) * w.gridX
                val yOff = (cellH + gap) * w.gridY
                val width = cellW * w.spanX + gap * (w.spanX - 1)
                val height = cellH * w.spanY + gap * (w.spanY - 1)

                val label = when (w.id) {
                    "CLOCK" -> clockTimeLabel(Calendar.getInstance())
                    "WEATHER" -> "WEATHER"
                    "NOW_PLAYING" -> "NOW PLAYING"
                    "TELEMETRY" -> "COMPASS"
                    "ALTIMETER" -> "ALTIMETER"
                    "SPEEDOMETER" -> "SPEED"
                    "TRIP_TRACKER" -> "TRIP"
                    "SOUNDBOARD" -> "SOUND"
                    else -> w.id
                }

                // Original (pre-auto-expand) spanX needed for drag boundary clamping
                val origSpanX = visible.find { it.id == w.id }?.spanX ?: 1
                val isDragging = draggingId == w.id
                // Weather with no data reserves its cell but draws nothing
                // (still visible in edit mode so it can be moved/removed)
                val isGhost = w.id == "WEATHER" && weather == null && !editMode
                val dragDpX = if (isDragging) with(density) { dragOffsetPx.x.toDp() } else 0.dp
                val dragDpY = if (isDragging) with(density) { dragOffsetPx.y.toDp() } else 0.dp

                @OptIn(ExperimentalFoundationApi::class)
                Box(
                    modifier = Modifier
                        .absoluteOffset(x = xOff + dragDpX, y = yOff + dragDpY)
                        .size(width, height)
                        .zIndex(if (isDragging) 1f else 0f)
                        .clip(WIDGET_RADIUS)
                        .background(if (isGhost) Color.Transparent else widgetBg)
                        .border(
                            width = if (editMode) 1.5.dp else 0.8.dp,
                            color = when {
                                editMode -> accent.copy(alpha = 0.45f)
                                isGhost -> Color.Transparent
                                else -> widgetBorder
                            },
                            shape = WIDGET_RADIUS
                        )
                        .combinedClickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { if (editMode) contextMenuId = w.id },
                            onLongClick = { if (!editMode) contextMenuId = w.id }
                        )
                        .then(
                            if (editMode) Modifier.pointerInput(editMode, w.id, w.gridX, w.gridY) {
                                var hasSignificantDrag = false
                                // Touch-slop gate: without it, sub-pixel jitter during a
                                // long-press counts as a drag and the context menu never opens
                                val slop = viewConfiguration.touchSlop
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { _ ->
                                        draggingId = w.id
                                        dragOffsetPx = Offset.Zero
                                        hasSignificantDrag = false
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetPx += dragAmount
                                        if (!hasSignificantDrag && dragOffsetPx.getDistance() > slop) {
                                            hasSignificantDrag = true
                                        }
                                    },
                                    onDragEnd = {
                                        if (hasSignificantDrag) {
                                            val newX =
                                                (w.gridX + (dragOffsetPx.x / cellStepXPx).roundToInt())
                                                    .coerceIn(0, GRID_COLS - origSpanX)
                                            val newY =
                                                (w.gridY + (dragOffsetPx.y / cellStepYPx).roundToInt())
                                                    .coerceIn(0, GRID_ROWS - w.spanY)
                                            onMoveWidget(w.id, newX, newY)
                                        } else {
                                            contextMenuId = w.id
                                        }
                                        draggingId = null
                                        dragOffsetPx = Offset.Zero
                                    },
                                    onDragCancel = {
                                        draggingId = null
                                        dragOffsetPx = Offset.Zero
                                    }
                                )
                            } else Modifier
                        )
                ) {
                    when (w.id) {
                        "CLOCK" -> ClockWidget(
                            style = settings.clockStyle,
                            accent = accent,
                            isDayMode = isDayMode,
                            modifier = Modifier.fillMaxSize()
                        )

                        "WEATHER" -> WeatherWidget(
                            state = weather,
                            accent = accent,
                            metric = settings.unitSystem.name == "METRIC",
                            isDayMode = isDayMode,
                            modifier = Modifier.fillMaxSize()
                        )

                        "NOW_PLAYING" -> NowPlayingWidget(
                            state = nowPlaying,
                            accent = accent,
                            carPlayPackage = settings.carPlayPackage,
                            androidAutoPackage = settings.androidAutoPackage,
                            onPlayPause = onPlayPause,
                            onNext = onNext,
                            onPrev = onPrev,
                            onLaunchCarPlay = onLaunchCarPlay,
                            onLaunchAndroidAuto = onLaunchAndroidAuto,
                            onTapToOpenApp = onTapNowPlaying,
                            modifier = Modifier.fillMaxSize(),
                            isEditing = editMode,
                            isDayMode = isDayMode,
                            hardwareRadio = hardwareRadio,
                            onLaunchHardwareRadio = onLaunchHardwareRadio,
                            onStopHardwareRadio = onStopHardwareRadio,
                            onRadioSeekUp = onRadioSeekUp,
                            onRadioSeekDown = onRadioSeekDown,
                            onRadioCycleFm = onRadioCycleFm,
                            onRadioSwitchAm = onRadioSwitchAm,
                            onRadioTune = onRadioTune,
                            onAssignRadio = onAssignRadio
                        )

                        "TELEMETRY" -> TelemetryWidget(
                            location = location,
                            bearing = (bearing + settings.compassOffset + 360f) % 360f,
                            accent = accent,
                            isDayMode = isDayMode,
                            modifier = Modifier.fillMaxSize()
                        )

                        "ALTIMETER" -> AltimeterWidget(
                            location = location,
                            isMetric = settings.unitSystem == com.openlauncher.app.data.UnitSystem.METRIC,
                            accent = accent,
                            isDayMode = isDayMode,
                            modifier = Modifier.fillMaxSize()
                        )

                        "SPEEDOMETER" -> SpeedometerWidget(
                            location = location,
                            isMetric = settings.unitSystem == com.openlauncher.app.data.UnitSystem.METRIC,
                            accent = accent,
                            isDayMode = isDayMode,
                            digitalOnly = settings.speedometerDigitalOnly,
                            modifier = Modifier.fillMaxSize()
                        )

                        "VITALS" -> VitalsWidget(
                            accent = accent,
                            isDayMode = isDayMode,
                            asBars = settings.vitalsAsBars,
                            modifier = Modifier.fillMaxSize()
                        )

                        "TRIP_TRACKER" -> TripTrackerWidget(
                            location = location,
                            isMetric = settings.unitSystem == com.openlauncher.app.data.UnitSystem.METRIC,
                            accent = accent,
                            isDayMode = isDayMode,
                            modifier = Modifier.fillMaxSize()
                        )

                        "SOUNDBOARD" -> SoundboardWidget(
                            pads = settings.soundboardPads,
                            accent = accent,
                            isDayMode = isDayMode,
                            isEditing = editMode,
                            onUpdatePad = onUpdateSoundPad,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Label — hide when album art fills the widget background
                    val labelColor = when {
                        isGhost -> Color.Transparent
                        w.id == "NOW_PLAYING" && nowPlaying?.albumArt != null && nowPlaying.title.isNotEmpty() -> Color.Transparent
                        isDayMode -> Color(0xFF999999)
                        else -> Color(0xFF3A3A3A)
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor,
                        letterSpacing = 2.sp,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 10.dp, top = 7.dp)
                    )
                }
            }
        }
    }

    // ── Widget context menu (long-press any cell) ────────────────────────────
    contextMenuId?.let { id ->
        WidgetContextMenu(
            widgetId = id,
            accent = accent,
            clockStyle = settings.clockStyle,
            vitalsAsBars = settings.vitalsAsBars,
            speedometerDigitalOnly = settings.speedometerDigitalOnly,
            carPlayPackage = settings.carPlayPackage,
            androidAutoPackage = settings.androidAutoPackage,
            pipAppPackage = settings.pipAppPackage,
            isDayMode = isDayMode,
            onResize = { contextMenuId = null; resizingId = id },
            onAssignCarPlay = { contextMenuId = null; onAssignCarPlay() },
            onAssignAndroidAuto = { contextMenuId = null; onAssignAndroidAuto() },
            onClearCarPlay = { contextMenuId = null; onClearCarPlay() },
            onClearAndroidAuto = { contextMenuId = null; onClearAndroidAuto() },
            onAssignPip = { contextMenuId = null; onAssignPip() },
            onClearPip = { contextMenuId = null; onClearPip() },
            onSetClockStyle = { onSetClockStyle(it) },
            onSetVitalsAsBars = { onSetVitalsAsBars(it) },
            onSetSpeedometerDigitalOnly = { onSetSpeedometerDigitalOnly(it) },
            onDismiss = { contextMenuId = null }
        )
    }

    // ── Resize dialog ────────────────────────────────────────────────────────
    resizingId?.let { id ->
        val config = settings.widgetLayout.find { it.id == id }
        if (config != null) {
            WidgetResizeDialog(
                config = config,
                accent = accent,
                isDayMode = isDayMode,
                onDismiss = { resizingId = null },
                onConfirm = { sx, sy ->
                    onUpdateWidget(id, sx, sy)
                    resizingId = null
                }
            )
        }
    }

    // ── Widget library ────────────────────────────────────────────────────────
    if (widgetLibraryOpen) {
        WidgetLibraryDialog(
            settings = settings,
            accent = accent,
            isDayMode = isDayMode,
            onAdd = { id -> onAddWidget(id) },
            onRemove = { id -> onRemoveWidget(id) },
            onDismiss = { widgetLibraryOpen = false }
        )
    }
}

@Composable
private fun WidgetContextMenu(
    widgetId: String,
    accent: Color,
    clockStyle: ClockStyle,
    vitalsAsBars: Boolean,
    speedometerDigitalOnly: Boolean,
    carPlayPackage: String = "",
    androidAutoPackage: String = "",
    pipAppPackage: String = "",
    isDayMode: Boolean,
    onResize: () -> Unit,
    onAssignCarPlay: () -> Unit,
    onAssignAndroidAuto: () -> Unit,
    onClearCarPlay: () -> Unit,
    onClearAndroidAuto: () -> Unit,
    onAssignPip: () -> Unit,
    onClearPip: () -> Unit,
    onSetClockStyle: (ClockStyle) -> Unit,
    onSetVitalsAsBars: (Boolean) -> Unit,
    onSetSpeedometerDigitalOnly: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val menuBg = if (isDayMode) Color(0xFFFFFFFF) else Color(0xFF111111)
    val menuBorder = if (isDayMode) Color(0xFFDDE1E5) else Color(0xFF1E1E1E)
    val menuDivider = if (isDayMode) Color(0xFFF1F3F5) else Color(0xFF1A1A1A)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(menuBg)
                .border(1.dp, menuBorder, RoundedCornerShape(4.dp))
                .padding(vertical = 4.dp)
                .width(200.dp)
        ) {
            val inactiveMenuTint = if (isDayMode) Color(0xFF777777) else Color(0xFF555555)
            ContextRow("RESIZE", Icons.Default.OpenWith, accent, onResize, isDayMode = isDayMode)
            if (widgetId == "CLOCK") {
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label = "DIGITAL",
                    icon = Icons.Default.Schedule,
                    tint = if (clockStyle == ClockStyle.DIGITAL) accent else inactiveMenuTint,
                    onClick = { onSetClockStyle(ClockStyle.DIGITAL); onDismiss() },
                    isDayMode = isDayMode
                )
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label = "ANALOG",
                    icon = Icons.Default.Watch,
                    tint = if (clockStyle == ClockStyle.ANALOG) accent else inactiveMenuTint,
                    onClick = { onSetClockStyle(ClockStyle.ANALOG); onDismiss() },
                    isDayMode = isDayMode
                )
            }
            if (widgetId == "VITALS") {
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label = "DIAL GAUGES",
                    icon = Icons.Default.Adjust,
                    tint = if (!vitalsAsBars) accent else inactiveMenuTint,
                    onClick = { onSetVitalsAsBars(false); onDismiss() },
                    isDayMode = isDayMode
                )
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label = "BARS VIEW",
                    icon = Icons.Default.FormatAlignLeft,
                    tint = if (vitalsAsBars) accent else inactiveMenuTint,
                    onClick = { onSetVitalsAsBars(true); onDismiss() },
                    isDayMode = isDayMode
                )
            }
            if (widgetId == "SPEEDOMETER") {
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label = "DIAL TRACK",
                    icon = Icons.Default.Speed,
                    tint = if (!speedometerDigitalOnly) accent else inactiveMenuTint,
                    onClick = { onSetSpeedometerDigitalOnly(false); onDismiss() },
                    isDayMode = isDayMode
                )
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    label = "DIGITAL ONLY",
                    icon = Icons.Default.Dialpad,
                    tint = if (speedometerDigitalOnly) accent else inactiveMenuTint,
                    onClick = { onSetSpeedometerDigitalOnly(true); onDismiss() },
                    isDayMode = isDayMode
                )
            }
            if (widgetId == "NOW_PLAYING") {
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    "ASSIGN CARPLAY APP",
                    Icons.Default.PhoneAndroid,
                    accent,
                    onAssignCarPlay,
                    isDayMode = isDayMode
                )
                if (carPlayPackage.isNotEmpty()) {
                    HorizontalDivider(color = menuDivider)
                    ContextRow(
                        "CLEAR CARPLAY APP",
                        Icons.Default.PhoneAndroid,
                        Color(0xFF884444),
                        onClearCarPlay,
                        isDayMode = isDayMode
                    )
                }
                HorizontalDivider(color = menuDivider)
                ContextRow(
                    "ASSIGN ANDROID AUTO APP",
                    Icons.Default.DirectionsCar,
                    accent,
                    onAssignAndroidAuto,
                    isDayMode = isDayMode
                )
                if (androidAutoPackage.isNotEmpty()) {
                    HorizontalDivider(color = menuDivider)
                    ContextRow(
                        "CLEAR ANDROID AUTO APP",
                        Icons.Default.DirectionsCar,
                        Color(0xFF884444),
                        onClearAndroidAuto,
                        isDayMode = isDayMode
                    )
                }
            }

        }
    }
}

@Composable
private fun ContextRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    isDayMode: Boolean = false
) {
    val finalTint = if (isDayMode) {
        if (tint == Color(0xFF884444)) {
            tint
        } else if (tint == Color(0xFF777777)) {
            Color(0xFF888888)
        } else {
            Color(0xFF111111)
        }
    } else {
        tint
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = finalTint, modifier = Modifier.size(16.dp))
        Text(label, color = finalTint, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun WidgetResizeDialog(
    config: WidgetConfig,
    accent: Color,
    isDayMode: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (spanX: Int, spanY: Int) -> Unit
) {
    var spanX by remember { mutableStateOf(config.spanX) }
    var spanY by remember { mutableStateOf(config.spanY) }

    val maxSpanX = GRID_COLS - config.gridX
    val maxSpanY = GRID_ROWS - config.gridY

    val dialogBg = if (isDayMode) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.background
    val dialogText = if (isDayMode) Color(0xFF111111) else MaterialTheme.colorScheme.onBackground
    val cancelColor =
        if (isDayMode) Color(0xFF6C757D) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = config.id.replace('_', ' '),
                color = dialogText,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                SpanRow(
                    label = "WIDTH",
                    value = spanX,
                    min = 1,
                    max = maxSpanX,
                    accent = accent,
                    isDayMode = isDayMode
                ) { spanX = it }
                SpanRow(
                    label = "HEIGHT",
                    value = spanY,
                    min = 1,
                    max = maxSpanY,
                    accent = accent,
                    isDayMode = isDayMode
                ) { spanY = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(spanX, spanY) }) {
                Text("APPLY", color = accent, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = cancelColor, fontSize = 11.sp, letterSpacing = 1.sp)
            }
        },
        containerColor = dialogBg,
        titleContentColor = dialogText,
        textContentColor = dialogText
    )
}

@Composable
private fun SpanRow(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    accent: Color,
    isDayMode: Boolean,
    onChange: (Int) -> Unit
) {
    val textColor = if (isDayMode) Color(0xFF111111) else MaterialTheme.colorScheme.onBackground
    val dimColor = if (isDayMode) Color(0xFF495057) else Color(0xFF666666)
    val disabledC = if (isDayMode) Color(0xFFCED4DA) else Color(0xFF333333)
    val inactiveBg = if (isDayMode) Color(0xFFE9ECEF) else Color(0xFF2A2A2A)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = dimColor,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.width(52.dp)
        )
        IconButton(
            onClick = { if (value > min) onChange(value - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Remove, null,
                tint = if (value > min) textColor else disabledC,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = "$value",
            color = textColor,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(24.dp)
        )
        IconButton(
            onClick = { if (value < max) onChange(value + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Add, null,
                tint = if (value < max) accent else disabledC,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(max) { i ->
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 10.dp)
                        .background(
                            if (i < value) accent.copy(alpha = 0.7f) else inactiveBg,
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

// ── Widget Library ────────────────────────────────────────────────────────────

@Composable
private fun WidgetLibraryDialog(
    settings: AppSettings,
    accent: Color,
    isDayMode: Boolean,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dialogBg = if (isDayMode) Color(0xFFEEEEEE) else Color(0xFF0C0C0C)
    val dialogBorder = if (isDayMode) Color(0xFFCCCCCC) else Color(0xFF1E1E1E)
    val titleColor = if (isDayMode) Color(0xFF495057) else Color(0xFF555555)
    val closeColor = if (isDayMode) Color(0xFF495057) else Color(0xFF444444)

    val activeIds = buildSet {
        if (settings.showClock) add("CLOCK")
        if (settings.showWeather) add("WEATHER")
        if (settings.showNowPlaying) add("NOW_PLAYING")
        if (settings.showTelemetry) add("TELEMETRY")
        if (settings.showAltimeter) add("ALTIMETER")
        if (settings.showSpeedometer) add("SPEEDOMETER")
        if (settings.showVitals) add("VITALS")
        if (settings.showTripTracker) add("TRIP_TRACKER")
        if (settings.showSoundboard) add("SOUNDBOARD")
    }
    val canAdd = canAddWidget(settings)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(dialogBg)
                .border(1.dp, dialogBorder, RoundedCornerShape(4.dp))
                .padding(16.dp)
                .widthIn(min = 320.dp, max = 520.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WIDGET LIBRARY",
                    color = titleColor,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = closeColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ALL_WIDGET_TYPES) { info ->
                    val isActive = info.id in activeIds
                    WidgetLibraryCard(
                        info = info,
                        isActive = isActive,
                        canAdd = canAdd,
                        accent = accent,
                        isDayMode = isDayMode,
                        onToggle = { if (isActive) onRemove(info.id) else onAdd(info.id) }
                    )
                }
            }

            if (!canAdd) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "ALL ${GRID_COLS * GRID_ROWS} CELLS OCCUPIED — REMOVE A WIDGET TO ADD MORE",
                    color = if (isDayMode) Color(0xFFE03131) else Color(0xFF3A3A3A),
                    fontSize = 8.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WidgetLibraryCard(
    info: WidgetTypeInfo,
    isActive: Boolean,
    canAdd: Boolean,
    accent: Color,
    isDayMode: Boolean,
    onToggle: () -> Unit
) {
    val enabled = isActive || canAdd
    val cardBorder =
        if (isActive) accent else if (isDayMode) Color(0xFFCCCCCC) else Color(0xFF1A1A1A)
    val cardBg =
        if (isActive) accent.copy(alpha = 0.15f) else if (isDayMode) Color(0xFFFFFFFF) else Color(
            0xFF0E0E0E
        )
    val iconTint = if (isActive) accent else if (isDayMode) Color(0xFF495057) else Color(0xFF333333)
    val labelColor =
        if (isActive) accent else if (isDayMode) Color(0xFF212529) else Color(0xFF3A3A3A)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(info.icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(5.dp))
        Text(
            text = info.label,
            color = labelColor,
            fontSize = 7.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 9.sp
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = when {
                isActive -> "ACTIVE"
                !canAdd -> "FULL"
                else -> "ADD"
            },
            color = when {
                isActive -> accent.copy(alpha = 0.75f)
                !canAdd -> if (isDayMode) Color(0xFFADB5BD) else Color(0xFF282828)
                else -> if (isDayMode) Color(0xFF495057) else Color(0xFF3A3A3A)
            },
            fontSize = 6.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Aw11MediaSelfTest(
    progress: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "████████████",
                    color = Aw11Secondary,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )

                Text(
                    text = "████████████████",
                    color = Aw11Primary,
                    fontSize = 15.sp,
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )

                Spacer(Modifier.height(1.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏮",
                            color = Aw11Primary,
                            fontSize = 17.sp
                        )
                    }

                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ⅱ",
                            color = Aw11Primary,
                            fontSize = 18.sp,
                            modifier = Modifier.offset(y = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏭",
                            color = Aw11Primary,
                            fontSize = 17.sp
                        )
                    }
                }
            }

            // Placeholder for album art during the display test
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(
                        width = 1.dp,
                        color = Aw11Primary
                    )
                    .background(
                        Aw11Dim.copy(alpha = 0.35f)
                    )
            )
        }

        Spacer(Modifier.height(1.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "88:88",
                color = Aw11Secondary,
                fontSize = 9.sp
            )

            Spacer(Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeSegments =
                    (progress * 24f)
                        .roundToInt()
                        .coerceIn(0, 24)

                repeat(24) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .background(
                                if (index < activeSegments) {
                                    Aw11Primary
                                } else {
                                    Aw11Dim.copy(alpha = 0.35f)
                                }
                            )
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = "88:88",
                color = Aw11Secondary,
                fontSize = 9.sp
            )
        }

        Spacer(Modifier.height(2.dp))

        // Equalizer self-test will be added next
        // Equalizer display self-test
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val rows = 7

            val activeRows =
                (progress * rows)
                    .roundToInt()
                    .coerceIn(0, rows)

            repeat(12) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    repeat(rows) { row ->
                        // Rows are drawn top-to-bottom, so activate them from the bottom
                        val active = row < activeRows

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(
                                    if (active) {
                                        Aw11Primary
                                    } else {
                                        Aw11Dim.copy(alpha = 0.35f)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}