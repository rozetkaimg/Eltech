package com.rozetka.epolitech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.rozetka.presentation.theme.EPolitechTheme
import com.rozetka.presentation.ui.AppRoot

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EPolitechTheme {
                    val windowSizeClass = calculateWindowSizeClass(this)
                    AppRoot(windowSizeClass = windowSizeClass)
            }
        }
    }


}
