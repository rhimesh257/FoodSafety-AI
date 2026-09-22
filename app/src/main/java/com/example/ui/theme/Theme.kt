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
        primary = EmeraldLight,
        onPrimary = Slate900,
        primaryContainer = EmeraldDark,
        onPrimaryContainer = EmeraldContainer,
        secondary = Navy100,
        onSecondary = Slate900,
        secondaryContainer = Navy800,
        onSecondaryContainer = Navy100,
        tertiary = AmberWarning,
        background = Navy900,
        surface = Slate900,
        onBackground = Slate100,
        onSurface = Slate100,
        surfaceVariant = Slate800,
        onSurfaceVariant = Slate200,
        error = RedCritical,
        errorContainer = RedContainer
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Navy800,
        onPrimary = Color.White,
        primaryContainer = Navy100,
        onPrimaryContainer = Navy900,
        secondary = EmeraldPrimary,
        onSecondary = Color.White,
        secondaryContainer = EmeraldContainer,
        onSecondaryContainer = OnEmeraldContainer,
        tertiary = AmberWarning,
        background = Slate50,
        surface = Color.White,
        onBackground = Slate900,
        onSurface = Slate900,
        surfaceVariant = Slate100,
        onSurfaceVariant = Slate700,
        error = RedCritical,
        errorContainer = RedContainer
    )

@Composable
fun FoodSafeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature food safety authority palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    FoodSafeTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
