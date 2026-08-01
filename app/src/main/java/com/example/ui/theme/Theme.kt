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

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    secondary = CyanAccent,
    onSecondary = Color.Black,
    tertiary = EmeraldSuccess,
    background = DarkBackground,
    onBackground = Color(0xFFF3F4F6),
    surface = DarkSurface,
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD1D5DB),
    outline = DarkBorder,
    error = RoseError
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = Color.White,
    secondary = CyanAccent,
    onSecondary = Color.Black,
    tertiary = EmeraldSuccess,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF334155),
    outline = LightBorder,
    error = RoseError
)

@Composable
fun DecodeItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent brand theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
