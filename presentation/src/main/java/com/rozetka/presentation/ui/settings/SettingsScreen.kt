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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import coil.compose.AsyncImage
import com.rozetka.domain.util.StringObject
import com.rozetka.model.UserAccount
import com.rozetka.presentation.R
import com.rozetka.presentation.colors.GreenColor
import com.rozetka.presentation.colors.OrangeColor
import com.rozetka.presentation.colors.PinkColor
import com.rozetka.presentation.colors.PixelColor
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.new.CalendarOutline28
import com.rozetka.presentation.ui.settings.components.MonetItem
import com.rozetka.presentation.ui.settings.components.StaticColorItem
import com.rozetka.presentation.ui.settings.components.ThemeComponent
import com.rozetka.presentation.util.ThemeObject.DynamicColorState
import com.rozetka.presentation.util.generateColorFromHash
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountItem(
    account: UserAccount,
    onSwitch: () -> Unit,
    onRemove: () -> Unit
) {
    val cornerRadius by animateDpAsState(
        targetValue = if (account.isActive) 14.dp else 24.dp,
        animationSpec = tween(durationMillis = 300),
        label = "avatarShapeAnimation"
    )
    val avatarShape = RoundedCornerShape(cornerRadius)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSwitch)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (account.avatar.isNotEmpty()) {
            AsyncImage(
                model = account.avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(avatarShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(avatarShape)
                    .background(generateColorFromHash(account.name).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = generateColorFromHash(account.name)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name.ifEmpty { account.login },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (account.isActive) FontWeight.ExtraBold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (account.group.isNotEmpty()) {
                Text(
                    text = account.group,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (account.isActive) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            settingsViewModel.onNotificationPermissionResult(isGranted)
        }
    )

    data class ThemeColorConfig(
        val colorOne: Color,
        val colorTwo: Color,
        val colorThree: Color
    )

    val accounts by settingsViewModel.accounts.collectAsState()

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
                            contentDescription = null
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
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    if (StringObject.avatar.isNotEmpty())
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        generateColorFromHash(StringObject.Name).copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (StringObject.avatar.isNotEmpty()) {
                                AsyncImage(
                                    model = StringObject.avatar,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(32.dp))
                                )
                            } else {
                                Text(
                                    text = StringObject.Name.take(1).uppercase(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = generateColorFromHash(StringObject.Name)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = "${StringObject.SurName} ${StringObject.Name}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = StringObject.groupName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(Modifier.height(32.dp))

                        if (accounts.size > 1 || true) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "Аккаунты",
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    accounts.forEach { account ->
                                        AccountItem(
                                            account = account,
                                            onSwitch = {
                                                settingsViewModel.switchAccount(account) {}
                                            },
                                            onRemove = { settingsViewModel.removeAccount(account) }
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                navController.navigate(Screen.AddAccount.route)
                                            }
                                            .padding(horizontal = 24.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Text(
                                            text = "Добавить аккаунт",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                settingsViewModel.logout { isSwitched ->
                                    if (!isSwitched) {
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.Exit),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
                        { settingsViewModel.setThemeState(2) }
                    )
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
                            .padding(vertical = 16.dp)
                    ) {
                        val themeConfigs = remember {
                            listOf(
                                ThemeColorConfig(
                                    colorOne = PixelColor().oneColor,
                                    colorTwo = PixelColor().twoColor,
                                    colorThree = PixelColor().threeColor
                                ),
                                ThemeColorConfig(
                                    colorOne = GreenColor().oneColor,
                                    colorTwo = GreenColor().twoColor,
                                    colorThree = GreenColor().threeColor
                                ),
                                ThemeColorConfig(
                                    colorOne = OrangeColor().oneColor,
                                    colorTwo = OrangeColor().twoColor,
                                    colorThree = OrangeColor().threeColor
                                ),
                                ThemeColorConfig(
                                    colorOne = PinkColor().oneColor,
                                    colorTwo = PinkColor().twoColor,
                                    colorThree = PinkColor().threeColor
                                )
                            )
                        }
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(themeConfigs) { index, theme ->
                                StaticColorItem(
                                    modifier = Modifier.size(82.dp),
                                    onClick = { settingsViewModel.setThemeState(index) },
                                    position = index,
                                    colorOne = theme.colorOne,
                                    colorTwo = theme.colorTwo,
                                    colorThree = theme.colorThree
                                )
                            }
                        }
                    }
                }
            }

            item {
                var isEnabled by remember { mutableStateOf(settingsViewModel.getScheduleState()) }
                Spacer(Modifier.size(26.dp))
                ModernSettingsItemSwitch(
                    icon = CalendarOutline28,
                    iconBackgroundColor = generateColorFromHash(stringResource(R.string.schedule_view_type)),
                    title = stringResource(R.string.schedule_view_type),
                    subtitle = if (isEnabled) stringResource(R.string.week_view) else stringResource(R.string.day_view),
                    position = ItemPosition.TOP,
                    checked = isEnabled,
                    onCheckedChange = { newState ->
                        isEnabled = newState
                        settingsViewModel.saveScheduleState(newState)
                    }
                )
            }

            item {
                val isScheduleEnabled by settingsViewModel.notificationState.collectAsState()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ModernSettingsItemSwitch(
                        icon = ImageVector.vectorResource(R.drawable.notifications_28),
                        iconBackgroundColor = generateColorFromHash(stringResource(R.string.live_notification_title)),
                        title = stringResource(R.string.live_notification_title),
                        subtitle = stringResource(R.string.live_notification_subtitle),
                        position = ItemPosition.MIDDLE,
                        checked = isScheduleEnabled,
                        onCheckedChange = { newState ->
                            if (newState) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                                    settingsViewModel.saveScheduleNotificationState(true)
                                }
                            } else {
                                settingsViewModel.saveScheduleNotificationState(false)
                            }
                        }
                    )
                }
            }

            item {
                val isChatEnabled by settingsViewModel.chatNotificationState.collectAsState()

                ModernSettingsItemSwitch(
                    icon = ImageVector.vectorResource(R.drawable.notifications_28),
                    iconBackgroundColor = generateColorFromHash(stringResource(R.string.chat_notification_title)),
                    title = stringResource(R.string.chat_notification_title),
                    subtitle = stringResource(R.string.chat_notification_subtitle),
                    position = ItemPosition.MIDDLE,
                    checked = isChatEnabled,
                    onCheckedChange = { newState ->
                        if (newState) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                when (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )) {
                                    PackageManager.PERMISSION_GRANTED -> {
                                        settingsViewModel.saveChatNotificationState(true)
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            } else {
                                settingsViewModel.saveChatNotificationState(true)
                            }
                        } else {
                            settingsViewModel.saveChatNotificationState(false)
                        }
                    }
                )
            }

            item {
                var isEnabled by remember { mutableStateOf(settingsViewModel.getNavBar()) }
                ModernSettingsItemSwitch(
                    icon = ImageVector.vectorResource(R.drawable.outline_bottom_navigation_24),
                    iconBackgroundColor = generateColorFromHash("Панель навигации"),
                    title = "Панель навигации",
                    subtitle = if (isEnabled) "Экспериментальная" else "Традиционная",
                    position = ItemPosition.BOTTOM,
                    checked = isEnabled,
                    onCheckedChange = { newState ->
                        isEnabled = newState
                        settingsViewModel.setNavBar(newState)
                    }
                )
                Spacer(Modifier.size(26.dp))
            }

            item {
                Spacer(Modifier.size(2.dp))
                ModernSettingsItem(
                    icon = Icons.Outlined.Info,
                    iconBackgroundColor = generateColorFromHash(stringResource(R.string.about_app)),
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
                    iconBackgroundColor = (generateColorFromHash(stringResource(R.string.check_updates))),
                    title = stringResource(R.string.check_updates),
                    subtitle = stringResource(R.string.check_updates_subtitle),
                    position = ItemPosition.BOTTOM,
                    onClick = {
                        settingsViewModel.checkForUpdate()
                    }
                )
            }

            item {
                Spacer(Modifier.size(paddingValues.calculateBottomPadding()))
            }
        }
    }
}