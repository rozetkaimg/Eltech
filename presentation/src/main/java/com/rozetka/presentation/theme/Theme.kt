package com.rozetka.presentation.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.rozetka.localdata.SettingsData
import com.rozetka.presentation.util.ThemeObject.ColorThemeState
import com.rozetka.presentation.util.ThemeObject.DarkThemeState
import com.rozetka.presentation.util.ThemeObject.DynamicColorState
import com.rozetka.presentation.util.ThemeObject.StatusBarIconColor
import com.rozetka.presentation.colors.GreenColor
import com.rozetka.presentation.colors.OrangeColor
import com.rozetka.presentation.colors.PinkColor
import com.rozetka.presentation.colors.PixelColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun EPolitechTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val settings = SettingsData(context)
            DynamicColorState.value = settings.getMonetState(context)
            DarkThemeState.value = settings.getThemeState(context)
            ColorThemeState.value = settings.getMonetStaticColor(context)
        }
    }

    val isDark = when (DarkThemeState.value) {
        0 -> darkTheme
        1 -> false
        else -> true
    }

    val useDynamicColor = dynamicColor &&
            DynamicColorState.value &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    StatusBarIconColor.value = !isDark

    val colorScheme = when {
        useDynamicColor -> {
            if (isDark) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        else -> getStaticColorScheme(isDark)
    }

    MaterialTheme(
        colorScheme = colorScheme.switch(),
        content = content
    )
}

@Composable
private fun getStaticColorScheme(isDark: Boolean): ColorScheme {
    val staticColor = when (ColorThemeState.value) {
        1 -> GreenColor()
        2 -> OrangeColor()
        3 -> PinkColor()
        else -> PixelColor()
    }
    return if (isDark) staticColor.darkScheme else staticColor.lightScheme
}

@Composable
fun animateColor(targetValue: Color) =
    animateColorAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 700)
    ).value

@Composable
fun ColorScheme.switch() = copy(
    primary = animateColor(primary),
    onPrimary = animateColor(onPrimary),
    primaryContainer = animateColor(primaryContainer),
    onPrimaryContainer = animateColor(onPrimaryContainer),
    inversePrimary = animateColor(inversePrimary),
    secondary = animateColor(secondary),
    onSecondary = animateColor(onSecondary),
    secondaryContainer = animateColor(secondaryContainer),
    onSecondaryContainer = animateColor(onSecondaryContainer),
    tertiary = animateColor(tertiary),
    onTertiary = animateColor(onTertiary),
    tertiaryContainer = animateColor(tertiaryContainer),
    onTertiaryContainer = animateColor(onTertiaryContainer),
    background = animateColor(background),
    onBackground = animateColor(onBackground),
    surface = animateColor(surface),
    onSurface = animateColor(onSurface),
    surfaceVariant = animateColor(surfaceVariant),
    onSurfaceVariant = animateColor(onSurfaceVariant),
    surfaceTint = animateColor(surfaceTint),
    inverseSurface = animateColor(inverseSurface),
    inverseOnSurface = animateColor(inverseOnSurface),
    error = animateColor(error),
    onError = animateColor(onError),
    errorContainer = animateColor(errorContainer),
    onErrorContainer = animateColor(onErrorContainer),
    outline = animateColor(outline),
    outlineVariant = animateColor(outlineVariant),
    scrim = animateColor(scrim)
)