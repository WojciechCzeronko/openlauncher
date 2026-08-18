package com.openlauncher.app.ui.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openlauncher.app.model.WeatherState
import com.openlauncher.app.ui.theme.DSEGWeather

@Composable
fun WeatherWidget(
    state: WeatherState?,
    accent: Color,
    metric: Boolean,
    isDayMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isDayMode) Color(0xFF111111) else MaterialTheme.colorScheme.onBackground
    val subColor     = if (isDayMode) Color(0xFF888888) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)

    Box(modifier = modifier) {
        if (state != null) {
            Column(
                modifier            = Modifier.fillMaxSize().padding(start = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text       = dsegWeatherGlyph(state.weatherCode, state.isDay),
                    color      = accent,
                    fontFamily = DSEGWeather,
                    fontSize   = 84.sp
                )
                Spacer(Modifier.height(0.dp))
                Text(
                    text       = state.temperatureDisplay(metric),
                    color      = contentColor,
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                )
                Text(
                    text          = state.conditionLabel.uppercase(),
                    color         = subColor,
                    fontSize      = 9.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

private fun dsegWeatherGlyph(code: Int, isDay: Boolean): String = when (code) {
    0 -> if (isDay) "1" else "0"      // clear / night
    1, 2 -> "9"                       // sun + cloud
    3 -> "2"                          // cloud
    45, 48 -> "2"                     // fog -> cloud fallback
    51, 53, 55 -> "3"                 // rain / drizzle
    61, 63, 65 -> "3"                 // rain
    71, 73, 75 -> "5"                 // snow
    80, 81, 82 -> "3"                 // showers
    95 -> "8"                         // thunder
    96, 99 -> "6"                     // thunder + rain
    else -> "2"
}