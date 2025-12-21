package com.rozetka.presentation.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.rozetka.domain.util.StringObject
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.navigation.bottomNavItems
import com.rozetka.presentation.util.ThemeObject.NavBarType
import com.rozetka.presentation.util.getNavigationBarHeightDp

@Composable
fun AppBottomNavigationBar(
    bottomBarItems: List<Screen>,
    currentScreen: Screen?,
    navController: NavHostController
) {
    if(NavBarType.value){
        AppBottomNavigationBarNew(bottomBarItems,  currentScreen, navController)
    }
    else {
        AppBottomNavigationBarOld(bottomBarItems,  currentScreen, navController)
    }
}

@Preview
@Composable
fun AppBottomNavigationBarPreview() {
    val navController = rememberNavController()
    AppBottomNavigationBar(
        bottomBarItems = bottomNavItems,
        currentScreen = Screen.Home,
        navController = navController
    )
}


@Composable
fun AppBottomNavigationBarNew(
    bottomBarItems: List<Screen>,
    currentScreen: Screen?,
    navController: NavHostController
) {
    Box(Modifier.fillMaxWidth()) {
        Row(Modifier.align(Alignment.BottomCenter)) {
            Card(
                modifier = Modifier.padding(
                    end = 8.dp
                ),
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                LazyRow(Modifier.padding(8.dp)) {
                    items(3) { index ->
                        val screen = bottomBarItems[index]
                        val isSelected = currentScreen?.route == screen.route ||
                                currentScreen?.parentRoute == screen.route

                        ShortNavigationBarItem(
                            iconPosition = NavigationItemIconPosition.Start,
                            modifier = Modifier.height(40.dp),
                            selected = isSelected,
                            onClick = {
                                    navigateToTab(navController, screen.route)

                            },
                            icon = {

                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = stringResource(screen.titleResId)
                                    )
                                }

                            },
                            label = {
                                Box(modifier = Modifier.height(32.dp)) {
                                    Text(
                                        stringResource(screen.titleResId),
                                        modifier = Modifier.align(
                                            Alignment.Center
                                        ),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                            }
                        )
                    }
                }
            }
            Box {
                val screen = bottomBarItems[3]
                val isSelected = currentScreen?.route == screen.route ||
                        currentScreen?.parentRoute == screen.route
                Card(
                    modifier = Modifier
                        .padding(bottom = getNavigationBarHeightDp() + 20.dp)
                        .size(58.dp),
                    shape = RoundedCornerShape(100),

                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 10.dp
                    )
                ) {


                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(onClick = {

                                    navigateToTab(navController, screen.route)

                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = stringResource(screen.titleResId),
                            tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun GuestBottomNavigationBar(
    currentScreen: Screen?,
    navController: NavHostController
) {
    val guestItems = listOf(
        Screen.ScheduleLink,
        Screen.Login
    )

    Box(Modifier.fillMaxWidth()) {
        Row(Modifier.align(Alignment.BottomCenter)) {
            Card(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp + getNavigationBarHeightDp()),
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                LazyRow(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    items(guestItems.size) { index ->
                        val screen = guestItems[index]

                        val isSelected = currentScreen?.route == screen.route ||
                                currentScreen?.parentRoute == screen.route ||
                                (screen == Screen.ScheduleLink && currentScreen?.route == Screen.GuestGroupInput.route) ||
                                (screen == Screen.ScheduleLink && currentScreen?.route?.startsWith(Screen.Schedule.route) == true)

                        val titleRes = if (screen == Screen.Login) com.rozetka.presentation. R.string.login_hint else screen.titleResId
                        val iconVec = if (screen == Screen.Login) Icons.Default.Login else screen.icon

                        ShortNavigationBarItem(
                            iconPosition = NavigationItemIconPosition.Start,
                            modifier = Modifier.height(40.dp),
                            selected = isSelected,
                            onClick = {
                                if (screen == Screen.Login) {
                                    if (currentScreen?.route != Screen.Login.route) {
                                        navController.navigate(Screen.Login.route) {

                                            launchSingleTop = true
                                        }
                                    }
                                } else {

                                    val targetRoute = if (StringObject.groupName.isNotBlank()) {
                                        Screen.ScheduleLink.route + "/${StringObject.groupName}"
                                    } else {
                                        Screen.GuestGroupInput.route
                                    }
                                    if (currentScreen?.route != targetRoute) {
                                        navController.navigate(targetRoute) {
                                            popUpTo(0) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                            icon = {
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    Icon(
                                        imageVector = iconVec,
                                        contentDescription = stringResource(titleRes)
                                    )
                                }
                            },
                            label = {
                                Box(modifier = Modifier.height(32.dp)) {
                                    Text(
                                        text = if (screen == Screen.Login) "Войти" else stringResource(titleRes),
                                        modifier = Modifier.align(Alignment.Center),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
@Preview
@Composable
fun AppBottomNavigationBarNewPreview() {
    val navController = rememberNavController()
    AppBottomNavigationBarNew(
        bottomBarItems = bottomNavItems,
        currentScreen = Screen.Home,
        navController = navController
    )
}

@Composable
fun AppBottomNavigationBarOld(
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

                        navigateToTab(navController, screen.route)

                }
            )
        }
    }
}

@Preview
@Composable
fun AppBottomNavigationBarOldPreview() {
    val navController = rememberNavController()
    AppBottomNavigationBarOld(
        bottomBarItems = bottomNavItems,
        currentScreen = Screen.Home,
        navController = navController
    )
}

fun navigateToTabs(navController: NavHostController, route: String) {
    navController.navigate(route) {
        navController.graph.startDestinationRoute?.let { screen_route ->
            popUpTo(screen_route) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
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

@Preview
@Composable
fun AppNavigationRailPreview() {
    val navController = rememberNavController()
    AppNavigationRail(
        bottomBarItems = bottomNavItems,
        currentScreen = Screen.Home,
        navController = navController
    )
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
