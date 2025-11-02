package com.rozetka.presentation.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

object ThemeObject {


        val DynamicColorState: MutableState<Boolean> = mutableStateOf(true)
        val StatusBarIconColor: MutableState<Boolean> = mutableStateOf(false)
        val DarkThemeState: MutableState<Int> = mutableIntStateOf(0)
        val ColorThemeState: MutableState<Int> = mutableIntStateOf(0)
        const val BottomNavBarPaddingValue: Int = 80

}