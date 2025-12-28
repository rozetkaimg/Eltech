package com.rozetka.presentation.util

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

object ThemeObject {
    val showSplashScreen: MutableState<Boolean> = mutableStateOf(true)
    val DynamicColorState: MutableState<Boolean> = mutableStateOf(true)
    val StatusBarIconColor: MutableState<Boolean> = mutableStateOf(false)
    val NavBarType: MutableState<Boolean> = mutableStateOf(false)
    val DarkThemeState: MutableState<Int> = mutableIntStateOf(0)
    val ColorThemeState: MutableState<Int> = mutableIntStateOf(0)
    var bottomNavBarPaddingValue: Int = 80

}