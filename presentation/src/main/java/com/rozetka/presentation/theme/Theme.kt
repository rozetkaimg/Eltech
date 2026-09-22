package com.rozetka.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.rozetka.model.ColorConfig
import com.rozetka.model.ThemeConfig

private fun getStaticColorScheme(isDark: Boolean, colorConfig: ColorConfig): ColorScheme {
    return when (colorConfig) {
        ColorConfig.STANDARD -> if (isDark) darkColorScheme() else lightColorScheme()
        
        ColorConfig.GREEN -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFF90D978),
                onPrimary = Color(0xFF043900),
                primaryContainer = Color(0xFF1B3710),
                onPrimaryContainer = Color(0xFFB5F59A),
                secondary = Color(0xFFBDCBAF),
                onSecondary = Color(0xFF273420),
                secondaryContainer = Color(0xFF3D4A36),
                onSecondaryContainer = Color(0xFFD7E8CD),
                tertiary = Color(0xFFA0E0E1),
                onTertiary = Color(0xFF003738),
                tertiaryContainer = Color(0xFF004F51),
                onTertiaryContainer = Color(0xFFBCEBEB)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF386A20),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFB5F59A),
                onPrimaryContainer = Color(0xFF042100),
                secondary = Color(0xFF55624C),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFD7E8CD),
                onSecondaryContainer = Color(0xFF121F0B),
                tertiary = Color(0xFF19686A),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFA0F0F1),
                onTertiaryContainer = Color(0xFF002020)
            )
        }
        ColorConfig.BLUE -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFF95CCFF),
                onPrimary = Color(0xFF003353),
                primaryContainer = Color(0xFF004A76),
                onPrimaryContainer = Color(0xFFC6E7FF),
                secondary = Color(0xFFBAC8DB),
                onSecondary = Color(0xFF243240),
                secondaryContainer = Color(0xFF3A4857),
                onSecondaryContainer = Color(0xFFD6E4F7),
                tertiary = Color(0xFFD5BEE5),
                onTertiary = Color(0xFF3A2A48),
                tertiaryContainer = Color(0xFF533F5F),
                onTertiaryContainer = Color(0xFFF2DAFF)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF00639B),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFC6E7FF),
                onPrimaryContainer = Color(0xFF001D35),
                secondary = Color(0xFF526070),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFD6E4F7),
                onSecondaryContainer = Color(0xFF0E1D2A),
                tertiary = Color(0xFF6B5778),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFF2DAFF),
                onTertiaryContainer = Color(0xFF251431)
            )
        }
        ColorConfig.PURPLE -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFD0BCFF),
                onPrimary = Color(0xFF381E72),
                primaryContainer = Color(0xFF4F378B),
                onPrimaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFFCCC2DC),
                onSecondary = Color(0xFF332D41),
                secondaryContainer = Color(0xFF4A4458),
                onSecondaryContainer = Color(0xFFE8DEF8),
                tertiary = Color(0xFFEFB8C8),
                onTertiary = Color(0xFF492532),
                tertiaryContainer = Color(0xFF633B48),
                onTertiaryContainer = Color(0xFFFFD8E4)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF6750A4),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFEADDFF),
                onPrimaryContainer = Color(0xFF21005D),
                secondary = Color(0xFF625B71),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFE8DEF8),
                onSecondaryContainer = Color(0xFF1D192B),
                tertiary = Color(0xFF7D5260),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFFFD8E4),
                onTertiaryContainer = Color(0xFF31111D)
            )
        }
        ColorConfig.ORANGE -> if (isDark) {
            darkColorScheme(
                primary = Color(0xFFFFB870),
                onPrimary = Color(0xFF4B2800),
                primaryContainer = Color(0xFF6A3B00),
                onPrimaryContainer = Color(0xFFFFDDB3),
                secondary = Color(0xFFE5C18D),
                onSecondary = Color(0xFF432C1E),
                secondaryContainer = Color(0xFF5D4235),
                onSecondaryContainer = Color(0xFFFFDCBE),
                tertiary = Color(0xFFCFC890),
                onTertiary = Color(0xFF353107),
                tertiaryContainer = Color(0xFF4C481D),
                onTertiaryContainer = Color(0xFFEBE4AA)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF8B5000),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFDDB3),
                onPrimaryContainer = Color(0xFF2C1600),
                secondary = Color(0xFF765A4B),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFFFDCBE),
                onSecondaryContainer = Color(0xFF2A170C),
                tertiary = Color(0xFF646032),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFEBE4AA),
                onTertiaryContainer = Color(0xFF1F1C00)
            )
        }
        ColorConfig.DEFAULT -> if (isDark) darkColorScheme() else lightColorScheme()
    }
}

@Composable
fun EPolitechTheme(
    themeConfig: ThemeConfig = ThemeConfig.SYSTEM,
    colorConfig: ColorConfig = ColorConfig.DEFAULT,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val isDark = when (themeConfig) {
        ThemeConfig.SYSTEM -> isSystemInDarkTheme()
        ThemeConfig.LIGHT -> false
        ThemeConfig.DARK -> true
    }

    val useDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && colorConfig == ColorConfig.DEFAULT

    val targetColorScheme = when {
        useDynamicColor && isDark -> dynamicDarkColorScheme(context)
        useDynamicColor && !isDark -> dynamicLightColorScheme(context)
        else -> getStaticColorScheme(isDark, colorConfig)
    }

    val animatedColorScheme = targetColorScheme.switch()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = animatedColorScheme
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
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
