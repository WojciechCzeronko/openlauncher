package com.openlauncher.app.ui.map.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.map.HereSearchResult
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import java.util.Locale

@Composable
fun Aw11SearchPanel(
    isOpen: Boolean,
    query: String,
    results: List<HereSearchResult>,
    isSearching: Boolean,
    error: String?,
    onOpen: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onResultSelected: (HereSearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun triggerSearch() {
        if (
            query.isBlank() ||
            isSearching
        ) {
            return
        }

        focusManager.clearFocus()
        keyboardController?.hide()

        onSearch()
    }
    if (!isOpen) {
        Box(
            modifier = modifier
                .widthIn(min = 112.dp)
                .heightIn(min = 56.dp)
                .background(
                    Aw11Background.copy(alpha = 0.90f)
                )
                .border(
                    width = 1.dp,
                    color = Aw11Primary.copy(alpha = 0.85f)
                )
                .clickable(onClick = onOpen)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SEARCH",
                color = Aw11Primary,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp
            )
        }

        return
    }

    Column(
        modifier = modifier
            .width(240.dp)
            .background(
                Aw11Background.copy(alpha = 0.96f)
            )
            .border(
                width = 1.dp,
                color = Aw11Primary.copy(alpha = 0.85f)
            )
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        triggerSearch()
                    }
                ),
                textStyle =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp
                    ),
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Text(
                            text = "CLR",
                            color = Aw11Secondary,
                            fontSize = 10.sp,
                            letterSpacing = 0.sp,
                            modifier = Modifier
                                .clickable(onClick = onClear)
                                .padding(6.dp)
                        )
                    }
                },
                placeholder = {
                    Text(
                        text = "DESTINATION...",
                        fontSize = 10.sp
                    )
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(
                Modifier.width(6.dp)
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(
                        width = 1.dp,
                        color = Aw11Primary.copy(
                            alpha = 0.85f
                        )
                    )
                    .clickable {
                        triggerSearch()
                    },
                contentAlignment =
                    Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(20.dp)
                ) {
                    val strokeWidth =
                        2.dp.toPx()

                    drawCircle(
                        color = Aw11Primary,
                        radius =
                            size.minDimension * 0.28f,
                        center = Offset(
                            x = size.width * 0.42f,
                            y = size.height * 0.42f
                        ),
                        style = Stroke(
                            width = strokeWidth
                        )
                    )

                    drawLine(
                        color = Aw11Primary,
                        start = Offset(
                            x = size.width * 0.62f,
                            y = size.height * 0.62f
                        ),
                        end = Offset(
                            x = size.width * 0.84f,
                            y = size.height * 0.84f
                        ),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Square
                    )
                }
            }

            Spacer(
                Modifier.width(4.dp)
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(
                        width = 1.dp,
                        color = Aw11Primary.copy(
                            alpha = 0.85f
                        )
                    )
                    .clickable {
                        focusManager.clearFocus()
                        keyboardController?.hide()

                        onClose()
                    },
                contentAlignment =
                    Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(18.dp)
                ) {
                    val strokeWidth =
                        2.dp.toPx()

                    drawLine(
                        color = Aw11Secondary,
                        start = Offset(
                            x = size.width * 0.2f,
                            y = size.height * 0.2f
                        ),
                        end = Offset(
                            x = size.width * 0.8f,
                            y = size.height * 0.8f
                        ),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Square
                    )

                    drawLine(
                        color = Aw11Secondary,
                        start = Offset(
                            x = size.width * 0.8f,
                            y = size.height * 0.2f
                        ),
                        end = Offset(
                            x = size.width * 0.2f,
                            y = size.height * 0.8f
                        ),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Square
                    )
                }
            }
        }

        if (isSearching) {
            Text(
                text = "SEARCHING...",
                color = Aw11Secondary,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        error?.let { searchError ->
            Text(
                text = "SEARCH ERROR: $searchError",
                color = Aw11Primary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        results.forEachIndexed { index, result ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .border(
                        width = 1.dp,
                        color = Aw11Primary.copy(alpha = 0.35f)
                    )
                    .clickable {
                        onResultSelected(result)
                    }
                    .padding(
                        horizontal = 6.dp,
                        vertical = 6.dp
                    )
            ) {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .border(
                                width = 1.dp,
                                color = Aw11Primary
                            )
                            .padding(vertical = 3.dp),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            color = Aw11Primary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(
                        Modifier.width(6.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Row {
                            Text(
                                text =
                                    result.title.uppercase(),
                                color = Aw11Primary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow =
                                    TextOverflow.Ellipsis,
                                modifier =
                                    Modifier.weight(1f)
                            )

                            Text(
                                text = formatDistance(
                                    result.distanceMeters
                                ),
                                color = Aw11Secondary,
                                fontSize = 9.sp
                            )
                        }

                        Text(
                            text =
                                result.address.uppercase(),
                            color = Aw11Secondary,
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun formatDistance(
    distanceMeters: Double
): String {
    return if (distanceMeters < 1000.0) {
        "${distanceMeters.toInt()} M"
    } else {
        String.format(
            Locale.US,
            "%.1f KM",
            distanceMeters / 1000.0
        )
    }
}