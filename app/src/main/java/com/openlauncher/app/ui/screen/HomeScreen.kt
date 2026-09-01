package com.openlauncher.app.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.openlauncher.app.data.AppSettings
import com.openlauncher.app.data.ClockStyle
import com.openlauncher.app.data.UnitSystem
import com.openlauncher.app.model.NowPlayingState
import com.openlauncher.app.model.WeatherState
import com.openlauncher.app.ui.screen.aw11.Aw11HomeLayout
import com.openlauncher.app.util.LocationData
import com.openlauncher.app.viewmodel.TripData

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
    onUpdateWidget: (
        id: String,
        spanX: Int,
        spanY: Int
    ) -> Unit,
    onMoveWidget: (
        id: String,
        gridX: Int,
        gridY: Int
    ) -> Unit,
    onAddWidget: (id: String) -> Unit,
    onRemoveWidget: (id: String) -> Unit,
    onSetClockStyle: (ClockStyle) -> Unit,
    onSetVitalsAsBars: (Boolean) -> Unit = {},
    onSetSpeedometerDigitalOnly: (Boolean) -> Unit = {},
    onUpdateSoundPad: (
        index: Int,
        pad: com.openlauncher.app.data.SoundPadConfig
    ) -> Unit = { _, _ -> },
    hardwareRadio:
    com.openlauncher.app.viewmodel.LauncherViewModel.HardwareRadioState? =
        null,
    onLaunchHardwareRadio: () -> Unit = {},
    onStopHardwareRadio: () -> Unit = {},
    onRadioSeekUp: () -> Unit = {},
    onRadioSeekDown: () -> Unit = {},
    onRadioCycleFm: () -> Unit = {},
    onRadioSwitchAm: () -> Unit = {},
    onRadioTune: (
        band: String,
        freq: Float
    ) -> Unit = { _, _ -> },
    onAssignRadio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Aw11HomeLayout(
        settings = settings,
        location = location,
        tripData = tripData,
        onResetTrip = onResetTrip,
        bearing = bearing,
        isMetric =
            settings.unitSystem ==
                    UnitSystem.METRIC,
        nowPlaying = nowPlaying,
        onPrev = onPrev,
        onPlayPause = onPlayPause,
        onNext = onNext,
        onOpenApps = onOpenApps,
        onOpenSettings = onOpenSettings,
        modifier = modifier
    )
}