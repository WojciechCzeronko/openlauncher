package com.openlauncher.app.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.map.HereSearchResult
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary

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

    if (!isOpen) {
        Box(
            modifier = modifier
                .background(
                    Aw11Background.copy(alpha = 0.90f)
                )
                .border(
                    width = 1.dp,
                    color = Aw11Primary.copy(alpha = 0.85f)
                )
                .clickable(onClick = onOpen)
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
        ) {
            Text(
                text = "SEARCH",
                color = Aw11Primary,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
        }

        return
    }

    Column(
        modifier = modifier
            .width(420.dp)
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
                                .padding(8.dp)
                        )
                    }
                },
                placeholder = {
                    Text(
                        text = "DESTINATION...",
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(
                Modifier.width(8.dp)
            )

            Text(
                text = "SEARCH",
                color = Aw11Primary,
                fontSize = 11.sp,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Aw11Primary.copy(alpha = 0.85f)
                    )
                    .clickable {
                        if (
                            query.isBlank() ||
                            isSearching
                        ) {
                            return@clickable
                        }

                        focusManager.clearFocus()
                        keyboardController?.hide()

                        onSearch()
                    }
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    )
            )

            Spacer(
                Modifier.width(6.dp)
            )

            Text(
                text = "X",
                color = Aw11Secondary,
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable {
                        focusManager.clearFocus()
                        keyboardController?.hide()

                        onClose()
                    }
                    .padding(8.dp)
            )
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

        results.forEach { result ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .border(
                        width = 1.dp,
                        color = Aw11Primary.copy(alpha = 0.35f)
                    )
                    .clickable {
                        onResultSelected(result)
                    }
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = result.title.uppercase(),
                        color = Aw11Primary,
                        fontSize = 12.sp,
                        letterSpacing = 0.25.sp
                    )

                    Text(
                        text = result.address.uppercase(),
                        color = Aw11Secondary,
                        fontSize = 10.sp,
                        letterSpacing = 0.sp
                    )
                }
            }
        }
    }
}