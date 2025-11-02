package com.rozetka.presentation.ui.main

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.rozetka.presentation.navigation.Screen
import com.rozetka.domain.util.StringObject


@Composable
fun AppBottomNavigationBar(
    bottomBarItems: List<Screen>,
    currentScreen: Screen?,
    navController: NavHostController
) {
    NavigationBar {
        bottomBarItems.forEach { screen ->
            val isSelected =
                currentScreen?.route == screen.route || currentScreen?.parentRoute == screen.route

            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = stringResource(screen.titleResId)
                    )
                },
                label = { Text(stringResource(screen.titleResId)) },
                selected = isSelected,
                onClick = {
                    if (screen.route == Screen.Schedule.route) {
                        navigateToTab(navController,Screen.Schedule.route + "/${StringObject.groupName}")
                    } else {
                        navigateToTab(navController, screen.route)
                    }
                }
            )
        }
    }
}

@Composable
fun AppNavigationRail(
    bottomBarItems: List<Screen>,
    currentScreen: Screen?,
    navController: NavHostController
) {
    NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        bottomBarItems.forEach { screen ->
            val isSelected =
                currentScreen?.route == screen.route || currentScreen?.parentRoute == screen.route
            NavigationRailItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = stringResource(screen.titleResId)
                    )
                },
                label = { Text(stringResource(screen.titleResId)) },
                selected = isSelected,
                onClick = { navigateToTab(navController, screen.route) }
            )
        }
    }
}


private fun navigateToTab(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
