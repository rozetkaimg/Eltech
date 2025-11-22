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
import androidx.compose.runtime.getValue        // Добавлен импорт
import androidx.compose.runtime.mutableStateOf  // Добавлен импорт
import androidx.compose.runtime.remember        // Добавлен импорт
import androidx.compose.runtime.setValue        // Добавлен импорт
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
    var isSettingsLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            val settings = SettingsData(context)
            DynamicColorState.value = settings.getMonetState(context)
            DarkThemeState.value = settings.getThemeState(context)
            ColorThemeState.value = settings.getMonetStaticColor(context)
        }
        isSettingsLoaded = true
    }


    val isDark = if (!isSettingsLoaded) {
        darkTheme
    } else {
        when (DarkThemeState.value) {
            0 -> darkTheme // Системная настройка в приложении
            1 -> false     // Принудительно светлая
            else -> true   // Принудительно темная
        }
    }

    val shouldUseDynamicColorState = if (!isSettingsLoaded) dynamicColor else DynamicColorState.value

    val useDynamicColor = shouldUseDynamicColorState &&
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
        animationSpec = tween(durationMillis = 700),
        label = "ColorAnimation"
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