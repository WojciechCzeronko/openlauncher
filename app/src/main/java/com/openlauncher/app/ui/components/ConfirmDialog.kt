package com.openlauncher.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.openlauncher.app.ui.theme.Aw11Background
import com.openlauncher.app.ui.theme.Aw11Border
import com.openlauncher.app.ui.theme.Aw11Primary
import com.openlauncher.app.ui.theme.Aw11Secondary
import com.openlauncher.app.ui.theme.JetBrainsMono

private val Aw11Danger = Color(0xFFCC6666)

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.42f)
                .widthIn(
                    min = 360.dp,
                    max = 520.dp
                )
                .background(Aw11Background)
                .border(
                    width = 1.dp,
                    color = Aw11Border
                )
        ) {
            Text(
                text = title.uppercase(),
                color = Aw11Primary,
                fontFamily = JetBrainsMono,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
            )

            HorizontalDivider(
                color = Aw11Border.copy(
                    alpha = 0.65f
                )
            )

            Text(
                text = message,
                color = Aw11Secondary,
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 18.dp
                )
            )

            HorizontalDivider(
                color = Aw11Border.copy(
                    alpha = 0.35f
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp,
                        Alignment.End
                    )
            ) {
                Aw11DialogButton(
                    text = "Cancel",
                    color = Aw11Secondary,
                    onClick = onDismiss
                )

                Aw11DialogButton(
                    text = confirmLabel,
                    color = Aw11Danger,
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
private fun Aw11DialogButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .border(
                width = 1.dp,
                color = color.copy(
                    alpha = 0.75f
                )
            )
            .background(
                color.copy(
                    alpha = 0.08f
                )
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontFamily = JetBrainsMono,
            fontSize = 9.sp,
            letterSpacing = 1.sp
        )
    }
}