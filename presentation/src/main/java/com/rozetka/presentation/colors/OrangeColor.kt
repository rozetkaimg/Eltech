package com.rozetka.presentation.colors

import androidx.compose.ui.graphics.Color

class OrangeColor : BaseColorTheme() {

    override val oneColor = Color(0xFF77574E)
    override val twoColor = Color(0xFF8F4C38)
    override val threeColor = Color(0xFFFFDBD1)

    override val light = ThemeColors(
        primary = Color(0xFF8F4C38), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBD1), onPrimaryContainer = Color(0xFF723523),
        secondary = Color(0xFF77574E), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDBD1), onSecondaryContainer = Color(0xFF5D4037),
        tertiary = Color(0xFF705575), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFAD8FD), onTertiaryContainer = Color(0xFF573E5C),
        error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF93000A),
        background = Color(0xFFFFF8F6), onBackground = Color(0xFF231917),
        surface = Color(0xFFFFF8F6), onSurface = Color(0xFF231917),
        surfaceVariant = Color(0xFFF5DED8), onSurfaceVariant = Color(0xFF53433F),
        outline = Color(0xFF85736E), outlineVariant = Color(0xFFD8C2BC),
        scrim = Color(0xFF000000), inverseSurface = Color(0xFF372E30),
        inverseOnSurface = Color(0xFFF1DFDA), inversePrimary = Color(0xFFFFB5A0),
        surfaceDim = Color(0xFFE6D6D9), surfaceBright = Color(0xFFFFF8F8),
        surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFFFF0F2),
        surfaceContainer = Color(0xFFFAEAED), surfaceContainerHigh = Color(0xFFF5E4E7),
        surfaceContainerHighest = Color(0xFFEFDFE1)
    )

    override val dark = ThemeColors(
        primary = Color(0xFFFFB5A0), onPrimary = Color(0xFF561F0F),
        primaryContainer = Color(0xFF723523), onPrimaryContainer = Color(0xFFFFDBD1),
        secondary = Color(0xFFE7BDB2), onSecondary = Color(0xFF442A22),
        secondaryContainer = Color(0xFF5D4037), onSecondaryContainer = Color(0xFFFFDBD1),
        tertiary = Color(0xFFDDBCE0), onTertiary = Color(0xFF3F2844),
        tertiaryContainer = Color(0xFF573E5C), onTertiaryContainer = Color(0xFFFAD8FD),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1A110F), onBackground = Color(0xFFF1DFDA),
        surface = Color(0xFF1A110F), onSurface = Color(0xFFF1DFDA),
        surfaceVariant = Color(0xFF53433F), onSurfaceVariant = Color(0xFFD8C2BC),
        outline = Color(0xFFA08C87), outlineVariant = Color(0xFF53433F),
        scrim = Color(0xFF000000), inverseSurface = Color(0xFFF1DFDA),
        inverseOnSurface = Color(0xFF372E30), inversePrimary = Color(0xFF8F4C38),
        surfaceDim = Color(0xFF191113), surfaceBright = Color(0xFF413739),
        surfaceContainerLowest = Color(0xFF140C0E), surfaceContainerLow = Color(0xFF22191C),
        surfaceContainer = Color(0xFF261D20), surfaceContainerHigh = Color(0xFF31282A),
        surfaceContainerHighest = Color(0xFF3C3235)
    )
}