package com.perrygarg.injoyapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.perrygarg.injoyapp.ui.theme.*

private val DarkColorScheme = darkColorScheme(
    primary = InshortsBlue,
    onPrimary = Color.White,
    secondary = InshortsRed,
    onSecondary = Color.White,
    tertiary = InshortsGold,
    background = InshortsDarkGray,
    onBackground = Color.White,
    surface = InshortsCardDark,
    onSurface = Color.White,
    surfaceVariant = InshortsDarkGray,
    onSurfaceVariant = InshortsTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = InshortsBlue,
    onPrimary = Color.White,
    secondary = InshortsRed,
    onSecondary = Color.White,
    tertiary = InshortsGold,
    background = InshortsGray,
    onBackground = InshortsText,
    surface = InshortsCard,
    onSurface = InshortsText,
    surfaceVariant = InshortsGray,
    onSurfaceVariant = InshortsTextSecondary
)

@Composable
fun InJoyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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