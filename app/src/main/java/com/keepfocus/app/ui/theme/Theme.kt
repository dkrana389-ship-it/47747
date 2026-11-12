package com.keepfocus.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepBluePrimary = Color(0xFF1E40AF)
val TealAccent = Color(0xFF14B8A6)
val LightBg = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val DarkBg = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)

private val LightColorScheme = lightColorScheme(
    primary = DeepBluePrimary,
    onPrimary = Color.White,
    secondary = TealAccent,
    onSecondary = Color.White,
    background = LightBg,
    surface = LightSurface,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    secondary = TealAccent,
    onSecondary = Color(0xFF0F172A),
    background = DarkBg,
    surface = DarkSurface,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

@Composable
fun KeepFocusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
