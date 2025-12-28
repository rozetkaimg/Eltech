package com.rozetka.epolitech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rozetka.presentation.theme.EPolitechTheme
import com.rozetka.presentation.ui.AppRoot
import com.rozetka.presentation.util.ThemeObject.showSplashScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            showSplashScreen.value
        }
        enableEdgeToEdge()
        setContent {
            EPolitechTheme {

                AppRoot(windowSizeClass = calculateWindowSizeClass(this))
            }
        }
    }


}
