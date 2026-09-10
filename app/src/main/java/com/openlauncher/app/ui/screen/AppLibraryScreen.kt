package com.openlauncher.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.openlauncher.app.model.AppInfo
import com.openlauncher.app.ui.theme.LocalDayMode
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Border
import com.openlauncher.app.ui.theme.Aw11Dim
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary

private enum class AppFilter { USER, SYSTEM, ALL }

@Composable
fun AppLibraryScreen(
    apps: List<AppInfo>,
    isLoading: Boolean,
    isPickerMode: Boolean,
    pickerSlot: Int?,
    isCarPlayPickerMode: Boolean,
    carPlayPickerLabel: String = "CHOOSE CARPLAY APP",
    accent: Color,
    onAppClick: (AppInfo) -> Unit,
    onPickerSelect: (Int, AppInfo) -> Unit,
    onCarPlaySelect: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenBg = Aw11Background
    val headerColor = Aw11Primary
    val placeholderC = Aw11Dim
    val dividerColor = Aw11Border.copy(alpha = 0.45f)
    val emptyColor = Aw11Secondary
    val fieldTextC = Aw11Primary
    val fieldBorderU = Aw11Border.copy(alpha = 0.65f)

    val anyPickerMode = isPickerMode || isCarPlayPickerMode
    var query     by remember { mutableStateOf("") }
    var appFilter by remember { mutableStateOf(AppFilter.USER) }

    val filtered = remember(apps, query, appFilter, anyPickerMode) {
        val byName = if (query.isBlank()) apps
                     else apps.filter { it.appName.contains(query, ignoreCase = true) }
        // In picker mode always show everything so shortcuts can be set to any app
        if (anyPickerMode) byName
        else when (appFilter) {
            AppFilter.USER   -> byName.filter { !it.isSystemApp }
            AppFilter.SYSTEM -> byName.filter { it.isSystemApp }
            AppFilter.ALL    -> byName
        }
    }

    Column(modifier = modifier.fillMaxSize().background(screenBg)) {
        // ── AW11 header ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 10.dp
                )
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        isCarPlayPickerMode -> carPlayPickerLabel
                        anyPickerMode -> "CHOOSE APP"
                        else -> "APP LIBRARY"
                    },
                    color = headerColor,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "${filtered.size.toString().padStart(2, '0')} ENTRIES",
                    color = Aw11Secondary,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                if (!anyPickerMode) {
                    AppFilter.entries.forEach { filter ->

                        val selected =
                            appFilter == filter

                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .border(
                                    width = 1.dp,
                                    color =
                                        if (selected) {
                                            Aw11Primary
                                        } else {
                                            fieldBorderU
                                        }
                                )
                                .clickable {
                                    appFilter = filter
                                }
                                .padding(
                                    horizontal = 14.dp
                                ),
                            contentAlignment =
                                Alignment.Center
                        ) {
                            Text(
                                text = when (filter) {
                                    AppFilter.USER -> "USER"
                                    AppFilter.SYSTEM -> "SYSTEM"
                                    AppFilter.ALL -> "ALL"
                                },
                                color =
                                    if (selected) {
                                        Aw11Primary
                                    } else {
                                        Aw11Secondary
                                    },
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                var searchFocused by remember {
                    mutableStateOf(false)
                }

                Box(
                    modifier = Modifier
                        .width(230.dp)
                        .height(34.dp)
                        .border(
                            width = 1.dp,
                            color =
                                if (searchFocused) {
                                    Aw11Primary
                                } else {
                                    fieldBorderU
                                }
                        )
                        .padding(
                            horizontal = 10.dp
                        ),
                    contentAlignment =
                        Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(7.dp)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Search,
                            contentDescription = null,
                            tint =
                                if (searchFocused) {
                                    Aw11Primary
                                } else {
                                    Aw11Secondary
                                },
                            modifier =
                                Modifier.size(14.dp)
                        )

                        BasicTextField(
                            value = query,
                            onValueChange = {
                                query = it
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = fieldTextC,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            ),
                            cursorBrush =
                                SolidColor(Aw11Primary),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged {
                                    searchFocused =
                                        it.isFocused
                                },
                            decorationBox = { inner ->
                                Box {
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "SEARCH...",
                                            color = placeholderC,
                                            fontSize = 10.sp,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    inner()
                                }
                            }
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            color = dividerColor
        )

        HorizontalDivider(color = dividerColor)

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accent, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
            return@Column
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No apps found", color = emptyColor, letterSpacing = 1.sp, fontSize = 12.sp)
            }
            return@Column
        }

        // ── App grid ────────────────────────────────────────────────────────────
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.packageName }) { app ->
                AppTile(
                    app     = app,
                    accent  = accent,
                    onClick = {
                        when {
                            isCarPlayPickerMode            -> onCarPlaySelect(app)
                            isPickerMode && pickerSlot != null -> onPickerSelect(pickerSlot, app)
                            else                           -> onAppClick(app)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AppTile(
    app: AppInfo,
    accent: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .aspectRatio(1.35f)
            .background(Aw11Background)
            .border(
                width = 1.dp,
                color = Aw11Border.copy(alpha = 0.55f)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 8.dp,
                vertical = 7.dp
            )
    ) {
        val bmp = remember(app.packageName) {
            try {
                app.icon.toBitmap(80, 80)
            } catch (_: Exception) {
                null
            }
        }

        if (bmp != null) {
            androidx.compose.foundation.Image(
                painter =
                    BitmapPainter(
                        bmp.asImageBitmap()
                    ),
                contentDescription =
                    app.appName,
                modifier =
                    Modifier.size(44.dp)
            )
        } else {
            Box(
                modifier =
                    Modifier.size(44.dp),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        app.appName
                            .take(1)
                            .uppercase(),
                    color = accent,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(
            Modifier.height(6.dp)
        )

        Text(
            text =
                app.appName.uppercase(),
            color = Aw11Primary,
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis,
            textAlign =
                TextAlign.Center,
            letterSpacing = 0.8.sp,
            fontSize = 10.sp
        )
    }
}
