package com.spider.vpn.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SpiderRed,
    onPrimary = TextPrimary,
    secondary = SpiderLightRed,
    onSecondary = TextPrimary,
    background = BgDark,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassStroke,
)

@Composable
fun SpiderVPNTheme(content: @Composable () -> Unit) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
