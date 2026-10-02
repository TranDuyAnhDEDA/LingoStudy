package com.example.mystudyapp.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary           = LingoBlue,
    onPrimary         = LingoSurface,
    primaryContainer  = LingoBlueLight,
    secondary         = LingoOrange,
    tertiary          = LingoGreen,
    background        = LingoBackground,
    surface           = LingoSurface,
    onBackground      = LingoOnSurface,
    onSurface         = LingoOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary   = LingoBlueLight,
    secondary = LingoOrange,
    tertiary  = LingoGreen
)

@Composable
fun MyStudyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disabled so LingoFlow brand colors are always used instead of wallpaper colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
