package com.openlauncher.app.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
                    color = Aw11Secondary.copy(alpha = 0.35f),
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        Aw11OnboardingButton(
                            text = "Back",
                            icon = Icons.Default.ArrowBack,
                            primary = false,
                            onClick = {
                                currentStep--
                            }
                        )
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    val nextButtonLabel =
                        when (currentStep) {
                            0 -> "Get Started"
                            1 ->
                                if (locationGranted) {
                                    "Continue"
                                } else {
                                    "Skip For Now"
                                }

                            2 ->
                                if (mediaGranted) {
                                    "Continue"
                                } else {
                                    "Skip For Now"
                                }

                            3 -> "Finish Setup"
                            else -> "Continue"
                        }

                    Aw11OnboardingButton(
                        text = nextButtonLabel,
                        icon =
                            if (currentStep == 3) {
                                Icons.Default.Check
                            } else {
                                Icons.Default.ArrowForward
                            },
                        primary = true,
                        onClick = {
                            if (currentStep < 3) {
                                currentStep++
                            } else {
                                onComplete()
                            }
                        }
                    )
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
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "WELCOME TO RETROLAUNCHER",
            color = Aw11Primary,
            fontFamily = JetBrainsMono,
            letterSpacing = 2.sp,
            fontSize = 18.sp
        )

        Text(
            text =
                "A retro-inspired automotive launcher built around navigation, vehicle data and media control.",
            color = Aw11Secondary,
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            lineHeight = 16.sp
        )

        Spacer(Modifier.height(6.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BulletItem(
                Icons.Default.Navigation,
                "Integrated Navigation",
                "Built-in destination search, route guidance and automatic rerouting."
            )

            BulletItem(
                Icons.Default.DirectionsCar,
                "Vehicle Dashboard",
                "Real-time speed, compass heading and trip information."
            )

            BulletItem(
                Icons.Default.MusicNote,
                "Media Control",
                "Track information and playback controls directly from the dashboard."
            )
        }
    }
}

@Composable
private fun LocationStep(accent: Color, isGranted: Boolean, onGrant: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "LOCATION & NAVIGATION",
            color = Aw11Primary,
            fontFamily = JetBrainsMono,
            letterSpacing = 2.sp,
            fontSize = 18.sp
        )

        Text(
            text =
                "RetroLauncher uses location access for navigation, vehicle speed, trip tracking and compass-related features.",
            color = Aw11Secondary,
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            lineHeight = 16.sp
        )

        Spacer(Modifier.height(16.dp))
        Aw11PermissionStatus(
            granted = isGranted,
            grantedText = "GPS AND LOCATION SERVICES AVAILABLE",
            missingText = "LOCATION ACCESS IS REQUIRED"
        )

        if (!isGranted) {
            Spacer(Modifier.height(8.dp))

            Aw11OnboardingButton(
                text = "Grant Location Access",
                icon = Icons.Default.LocationOn,
                onClick = onGrant
            )
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
            color = Aw11Primary,
            fontFamily = JetBrainsMono,
            letterSpacing = 2.sp,
            fontSize = 18.sp
        )

        Text(
            text =
                "Notification access allows RetroLauncher to display active media and provide playback controls.",
            color = Aw11Secondary,
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            lineHeight = 16.sp
        )

        Spacer(Modifier.height(16.dp))

        Aw11PermissionStatus(
            granted = isGranted,
            grantedText = "MEDIA SESSION ACCESS AVAILABLE",
            missingText = "NOTIFICATION ACCESS IS REQUIRED"
        )

        if (!isGranted) {
            Spacer(Modifier.height(8.dp))

            Aw11OnboardingButton(
                text = "Enable Media Listener",
                icon = Icons.Default.VolumeUp,
                onClick = onGrant
            )
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
private fun FinalStep(
    accent: Color,
    onSetDefault: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SYSTEM READY",
            color = Aw11Primary,
            fontFamily = JetBrainsMono,
            letterSpacing = 2.sp,
            fontSize = 18.sp
        )

        Text(
            text =
                "RetroLauncher is configured and ready for use. Set it as the default home application to launch directly into the dashboard.",
            color = Aw11Secondary,
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            lineHeight = 16.sp
        )

        Spacer(Modifier.height(8.dp))

        Aw11OnboardingButton(
            text = "Set As Default Launcher",
            icon = Icons.Default.Home,
            onClick = onSetDefault
        )
    }
}

@Composable
private fun Aw11OnboardingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    primary: Boolean = true
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .border(
                width = 1.dp,
                color =
                    if (primary) {
                        Aw11Primary
                    } else {
                        Aw11Border.copy(alpha = 0.75f)
                    }
            )
            .background(
                if (primary) {
                    Aw11Primary.copy(alpha = 0.14f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint =
                    if (primary) {
                        Aw11Primary
                    } else {
                        Aw11Secondary
                    },
                modifier = Modifier.size(15.dp)
            )

            Spacer(Modifier.width(8.dp))
        }

        Text(
            text = text.uppercase(),
            color =
                if (primary) {
                    Aw11Primary
                } else {
                    Aw11Secondary
                },
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun Aw11PermissionStatus(
    granted: Boolean,
    grantedText: String,
    missingText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color =
                    if (granted) {
                        Aw11Primary.copy(alpha = 0.8f)
                    } else {
                        Aw11Border.copy(alpha = 0.75f)
                    }
            )
            .background(
                Aw11Secondary.copy(alpha = 0.04f)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(
                    1.dp,
                    if (granted) {
                        Aw11Primary
                    } else {
                        Aw11Secondary
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text =
                    if (granted) {
                        "OK"
                    } else {
                        "--"
                    },
                color =
                    if (granted) {
                        Aw11Primary
                    } else {
                        Aw11Secondary
                    },
                fontFamily = JetBrainsMono,
                fontSize = 8.sp
            )
        }

        Column {
            Text(
                text =
                    if (granted) {
                        "SYSTEM READY"
                    } else {
                        "ACTION REQUIRED"
                    },
                color =
                    if (granted) {
                        Aw11Primary
                    } else {
                        Aw11Secondary
                    },
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text =
                    if (granted) {
                        grantedText
                    } else {
                        missingText
                    },
                color = Aw11Secondary,
                fontFamily = JetBrainsMono,
                fontSize = 9.sp
            )
        }
    }
}
@Composable
private fun BulletItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(
                    1.dp,
                    Aw11Border.copy(alpha = 0.75f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Aw11Secondary,
                modifier = Modifier.size(15.dp)
            )
        }

        Column {
            Text(
                text = title.uppercase(),
                color = Aw11Primary,
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = desc,
                color = Aw11Secondary,
                fontFamily = JetBrainsMono,
                fontSize = 9.sp,
                lineHeight = 14.sp
            )
        }
    }
}
