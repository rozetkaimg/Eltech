package com.rozetka.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rozetka.domain.util.StringObject
import com.rozetka.presentation.ui.login.LoginScreen
import com.rozetka.presentation.ui.main.MainScreen
import com.rozetka.presentation.ui.pay.LoadingState
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppRoot(
    windowSizeClass: WindowSizeClass,
    viewModel: RootViewModel = koinViewModel()
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    if (startDestination.isNotEmpty()) {
        NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.background(
            MaterialTheme.colorScheme.surface)) {

            composable(RootViewModel.LOGIN_ROUTE) {
                LoginScreen(
                    onLoginSuccess = {
                        StringObject.isGuest = false
                        navController.navigate(RootViewModel.MAIN_ROUTE) {
                            popUpTo(RootViewModel.LOGIN_ROUTE) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(RootViewModel.MAIN_ROUTE) {
                MainScreen(
                    windowSizeClass = windowSizeClass
                )
            }
        }
    } else {
        LoadingState()
    }
}