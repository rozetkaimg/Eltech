package com.rozetka.epolitech

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rozetka.epolitech.weights.MessagePollingWorker
import com.rozetka.presentation.theme.EPolitechTheme
import com.rozetka.presentation.ui.AppRoot
import com.rozetka.presentation.util.ThemeObject.showSplashScreen

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupMessagePolling()

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

    private fun setupMessagePolling() {
        val secureStorage = com.rozetka.data.SecureStorage(this)
        if (!secureStorage.getChatNotificationState()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        MessagePollingWorker.schedule(this)
    }
}