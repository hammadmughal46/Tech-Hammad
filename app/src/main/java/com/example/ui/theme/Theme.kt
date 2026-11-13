package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PakAdsColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = DarkBackground,
    primaryContainer = Color(0xFF003816),
    onPrimaryContainer = NeonGreen,
    
    secondary = NeonCyan,
    onSecondary = DarkBackground,
    secondaryContainer = Color(0xFF00364A),
    onSecondaryContainer = NeonCyan,
    
    tertiary = GoldYellow,
    onTertiary = DarkBackground,
    tertiaryContainer = Color(0xFF423700),
    onTertiaryContainer = GoldYellow,
    
    background = DarkBackground,
    onBackground = TextWhite,
    
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextGrayLight,
    
    outline = DarkBorderHighlight,
    outlineVariant = DarkSurfaceBorder,
    
    error = ErrorRed,
    onError = TextWhite,
    errorContainer = Color(0xFF4D0005),
    onErrorContainer = Color(0xFFFFB4AB)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme as required by PAK ADS design spec
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                WindowCompat.setDecorFitsSystemWindows(it, false)
                it.statusBarColor = DarkBackground.toArgb()
                it.navigationBarColor = DarkBackground.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = PakAdsColorScheme,
        typography = Typography,
        content = content
    )
}
