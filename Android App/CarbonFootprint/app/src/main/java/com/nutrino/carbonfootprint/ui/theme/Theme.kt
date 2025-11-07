package com.nutrino.carbonfootprint.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFF388E3C),
    tertiary = Color(0xFF5D4037),
    background = Color(0xFF0D1B0F),
    surface = Color(0xFF1B2E1F),
    surfaceVariant = Color(0xFF2C3E30),
    onPrimary = Color(0xFFFAFAFA),
    onSecondary = Color(0xFFFAFAFA),
    onTertiary = Color(0xFFFAFAFA),
    onBackground = Color(0xFFE8F5E8),
    onSurface = Color(0xFFE8F5E8),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondaryContainer = Color(0xFF2E7D32),
    onSecondaryContainer = Color(0xFFDCEDC8),
    error = Color(0xFFE57373),
    onError = Color(0xFFFAFAFA)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF8BC34A),
    tertiary = Color(0xFF66BB6A),
    background = Color(0xFFF1F8E9),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8F5E8),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1B5E20),
    onSurface = Color(0xFF1B5E20),
    primaryContainer = Color(0xFFDCEDC8),
    onPrimaryContainer = Color(0xFF0D5302),
    secondaryContainer = Color(0xFFE7F4E4),
    onSecondaryContainer = Color(0xFF1B5E20),
    error = Color(0xFFE53935),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun CarbonFootprintTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Always use our beautiful custom eco-theme instead of dynamic colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Always use our custom green eco-theme colors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
