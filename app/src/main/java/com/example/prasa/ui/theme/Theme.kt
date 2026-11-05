package com.example.prasa.ui.theme

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
    primary = PrasaBlue80,
    secondary = PrasaGold80,
    tertiary = PrasaSlate80
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF245F93),
    secondary = Color(0xFFE8B846),
    tertiary = Color(0xFF647789),
    background = Color(0xFFF1F5F8),
    surface = Color(0xFFFAFCFD),
    surfaceVariant = Color(0xFFE7EEF3),
    onPrimary = Color.White,
    onSecondary = Color(0xFF273441),
    onBackground = Color(0xFF253746),
    onSurface = Color(0xFF253746)
)

@Composable
fun PrasaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
