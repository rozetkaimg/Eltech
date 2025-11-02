package com.rozetka.presentation.ui.main

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rozetka.presentation.navigation.AppNavHost
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.navigation.bottomNavItems


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()

    val allScreens = listOf(
        Screen.Schedule,
        Screen.Mail,
        Screen.Profile,
        Screen.Service,
        Screen.Settings,
        Screen.Payment,
        Screen.Home,
        Screen.Dialo,
        Screen.AcademicPerScreen,
        Screen.DigitalService,
        Screen.PhysEdJournalScreen,
        Screen.SearchGroupScreen,
        Screen.AboutApplication,
        Screen.ProjectActivity,
        Screen.Maps
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination


    val currentScreen = allScreens.find { screen ->
        currentDestination?.route?.startsWith(screen.route.substringBefore("/")) == true
    }

    val isNavigationVisible = currentScreen?.isFullScreen == true
    val useNavRail = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact

    if (useNavRail) {
        TabletLayout(
            navController = navController,
            bottomBarItems = bottomNavItems,
            currentScreen = currentScreen,
            isNavigationVisible = isNavigationVisible,
            windowSizeClass = windowSizeClass
        )
    } else {
        PhoneLayout(
            navController = navController,
            bottomBarItems = bottomNavItems,
            currentScreen = currentScreen,
            isNavigationVisible = isNavigationVisible,
            windowSizeClass = windowSizeClass
        )
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun PhoneLayout(
    navController: NavHostController,
    bottomBarItems: List<Screen>,
    currentScreen: Screen?,
    isNavigationVisible: Boolean,
    windowSizeClass: WindowSizeClass
) {
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = !(currentScreen?.isFullScreen == true),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                AppBottomNavigationBar(
                    bottomBarItems = bottomBarItems,
                    currentScreen = currentScreen,
                    navController = navController
                )
            }
        }
    ) {
        AppNavHost(
            navController = navController,
            modifier = Modifier,
            windowSizeClass = windowSizeClass
        )
    }
}


@Composable
private fun TabletLayout(
    navController: NavHostController,
    bottomBarItems: List<Screen>,
    currentScreen: Screen?,
    isNavigationVisible: Boolean,
    windowSizeClass: WindowSizeClass
) {
    Row(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = isNavigationVisible) {
            AppNavigationRail(
                bottomBarItems = bottomBarItems,
                currentScreen = currentScreen,
                navController = navController
            )
        }
        AppNavHost(
            navController = navController,
            modifier = Modifier.weight(1f),
            windowSizeClass = windowSizeClass
        )
    }
}