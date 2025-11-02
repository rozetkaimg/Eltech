package com.rozetka.presentation.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.rozetka.domain.util.StringObject
import com.rozetka.presentation.R
import com.rozetka.presentation.colors.GreenColor
import com.rozetka.presentation.colors.OrangeColor
import com.rozetka.presentation.colors.PinkColor
import com.rozetka.presentation.colors.PixelColor
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.new.CalendarOutline28
import com.rozetka.presentation.ui.settings.components.MonetItem
import com.rozetka.presentation.ui.settings.components.ProfileCard
import com.rozetka.presentation.ui.settings.components.StaticColorItem
import com.rozetka.presentation.ui.settings.components.ThemeComponent
import com.rozetka.presentation.util.ThemeObject.DynamicColorState
import org.koin.androidx.compose.koinViewModel

enum class ItemPosition {
    TOP, MIDDLE, BOTTOM, STANDALONE
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = koinViewModel(),
    navController: NavController
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            settingsViewModel.onNotificationPermissionResult(isGranted)
        }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow

                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),

                    ) {

                    Column {

                        ProfileCard(
                            StringObject.avatar,
                            {},
                            "${
                                StringObject.SurName
                            } ${StringObject.Name}",
                            StringObject.groupName
                        )


                        Button(
                            {}, modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {

                            Text(stringResource(R.string.Exit))
                        }

                    }



                    Spacer(Modifier.size(16.dp))
                }
                Spacer(Modifier.size(24.dp))
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp,
                        bottomStart = 8.dp,
                        bottomEnd = 8.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ThemeComponent(
                        { settingsViewModel.setThemeState(0) },
                        { settingsViewModel.setThemeState(1) },
                        { settingsViewModel.setThemeState(2) })
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    val targetWidth = if (!DynamicColorState.value) 8.dp else 28.dp
                    val animatedWidth by animateDpAsState(
                        targetValue = targetWidth,
                        animationSpec = tween(durationMillis = 250)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(
                            bottomStart = animatedWidth,
                            bottomEnd = animatedWidth,
                            topStart = 8.dp,
                            topEnd = 8.dp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Box(Modifier.weight(1f)) {
                                MonetItem(
                                    true,
                                    stringResource(R.string.monet),
                                    {
                                        DynamicColorState.value = true
                                        settingsViewModel.setMonetState(true)
                                    },
                                    dynamicLightColorScheme(LocalContext.current).secondary,
                                    dynamicDarkColorScheme(LocalContext.current).primary,
                                    dynamicLightColorScheme(LocalContext.current).primaryContainer
                                )
                            }
                            Spacer(Modifier.size(8.dp))
                            Box(Modifier.weight(1f)) {
                                MonetItem(
                                    false,
                                    stringResource(R.string.static_theme),
                                    {
                                        DynamicColorState.value = false
                                        settingsViewModel.setMonetState(false)
                                    },
                                    lightColorScheme().secondary,
                                    darkColorScheme().primary,
                                    lightColorScheme().primaryContainer
                                )
                            }
                        }
                    }
                }
            }




            item {
                val targetWidth = if (!DynamicColorState.value) 120.dp else 0.dp
                val animatedWidth by animateDpAsState(
                    targetValue = targetWidth,
                    animationSpec = tween(
                        durationMillis = 250,
                        delayMillis = 500,
                        easing = LinearEasing
                    )
                )
                val targetRounded = if (!DynamicColorState.value) 8.dp else 28.dp
                val animatedRounded by animateDpAsState(
                    targetValue = targetRounded,
                    animationSpec = tween(
                        durationMillis = 250,
                        delayMillis = 500,
                        easing = LinearEasing
                    )
                )

                val targetAlpha = if (!DynamicColorState.value) 1f else 0f
                val animatedAlpha by animateFloatAsState(
                    targetValue = targetAlpha,
                    animationSpec = tween(
                        durationMillis = 250,
                        delayMillis = 500,
                        easing = LinearEasing
                    )

                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(
                        bottomStart = 28.dp,
                        bottomEnd = 28.dp,
                        topStart = animatedRounded,
                        topEnd = animatedRounded
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animatedWidth)
                        .graphicsLayer(alpha = animatedAlpha)

                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(Modifier.align(Alignment.TopCenter)) {
                            StaticColorItem(
                                { settingsViewModel.setThemeState(0) },
                                0,
                                PixelColor().oneColor,
                                PixelColor().twoColor,
                                PixelColor().threeColor
                            )
                            Spacer(Modifier.size(4.dp))
                            StaticColorItem(
                                { settingsViewModel.setThemeState(1) },
                                1,
                                GreenColor().oneColor,
                                GreenColor().twoColor,
                                GreenColor().threeColor
                            )
                            Spacer(Modifier.size(4.dp))
                            StaticColorItem(
                                { settingsViewModel.setThemeState(2) },
                                2,
                                OrangeColor().oneColor,
                                OrangeColor().twoColor,
                                OrangeColor().threeColor
                            )
                            Spacer(Modifier.size(4.dp))
                            StaticColorItem(
                                { settingsViewModel.setThemeState(3) },
                                3,
                                PinkColor().oneColor,
                                PinkColor().twoColor,
                                PinkColor().threeColor
                            )
                        }
                    }
                }

            }

            item {
                var isEnabled by remember { mutableStateOf(settingsViewModel.getScheduleState()) }
                Spacer(Modifier.size(26.dp))
                ModernSettingsItemSwitch(
                    icon = CalendarOutline28,
                    iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    title = stringResource(R.string.schedule_view_type),
                    subtitle = if (isEnabled) stringResource(R.string.week_view) else stringResource(R.string.day_view),
                    position = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                        ItemPosition.TOP
                    } else ItemPosition.STANDALONE,
                    checked = isEnabled,
                    onCheckedChange = { newState ->
                        isEnabled = newState
                        settingsViewModel.saveScheduleState(newState)
                    }
                )
            }
            item {
                val isEnabled by settingsViewModel.notificationState.collectAsState()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                    ModernSettingsItemSwitch(
                        icon = ImageVector.vectorResource(R.drawable.notifications_28),
                        iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        title = stringResource(R.string.live_notification_title),
                        subtitle = stringResource(R.string.live_notification_subtitle),
                        position = ItemPosition.BOTTOM,
                        checked = isEnabled,
                        onCheckedChange = { newState ->
                            if (newState) {
                                when (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )) {
                                    PackageManager.PERMISSION_GRANTED -> {
                                        settingsViewModel.saveScheduleNotificationState(true)
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            } else {
                                settingsViewModel.saveScheduleNotificationState(false)
                            }
                        }
                    )
                }
                Spacer(Modifier.size(26.dp))
            }

            item {
                Spacer(Modifier.size(2.dp))
                ModernSettingsItem(
                    icon = Icons.Outlined.Info,
                    iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    title = stringResource(R.string.about_app),
                    subtitle = stringResource(R.string.about_app_subtitle),
                    position = ItemPosition.TOP,
                    onClick = {
                        navController.navigate(Screen.AboutApplication.route)
                    }
                )
            }

            item {
                ModernSettingsItem(
                    icon = Icons.Outlined.SystemUpdate,
                    iconBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    title = stringResource(R.string.check_updates),
                    subtitle = stringResource(R.string.check_updates_subtitle),
                    position = ItemPosition.BOTTOM,
                    onClick = {
                        settingsViewModel.checkForUpdate()
                    }
                )
            }
            item {
                Spacer(Modifier.size(paddingValues.calculateBottomPadding() ))
            }
        }
    }
}