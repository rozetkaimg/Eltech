package com.rozetka.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rozetka.presentation.util.ThemeObject.bottomNavBarPaddingValue

class UiSize {

@Composable
    fun getNavBarPaddingSize(): Dp  = getNavigationBarHeightDp() + bottomNavBarPaddingValue.dp + 8.dp


}