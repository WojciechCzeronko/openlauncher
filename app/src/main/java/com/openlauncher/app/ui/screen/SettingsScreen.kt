package com.openlauncher.app.ui.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.data.AppSettings
import com.openlauncher.app.data.UnitSystem
import com.openlauncher.app.ui.components.ConfirmDialog
import com.openlauncher.app.ui.theme.Aw11Border
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import com.openlauncher.app.ui.theme.JetBrainsMono
import com.openlauncher.app.ui.theme.LocalDayMode
import kotlin.math.roundToInt


// Resolved at call site via LocalDayMode — see SettingsDivider / SettingsSection

@Composable
fun SettingsScreen(
    settings: AppSettings,
    accent: Color,
    onUpdate: (AppSettings.() -> AppSettings) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    val isDayMode = LocalDayMode.current
    val screenBg = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = 24.dp,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Title ────────────────────────────────────────────────────────────
            item(
                key = "settings_header"
            ) {
                Column {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.titleLarge,
                        color =
                            if (isDayMode) {
                                Color(0xFF111111)
                            } else {
                                accent
                            },
                        letterSpacing = 3.sp,
                        fontSize = 14.sp
                    )

                    Spacer(
                        Modifier.height(4.dp)
                    )
                }
            }

            // ── Permissions ──────────────────────────────────────────────────────
            item(
                key = "permissions"
            ) {
                SettingsSection("Permissions") {
                    val isMediaConnected by com.openlauncher.app.service.MediaListenerService.isConnected.collectAsState()

                    // Bumped on ON_RESUME so statuses refresh when the user returns from
                    // system settings (recomposition alone doesn't re-run these checks)
                    var permissionRefresh by remember { mutableIntStateOf(0) }
                    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) permissionRefresh++
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                    }

                    // canDrawOverlays requires API 23 — on Android 5.x the permission
                    // model doesn't exist, so treat it as granted
                    val canDrawOverlays = remember(permissionRefresh) {
                        android.os.Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context)
                    }
                    val hasLocation = remember(permissionRefresh) {
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    }
                    val isDefaultLauncher = remember(permissionRefresh) {
                        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                        context.packageManager.resolveActivity(
                            home, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                        )?.activityInfo?.packageName == context.packageName
                    }

                    val homeRoleLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { permissionRefresh++ }

                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissionRefresh++ }

                    SettingsButton(
                        label = "Set as Default Launcher",
                        sublabel = if (isDefaultLauncher) "Active — RetroLauncher is the home app"
                        else "Required so the head unit boots into RetroLauncher",
                        icon = Icons.Default.Home,
                        accent = if (isDefaultLauncher) accent else Color(0xFF993333),
                        onClick = {
                            // Preferred: the system home-role dialog (API 29+). Vendor ROMs
                            // sometimes ship without it, so fall through to the home-settings
                            // screen, then the default-apps screen.
                            var launched = false
                            if (android.os.Build.VERSION.SDK_INT >= 29) {
                                val rm =
                                    context.getSystemService(android.app.role.RoleManager::class.java)
                                if (rm != null && rm.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) &&
                                    !rm.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)
                                ) {
                                    launched = runCatching {
                                        homeRoleLauncher.launch(rm.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME))
                                    }.isSuccess
                                }
                            }
                            if (!launched) {
                                launched = runCatching {
                                    context.startActivity(
                                        Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }.isSuccess
                            }
                            if (!launched) {
                                runCatching {
                                    context.startActivity(
                                        Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS")
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                        }
                    )
                    SettingsDivider()
                    SettingsButton(
                        label = "Notification Access",
                        sublabel = if (isMediaConnected) "Granted — media controls active" else "Required for Now Playing widget",
                        icon = if (isMediaConnected) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        accent = if (isMediaConnected) accent else Color(0xFF993333),
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    )
                    SettingsDivider()
                    SettingsButton(
                        label = "Draw Over Other Apps",
                        sublabel = if (canDrawOverlays) "Granted — PIP overlay enabled" else "Required for PIP floating window",
                        icon = if (canDrawOverlays) Icons.Default.Layers else Icons.Default.LayersClear,
                        accent = if (canDrawOverlays) accent else Color(0xFF993333),
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= 23) {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                        }
                    )
                    SettingsDivider()
                    SettingsButton(
                        label = "Location Access",
                        sublabel = if (hasLocation) "Granted — GPS, compass & weather active" else "Required for compass, speed & weather",
                        icon = if (hasLocation) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        accent = if (hasLocation) accent else Color(0xFF993333),
                        onClick = {
                            if (!hasLocation) {
                                // Ask in-app first — previously the only grant path was the
                                // onboarding flow; skipping it left GPS features dead forever
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.parse("package:${context.packageName}")
                                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // ── Vehicle Name ─────────────────────────────────────────────────────
            item(key = "vehicle") {
                SettingsSection("Vehicle") {
                    var nameInput by remember(settings.vehicleName) { mutableStateOf(settings.vehicleName) }
                    SettingsRow(
                        label = "Vehicle Name",
                        sublabel = "",
                        icon = Icons.Default.DirectionsCar
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Aw11TextField(
                                value = nameInput,
                                onValueChange = {
                                    nameInput = it
                                },
                                placeholder = "MY CAR",
                                modifier = Modifier.width(140.dp)
                            )

                            if (nameInput != settings.vehicleName) {
                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Aw11Primary
                                        )
                                        .background(
                                            Aw11Primary.copy(
                                                alpha = 0.16f
                                            )
                                        )
                                        .clickable {
                                            onUpdate {
                                                copy(
                                                    vehicleName = nameInput
                                                )
                                            }
                                        }
                                        .padding(
                                            horizontal = 10.dp
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "OK",
                                        color = Aw11Primary,
                                        fontFamily = JetBrainsMono,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }
                        }
                    }

                    SettingsDivider()

                    SettingsRow(
                        label = "Unit System",
                        sublabel = if (settings.unitSystem == UnitSystem.METRIC) "Metric (°C, km)" else "Imperial (°F, mi)",
                        icon = Icons.Default.Straighten
                    ) {
                        Row {
                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(6.dp)
                            ) {
                                Aw11OptionButton(
                                    text = "Metric",
                                    selected =
                                        settings.unitSystem ==
                                                UnitSystem.METRIC,
                                    onClick = {
                                        onUpdate {
                                            copy(
                                                unitSystem =
                                                    UnitSystem.METRIC
                                            )
                                        }
                                    }
                                )

                                Aw11OptionButton(
                                    text = "Imperial",
                                    selected =
                                        settings.unitSystem ==
                                                UnitSystem.IMPERIAL,
                                    onClick = {
                                        onUpdate {
                                            copy(
                                                unitSystem =
                                                    UnitSystem.IMPERIAL
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    SettingsDivider()

                    Column(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    ) {
                        SettingsRow(
                            label = "Compass Heading Offset",
                            sublabel =
                                "Manual Alignment: ${
                                    if (settings.compassOffset >= 0) "+" else ""
                                }${settings.compassOffset.toInt()}°",
                            icon = Icons.Default.Explore
                        ) {}

                        Aw11Slider(
                            value = settings.compassOffset,
                            onValueChange = {
                                onUpdate {
                                    copy(
                                        compassOffset = it
                                    )
                                }
                            },
                            valueRange = -180f..180f,
                            steps = 71,
                            visualTickCount = 13,
                            highlightCenter = true,
                            centeredFill = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            }
            // Navigation settings
            item(key = "navigation") {
                SettingsSection("Navigation") {

                    SettingsRow(
                        label = "Auto Reroute",
                        sublabel = if (settings.autoReroute) {
                            "Automatically recalculate route when off route"
                        } else {
                            "Disabled"
                        },
                        icon = Icons.Default.Navigation
                    ) {
                        Aw11Toggle(
                            checked = settings.autoReroute,
                            onCheckedChange = {
                                onUpdate {
                                    copy(
                                        autoReroute = it
                                    )
                                }
                            }
                        )
                    }


                }
            }

            // ── Advanced ───────────────────────────────────────────────────────

            item(key = "advanced") {
                var calibrationStatus by remember { mutableStateOf<String?>(null) }

                SettingsSection("Advanced") {
                    SettingsRow(
                        label = "Traffic Refresh",
                        sublabel = "${settings.routeRefreshIntervalSeconds / 60} min",
                        icon = Icons.Default.Update
                    ) {
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(60, 120, 180, 300)
                                .forEach { seconds ->

                                    Aw11OptionButton(
                                        text = "${seconds / 60}m",
                                        selected =
                                            settings.routeRefreshIntervalSeconds ==
                                                    seconds,
                                        onClick = {
                                            onUpdate {
                                                copy(
                                                    routeRefreshIntervalSeconds =
                                                        seconds
                                                )
                                            }
                                        }
                                    )
                                }
                        }
                    }

                    SettingsDivider()

                    SettingsRow(
                        label = "Off Route Distance",
                        sublabel = "${settings.offRouteThresholdMeters} m",
                        icon = Icons.Default.Route
                    ) {
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(25, 50, 75, 100)
                                .forEach { meters ->

                                    Aw11OptionButton(
                                        text = "${meters}m",
                                        selected =
                                            settings.offRouteThresholdMeters ==
                                                    meters,
                                        onClick = {
                                            onUpdate {
                                                copy(
                                                    offRouteThresholdMeters =
                                                        meters
                                                )
                                            }
                                        }
                                    )
                                }
                        }
                    }

                    SettingsDivider()

                    SettingsRow(
                        label = "Reroute Delay",
                        sublabel = "${settings.rerouteDelaySeconds} sec",
                        icon = Icons.Default.Timer
                    ) {
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(2, 3, 5, 10)
                                .forEach { seconds ->

                                    Aw11OptionButton(
                                        text = "${seconds}s",
                                        selected =
                                            settings.rerouteDelaySeconds ==
                                                    seconds,
                                        onClick = {
                                            onUpdate {
                                                copy(
                                                    rerouteDelaySeconds =
                                                        seconds
                                                )
                                            }
                                        }
                                    )
                                }
                        }
                    }

                    SettingsDivider()

                    SettingsRow(
                        label = "Minimum Route Gain",
                        sublabel =
                            "${settings.minimumRouteGainSeconds} sec",
                        icon = Icons.Default.AccessTime
                    ) {
                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(30, 60, 120, 300)
                                .forEach { seconds ->

                                    Aw11OptionButton(
                                        text =
                                            when (seconds) {
                                                30 -> "30s"
                                                60 -> "1m"
                                                120 -> "2m"
                                                else -> "5m"
                                            },
                                        selected =
                                            settings.minimumRouteGainSeconds ==
                                                    seconds,
                                        onClick = {
                                            onUpdate {
                                                copy(
                                                    minimumRouteGainSeconds =
                                                        seconds
                                                )
                                            }
                                        }
                                    )
                                }
                        }
                    }
                    SettingsDivider()

                    Column {
                        SettingsRow(
                            label = "Text Scale",
                            sublabel =
                                "${"%.0f".format(settings.textScale * 100)}%",
                            icon = Icons.Default.TextFields
                        ) {}

                        Aw11Slider(
                            value = settings.textScale,
                            onValueChange = {
                                onUpdate {
                                    copy(
                                        textScale = it
                                    )
                                }
                            },
                            valueRange = 0.8f..1.4f,
                            steps = 5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp
                                )
                        )
                    }
                    SettingsDivider()

                    // 1. Reset A-GPS Button
                    SettingsButton(
                        label = "Reset A-GPS Assistance Data",
                        sublabel = calibrationStatus
                            ?: "Forces cold start to download fresh satellite orbits entirely offline",
                        icon = Icons.Default.MyLocation,
                        accent = accent,
                        onClick = {
                            calibrationStatus = "Clearing A-GPS cache..."
                            val lm =
                                context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                            var success = false
                            try {
                                // "delete_aiding_data" is the command AOSP's GPS provider
                                // actually recognizes (requires ACCESS_LOCATION_EXTRA_COMMANDS)
                                success = lm.sendExtraCommand(
                                    android.location.LocationManager.GPS_PROVIDER,
                                    "delete_aiding_data",
                                    android.os.Bundle()
                                )
                                lm.sendExtraCommand(
                                    android.location.LocationManager.GPS_PROVIDER,
                                    "force_xtra_injection",
                                    null
                                )
                                lm.sendExtraCommand(
                                    android.location.LocationManager.GPS_PROVIDER,
                                    "force_time_injection",
                                    null
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            calibrationStatus = if (success) {
                                "Cold start forced — go outdoors for a fresh satellite lock (2–3 min)"
                            } else {
                                "Not supported by this device's GPS driver — no data was cleared"
                            }
                        }
                    )
                }
            }
            // ── Updates ──────────────────────────────────────────────────────────
            item(key = "updates") {
                SettingsSection("Updates") {
                    SettingsButton(
                        label = "Check for Updates",
                        sublabel = "View releases on GitHub",
                        icon = Icons.Default.SystemUpdate,
                        accent = accent,
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://github.com/WojciechCzeronko/retrolauncher/releases"
                                )
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }
            // ── Maintenance ──────────────────────────────────────────────────────
            item(key = "maintenance") {
                SettingsSection("Maintenance") {
                    Spacer(Modifier.height(8.dp))
                    Aw11DangerButton(
                        text = "Reset to Defaults",
                        onClick = {
                            showResetDialog = true
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(32.dp))
            }
            item(
                key = "footer"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Spacer(
                        Modifier.height(32.dp)
                    )

                    Text(
                        text = "v0.0.5  ·  Made by David Lam  ·  2026",
                        color =
                            if (isDayMode) {
                                Color(0xFFAAAAAA)
                            } else {
                                Color(0xFF2A2A2A)
                            },
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .align(
                                Alignment.CenterHorizontally
                            )
                            .padding(
                                bottom = 16.dp
                            )
                    )
                }
            }
        }
    } // end Box

// ── Dialogs ──────────────────────────────────────────────────────────────
    if (showResetDialog) {
        ConfirmDialog(
            title = "Reset Settings",
            message = "Are you sure you want to reset all settings to default? This cannot be undone.",
            confirmLabel = "Reset",
            onConfirm = { onReset(); showResetDialog = false },
            onDismiss = { showResetDialog = false }
        )
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Aw11Border.copy(
                    alpha = 0.65f
                )
            )
    ) {
        Text(
            text = title.uppercase(),
            color = Aw11Primary,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Aw11Secondary.copy(
                        alpha = 0.10f
                    )
                )
                .padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                )
        )

        HorizontalDivider(
            color = Aw11Border.copy(
                alpha = 0.65f
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun SettingsRow(
    label: String,
    sublabel: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 10.dp,
                vertical = 9.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Aw11Secondary,
            modifier = Modifier.size(16.dp)
        )

        Spacer(
            Modifier.width(10.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = Aw11Primary,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )

            if (sublabel.isNotEmpty()) {
                Text(
                    text = sublabel,
                    color = Aw11Secondary,
                    fontFamily = JetBrainsMono,
                    fontSize = 9.sp,
                    lineHeight = 11.sp
                )
            }
        }

        content()
    }
}

@Composable
private fun ColumnScope.SettingsButton(
    label: String,
    sublabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 10.dp,
                vertical = 9.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Aw11Secondary,
            modifier = Modifier.size(16.dp)
        )

        Spacer(
            Modifier.width(10.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label.uppercase(),
                color = accent,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )

            if (sublabel.isNotEmpty()) {
                Text(
                    text = sublabel,
                    color = Aw11Secondary,
                    fontFamily = JetBrainsMono,
                    fontSize = 9.sp
                )
            }
        }

        Text(
            text = ">",
            color = Aw11Secondary,
            fontFamily = JetBrainsMono,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ColumnScope.SettingsDivider() {
    HorizontalDivider(
        color = Aw11Border.copy(
            alpha = 0.35f
        )
    )
}

@Composable
private fun Aw11OptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
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
            .background(
                if (selected) {
                    Aw11Primary.copy(
                        alpha = 0.16f
                    )
                } else {
                    Color.Transparent
                }
            )
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 12.dp
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color =
                if (selected) {
                    Aw11Primary
                } else {
                    Aw11Secondary
                },
            fontFamily = JetBrainsMono,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun Aw11Toggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(76.dp)
            .height(32.dp)
            .border(
                width = 1.dp,
                color = Aw11Border.copy(
                    alpha = 0.65f
                )
            )
            .clickable {
                onCheckedChange(!checked)
            }
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (!checked) {
                        Aw11Primary.copy(
                            alpha = 0.16f
                        )
                    } else {
                        Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "OFF",
                color =
                    if (!checked) {
                        Aw11Primary
                    } else {
                        Aw11Secondary.copy(
                            alpha = 0.55f
                        )
                    },
                fontFamily = JetBrainsMono,
                fontSize = 8.sp,
                letterSpacing = 0.6.sp
            )
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(
                    Aw11Border.copy(
                        alpha = 0.65f
                    )
                )
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (checked) {
                        Aw11Primary.copy(
                            alpha = 0.16f
                        )
                    } else {
                        Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ON",
                color =
                    if (checked) {
                        Aw11Primary
                    } else {
                        Aw11Secondary.copy(
                            alpha = 0.55f
                        )
                    },
                fontFamily = JetBrainsMono,
                fontSize = 8.sp,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun Aw11Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier,
    visualTickCount: Int? = null,
    highlightCenter: Boolean = false,
    centeredFill: Boolean = false
) {
    var widthPx by remember {
        mutableFloatStateOf(1f)
    }

    fun valueForX(x: Float): Float {
        val fraction =
            (x / widthPx)
                .coerceIn(0f, 1f)

        val rawValue =
            valueRange.start +
                    fraction *
                    (valueRange.endInclusive -
                            valueRange.start)

        if (steps <= 0) {
            return rawValue
        }

        val intervals = steps + 1
        val stepSize =
            (valueRange.endInclusive -
                    valueRange.start) /
                    intervals

        val step =
            ((rawValue - valueRange.start) /
                    stepSize)
                .roundToInt()

        return (
                valueRange.start +
                        step * stepSize
                ).coerceIn(
                valueRange.start,
                valueRange.endInclusive
            )
    }

    val fraction =
        ((value - valueRange.start) /
                (valueRange.endInclusive -
                        valueRange.start))
            .coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .onSizeChanged {
                widthPx =
                    it.width.toFloat()
            }
            .pointerInput(
                valueRange,
                steps
            ) {
                detectTapGestures { offset ->
                    onValueChange(
                        valueForX(offset.x)
                    )
                }
            }
            .pointerInput(
                valueRange,
                steps
            ) {
                detectHorizontalDragGestures { change,
                                               _ ->

                    onValueChange(
                        valueForX(
                            change.position.x
                        )
                    )
                }
            }
    ) {
        val trackHeight =
            4.dp.toPx()

        val centerY =
            size.height / 2f

        val activeWidth =
            size.width * fraction

        // inactive track
        drawRect(
            color =
                Aw11Border.copy(
                    alpha = 0.45f
                ),
            topLeft = Offset(
                0f,
                centerY -
                        trackHeight / 2f
            ),
            size = Size(
                size.width,
                trackHeight
            )
        )

        // active track
        val valueX =
            size.width * fraction

        val fillStartX =
            if (centeredFill) {
                minOf(
                    size.width / 2f,
                    valueX
                )
            } else {
                0f
            }

        val fillEndX =
            if (centeredFill) {
                maxOf(
                    size.width / 2f,
                    valueX
                )
            } else {
                valueX
            }

        drawRect(
            color = Aw11Primary.copy(
                alpha = 0.75f
            ),
            topLeft = Offset(
                fillStartX,
                centerY - trackHeight / 2f
            ),
            size = Size(
                fillEndX - fillStartX,
                trackHeight
            )
        )

        // discrete tick marks
        if (steps > 0) {
            val tickCount =
                visualTickCount ?: (steps + 2)

            val intervals =
                tickCount - 1

            for (i in 0 until tickCount) {
                val x =
                    size.width *
                            i / intervals

                val isCenter =
                    highlightCenter &&
                            i == intervals / 2

                val tickHeight =
                    if (isCenter) {
                        14.dp.toPx()
                    } else {
                        8.dp.toPx()
                    }

                val tickWidth =
                    if (isCenter) {
                        3.dp.toPx()
                    } else {
                        2.dp.toPx()
                    }

                drawRect(
                    color =
                        if (isCenter) {
                            Aw11Primary
                        } else if (x <= activeWidth) {
                            Aw11Primary
                        } else {
                            Aw11Secondary.copy(
                                alpha = 0.55f
                            )
                        },
                    topLeft = Offset(
                        x - tickWidth / 2f,
                        centerY - tickHeight / 2f
                    ),
                    size = Size(
                        tickWidth,
                        tickHeight
                    )
                )
            }
        }

        // vertical position marker
        val markerX =
            activeWidth.coerceIn(
                2.dp.toPx(),
                size.width -
                        2.dp.toPx()
            )

        drawRect(
            color = Aw11Primary,
            topLeft = Offset(
                markerX -
                        2.dp.toPx(),
                centerY -
                        15.dp.toPx()
            ),
            size = Size(
                4.dp.toPx(),
                30.dp.toPx()
            )
        )
    }
}

@Composable
private fun Aw11TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        cursorBrush = SolidColor(Aw11Primary),
        textStyle = LocalTextStyle.current.copy(
            color = Aw11Primary,
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            letterSpacing = 0.8.sp
        ),
        modifier = modifier
            .height(34.dp)
            .border(
                width = 1.dp,
                color = Aw11Border.copy(
                    alpha = 0.65f
                )
            )
            .padding(
                horizontal = 10.dp
            ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Aw11Secondary.copy(
                            alpha = 0.55f
                        ),
                        fontFamily = JetBrainsMono,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                innerTextField()
            }
        }
    )
}

@Composable
private fun Aw11DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .border(
                width = 1.dp,
                color = Color(0xFF993333)
            )
            .background(
                Color(0xFF993333).copy(
                    alpha = 0.08f
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = Color(0xFFCC6666),
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp
        )
    }
}