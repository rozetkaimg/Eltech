package com.rozetka.presentation.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

fun removeEmojis(text: String): String {

    val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+|[\\u2600-\\u27BF]+|[\\uD83E\uDD00-\\uD83E\uDFFF]+")

    return emojiRegex.replace(text, "")
}
@Composable
fun getNavigationBarHeightDp(): Dp {
    val navigationBarInset = WindowInsets.navigationBars
    val navigationBarHeightPx = navigationBarInset.getBottom(LocalDensity.current)
    return with(LocalDensity.current) {
        navigationBarHeightPx.toDp()
    }
}