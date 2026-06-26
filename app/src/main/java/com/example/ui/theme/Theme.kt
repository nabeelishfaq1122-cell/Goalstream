package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = GoalStreamPrimary,
    secondary = GoalStreamSecondary,
    background = Color(0xFF121318),
    surface = Color(0xFF1B1B1F),
    onPrimary = Color.White,
    onSecondary = GoalStreamDarkBlue,
    onBackground = Color(0xFFE3E2E6),
    onSurface = Color(0xFFE3E2E6)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GoalStreamPrimary,
    secondary = GoalStreamSecondary,
    tertiary = GoalStreamDeepBlue,
    background = GoalStreamBackground,
    surface = GoalStreamSurface,
    onPrimary = Color.White,
    onSecondary = GoalStreamDarkBlue,
    onBackground = GoalStreamTextMain,
    onSurface = GoalStreamTextMain,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to preserve the exact brand colors requested
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
