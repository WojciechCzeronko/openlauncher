package com.openlauncher.app.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.openlauncher.app.data.AppSettings
import com.openlauncher.app.data.UnitSystem
import com.openlauncher.app.model.NowPlayingState
import com.openlauncher.app.ui.screen.aw11.Aw11HomeLayout
import com.openlauncher.app.util.LocationData
import com.openlauncher.app.viewmodel.TripData

@Composable
fun HomeScreen(
    settings: AppSettings,
    nowPlaying: NowPlayingState?,
    location: LocationData?,
    tripData: TripData,
    onResetTrip: () -> Unit,
    onOpenMedia: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    modifier: Modifier = Modifier
) {
    Aw11HomeLayout(
        settings = settings,
        location = location,
        tripData = tripData,
        onResetTrip = onResetTrip,
        isMetric =
            settings.unitSystem ==
                    UnitSystem.METRIC,
        nowPlaying = nowPlaying,
        onPrev = onPrev,
        onPlayPause = onPlayPause,
        onNext = onNext,
        onOpenMedia = onOpenMedia,
        onOpenApps = onOpenApps,
        onOpenSettings = onOpenSettings,
        modifier = modifier
    )
}