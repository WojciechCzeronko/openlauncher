package com.openlauncher.app.ui.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.openlauncher.app.data.AppSettings
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Border
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import com.openlauncher.app.ui.theme.JetBrainsMono

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    accent: Color,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var currentStep by rememberSaveable { mutableStateOf(0) }
    var locationGranted by remember { mutableStateOf(false) }
    var mediaGranted by remember { mutableStateOf(false) }

    val checkPermissions = {
        locationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val enabledListeners = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        )
        mediaGranted = enabledListeners != null && enabledListeners.contains(context.packageName)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Trigger initial check
    LaunchedEffect(Unit) {
        checkPermissions()
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationGranted = granted
        if (granted) {
            currentStep = 2 // Auto-advance to next step
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Aw11Background)
    ) {

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Left branding pane ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.36f)
                    .fillMaxHeight()
                    .background(
                        Aw11Secondary.copy(alpha = 0.05f)
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Aw11Secondary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "RETROLAUNCHER",
                        color = Aw11Primary,
                        fontFamily = JetBrainsMono,
                        fontSize = 15.sp,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "AUTOMOTIVE DISPLAY SYSTEM",
                        color = Aw11Secondary,
                        fontFamily = JetBrainsMono,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Step indicator list
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StepItem(0, "Introduction", currentStep)
                    StepItem(1, "Location Services", currentStep)
                    StepItem(2, "Media Integration", currentStep)
                    StepItem(3, "Ready to Go", currentStep)
                }

                Text(
                    text = "v0.0.5",
                    color = Color(0xFF333333),
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }

            // Vertical separator line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(
                        Aw11Border.copy(alpha = 0.65f)
                    )
            )

            // ── Right content wizard ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.64f)
                    .padding(
                        horizontal = 36.dp,
                        vertical = 28.dp
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Active Step Content
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            fadeIn() + slideInHorizontally { it / 5 } togetherWith
                            fadeOut() + slideOutHorizontally { -it / 5 }
                        },
                        label = "step_transition"
                    ) { step ->
                        when (step) {
                            0 -> IntroStep(accent)
                            1 -> LocationStep(accent, locationGranted, onGrant = {
                                locationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            })
                            2 -> MediaStep(accent, mediaGranted, onGrant = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                }
                            })
                            3 -> FinalStep(accent, onSetDefault = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                                }
                            })
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // UNIFIED WIZARD FOOTER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button (left aligned)
                    if (currentStep > 0) {
                        TextButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("BACK", color = Color(0xFF888888), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    // Next / Finish button (right aligned)
                    val isPrimary = when (currentStep) {
                        0 -> true
                        1 -> locationGranted
                        2 -> mediaGranted
                        3 -> true
                        else -> true
                    }

                    val nextButtonLabel = when (currentStep) {
                        0 -> "GET STARTED"
                        1 -> if (locationGranted) "CONTINUE" else "SKIP FOR NOW"
                        2 -> if (mediaGranted) "CONTINUE" else "SKIP FOR NOW"
                        3 -> "FINISH SETUP"
                        else -> "CONTINUE"
                    }

                    val nextButtonIcon = if (currentStep == 3) Icons.Default.Check else Icons.Default.ArrowForward

                    if (isPrimary) {
                        Button(
                            onClick = {
                                if (currentStep < 3) {
                                    currentStep++
                                } else {
                                    onComplete()
                                }
                            },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(nextButtonLabel, color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(nextButtonIcon, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                currentStep++
                            },
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(nextButtonLabel, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(nextButtonIcon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    stepIndex: Int,
    title: String,
    currentStep: Int
) {
    val active = stepIndex == currentStep
    val completed = stepIndex < currentStep

    val markerColor =
        when {
            active -> Aw11Primary
            completed -> Aw11Secondary
            else -> Aw11Border
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .border(
                    width = 1.dp,
                    color = markerColor
                ),
            contentAlignment = Alignment.Center
        ) {
            if (active) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Aw11Primary)
                )
            } else if (completed) {
                Text(
                    text = ">",
                    color = Aw11Secondary,
                    fontFamily = JetBrainsMono,
                    fontSize = 9.sp
                )
            }
        }

        Text(
            text = title.uppercase(),
            color =
                if (active) {
                    Aw11Primary
                } else {
                    Aw11Secondary
                },
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun IntroStep(accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "WELCOME TO RETROLAUNCHER",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 2.sp,
            fontSize = 20.sp
        )
        Text(
            text = "A retro-inspired automotive launcher built around navigation, vehicle data and media control.",
            color = Color(0xFFAAAAAA),
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BulletItem(
                Icons.Default.Navigation,
                "Integrated Navigation",
                "Built-in map, destination search, route guidance and automatic rerouting."
            )

            BulletItem(
                Icons.Default.DirectionsCar,
                "Vehicle Dashboard",
                "Real-time speed, compass heading and trip information designed for an in-car display."
            )

            BulletItem(
                Icons.Default.MusicNote,
                "Media Control",
                "View the active media source and control playback directly from the dashboard."
            )
        }
    }
}

@Composable
private fun LocationStep(accent: Color, isGranted: Boolean, onGrant: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "LOCATION & NAVIGATION",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 2.sp,
            fontSize = 20.sp
        )
        Text(
            text = "RetroLauncher uses location access for navigation, vehicle speed, trip tracking and compass-related features.",
            color = Color(0xFFAAAAAA),
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(if (isGranted) Color(0xFF0F1E10) else Color(0xFF1E1010))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF44AA44) else Color(0xFFDD5555),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = if (isGranted) "Permission Granted" else "Permission Required",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isGranted) "GPS telemetry is active and ready." else "Telemetry is currently disabled.",
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (!isGranted) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onGrant,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("GRANT ACCESS", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MediaStep(accent: Color, isGranted: Boolean, onGrant: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "MEDIA INTEGRATION",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 2.sp,
            fontSize = 20.sp
        )
        Text(
            text = "To access active media sessions, display track information and provide playback controls, RetroLauncher requires notification access.",
            color = Color(0xFFAAAAAA),
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(if (isGranted) Color(0xFF0F1E10) else Color(0xFF1E1010))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF44AA44) else Color(0xFFDD5555),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = if (isGranted) "Notification Access Granted" else "Notification Access Required",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isGranted) "Music player widget is connected." else "Now Playing dashboard will remain inactive.",
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (!isGranted) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onGrant,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.VolumeUp, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("ENABLE MEDIA LISTENER", color = Color.Black, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FinalStep(accent: Color, onSetDefault: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "READY FOR THE ROAD!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 2.sp,
            fontSize = 20.sp
        )
        Text(
            text = "You are all set up and ready to go. You can set Open Launcher as your default home app so it launches automatically whenever you start your vehicle.",
            color = Color(0xFFAAAAAA),
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onSetDefault,
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("SET AS DEFAULT", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
        }
    }
}

@Composable
private fun BulletItem(icon: ImageVector, title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Column {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Text(desc, color = Color(0xFF888888), fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}
