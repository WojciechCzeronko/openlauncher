package com.openlauncher.app.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Primary

@Composable
fun Aw11RecenterButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) {
        return
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .background(
                Aw11Background.copy(alpha = 0.90f)
            )
            .border(
                width = 1.dp,
                color = Aw11Primary.copy(alpha = 0.85f)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "⌖",
            color = Aw11Primary,
            fontSize = 20.sp
        )
    }
}