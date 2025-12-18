package com.rozetka.presentation.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

abstract class BaseColorTheme {

    abstract val oneColor: Color
    abstract val twoColor: Color
    abstract val threeColor: Color

    protected abstract val light: ThemeColors
    protected abstract val dark: ThemeColors

    protected data class ThemeColors(
        val primary: Color, val onPrimary: Color,
        val primaryContainer: Color, val onPrimaryContainer: Color,
        val secondary: Color, val onSecondary: Color,
        val secondaryContainer: Color, val onSecondaryContainer: Color,
        val tertiary: Color, val onTertiary: Color,
        val tertiaryContainer: Color, val onTertiaryContainer: Color,
        val error: Color, val onError: Color,
        val errorContainer: Color, val onErrorContainer: Color,
        val background: Color, val onBackground: Color,
        val surface: Color, val onSurface: Color,
        val surfaceVariant: Color, val onSurfaceVariant: Color,
        val outline: Color, val outlineVariant: Color,
        val scrim: Color, val inverseSurface: Color,
        val inverseOnSurface: Color, val inversePrimary: Color,
        val surfaceDim: Color, val surfaceBright: Color,
        val surfaceContainerLowest: Color, val surfaceContainerLow: Color,
        val surfaceContainer: Color, val surfaceContainerHigh: Color,
        val surfaceContainerHighest: Color,
    )

    val lightScheme: ColorScheme by lazy {
        lightColorScheme(
            primary = light.primary,
            onPrimary = light.onPrimary,
            primaryContainer = light.primaryContainer,
            onPrimaryContainer = light.onPrimaryContainer,
            secondary = light.secondary,
            onSecondary = light.onSecondary,
            secondaryContainer = light.secondaryContainer,
            onSecondaryContainer = light.onSecondaryContainer,
            tertiary = light.tertiary,
            onTertiary = light.onTertiary,
            tertiaryContainer = light.tertiaryContainer,
            onTertiaryContainer = light.onTertiaryContainer,
            error = light.error,
            onError = light.onError,
            errorContainer = light.errorContainer,
            onErrorContainer = light.onErrorContainer,
            background = light.background,
            onBackground = light.onBackground,
            surface = light.surface,
            onSurface = light.onSurface,
            surfaceVariant = light.surfaceVariant,
            onSurfaceVariant = light.onSurfaceVariant,
            outline = light.outline,
            outlineVariant = light.outlineVariant,
            scrim = light.scrim,
            inverseSurface = light.inverseSurface,
            inverseOnSurface = light.inverseOnSurface,
            inversePrimary = light.inversePrimary,
            surfaceDim = light.surfaceDim,
            surfaceBright = light.surfaceBright,
            surfaceContainerLowest = light.surfaceContainerLowest,
            surfaceContainerLow = light.surfaceContainerLow,
            surfaceContainer = light.surfaceContainer,
            surfaceContainerHigh = light.surfaceContainerHigh,
            surfaceContainerHighest = light.surfaceContainerHighest
        )
    }

    val darkScheme: ColorScheme by lazy {
        darkColorScheme(
            primary = dark.primary,
            onPrimary = dark.onPrimary,
            primaryContainer = dark.primaryContainer,
            onPrimaryContainer = dark.onPrimaryContainer,
            secondary = dark.secondary,
            onSecondary = dark.onSecondary,
            secondaryContainer = dark.secondaryContainer,
            onSecondaryContainer = dark.onSecondaryContainer,
            tertiary = dark.tertiary,
            onTertiary = dark.onTertiary,
            tertiaryContainer = dark.tertiaryContainer,
            onTertiaryContainer = dark.onTertiaryContainer,
            error = dark.error,
            onError = dark.onError,
            errorContainer = dark.errorContainer,
            onErrorContainer = dark.onErrorContainer,
            background = dark.background,
            onBackground = dark.onBackground,
            surface = dark.surface,
            onSurface = dark.onSurface,
            surfaceVariant = dark.surfaceVariant,
            onSurfaceVariant = dark.onSurfaceVariant,
            outline = dark.outline,
            outlineVariant = dark.outlineVariant,
            scrim = dark.scrim,
            inverseSurface = dark.inverseSurface,
            inverseOnSurface = dark.inverseOnSurface,
            inversePrimary = dark.inversePrimary,
            surfaceDim = dark.surfaceDim,
            surfaceBright = dark.surfaceBright,
            surfaceContainerLowest = dark.surfaceContainerLowest,
            surfaceContainerLow = dark.surfaceContainerLow,
            surfaceContainer = dark.surfaceContainer,
            surfaceContainerHigh = dark.surfaceContainerHigh,
            surfaceContainerHighest = dark.surfaceContainerHighest
        )
    }
}