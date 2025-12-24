package com.rozetka.presentation.ui.sessionSchedule

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rozetka.model.ScheduleModel
import com.rozetka.model.local.CalendarAccount
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.getNavigationBarHeightDp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

data class NotificationState(
    val message: String,
    val type: NotificationType = NotificationType.SUCCESS
)

enum class NotificationType { SUCCESS, ERROR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScheduleScreen(
    navController: NavHostController,
    groupName: String,
    viewModel: SessionScheduleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }
    var availableCalendars by remember { mutableStateOf<List<CalendarAccount>>(emptyList()) }
    var notification by remember { mutableStateOf<NotificationState?>(null) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        viewModel.calendarEffect.collect { effect ->
            when (effect) {
                is CalendarEffect.ShowNotification -> {
                    notification = NotificationState(effect.message, effect.type)
                    kotlinx.coroutines.delay(3000)
                    notification = null
                }
                is CalendarEffect.ShowCalendarSelection -> {
                    availableCalendars = effect.calendars
                    showBottomSheet = true
                }
            }
        }
    }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.WRITE_CALENDAR] == true &&
                permissions[Manifest.permission.READ_CALENDAR] == true
        if (granted) {
            viewModel.onExportClicked()
        } else {
            notification = NotificationState("Нужен доступ к календарю", NotificationType.ERROR)
        }
    }

    LaunchedEffect(groupName) {
        viewModel.getSessionSchedule(groupName)
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Выберите календарь",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn {
                    items(availableCalendars) { calendar ->
                        ListItem(
                            leadingContent = {
                                CalendarAvatar(name = calendar.displayName)
                            },
                            headlineContent = { Text(calendar.displayName, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(calendar.accountName, fontSize = 12.sp) },
                            modifier = Modifier
                                .clickable {
                                    scope.launch {
                                        sheetState.hide()
                                    }.invokeOnCompletion {
                                        showBottomSheet = false
                                        viewModel.exportToCalendar(calendar.id)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.session_schedule_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (uiState is SessionScheduleUiState.Success) {
                        IconButton(onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.WRITE_CALENDAR
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                viewModel.onExportClicked()
                            } else {
                                calendarPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CALENDAR,
                                        Manifest.permission.WRITE_CALENDAR
                                    )
                                )
                            }
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = com.rozetka.presentation.R.drawable.list_arrow_left_down_outline_24),
                                contentDescription = "Export All"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                color = MaterialTheme.colorScheme.surface
            ) {
                when (val state = uiState) {
                    is SessionScheduleUiState.Loading -> LoadingState()
                    is SessionScheduleUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = state.message)
                        }
                    }
                    is SessionScheduleUiState.Success -> {
                        SessionContent(state.data, navController)
                    }
                    else -> {}
                }
            }

            TopNotification(
                notification = notification,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = paddingValues.calculateTopPadding() + 8.dp)
            )
        }
    }
}

@Composable
fun TopNotification(
    notification: NotificationState?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier.padding(horizontal = 16.dp).fillMaxWidth()
    ) {
        notification?.let {
            val backgroundColor = if (it.type == NotificationType.SUCCESS)
                Color(0xFFE6F4EA) else Color(0xFFFDE7E9)
            val contentColor = if (it.type == NotificationType.SUCCESS)
                Color(0xFF1E8E3E) else Color(0xFFD93025)
            val icon = if (it.type == NotificationType.SUCCESS)
                Icons.Default.CheckCircle else Icons.Default.Warning

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = it.message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarAvatar(name: String) {
    val firstChar = name.firstOrNull()?.uppercase() ?: "?"

    val color = remember(name) {
        val colors = listOf(
            Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC),
            Color(0xFF7E57C2), Color(0xFF5C6BC0), Color(0xFF42A5F5),
            Color(0xFF26A69A), Color(0xFF66BB6A), Color(0xFF9CCC65),
            Color(0xFFD4E157), Color(0xFFFFCA28), Color(0xFFFF7043)
        )
        colors[name.hashCode().absoluteValue % colors.size]
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = firstChar,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SessionContent(schedule: ScheduleModel, navController: NavHostController) {
    val context = LocalContext.current
    val dayNameFormatter = remember { DateTimeFormatter.ofPattern("EEEE", Locale("ru")) }
    val dateNumFormatter = remember { DateTimeFormatter.ofPattern("dd.MM", Locale("ru")) }

    val sortedDays = remember(schedule) {
        schedule.grid.entries
            .mapNotNull { entry ->
                try {
                    LocalDate.parse(entry.key) to entry.value
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.first }
    }

    val lastEntry = sortedDays.lastOrNull()
    val lastSessionDate = lastEntry?.first
    val lastDayHasLessons = lastEntry?.second?.values?.flatten()?.isNotEmpty() == true

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        sortedDays.forEach { (date, timeMap) ->
            item(key = "header_${date}") {
                val dayName = date.format(dayNameFormatter)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
                val formattedDate = date.format(dateNumFormatter)

                Text(
                    text = "$dayName, $formattedDate",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
            }

            val dayLessons = timeMap.flatMap { (timeKey, lessons) ->
                lessons.map { it to timeKey }
            }

            if (dayLessons.isEmpty()) {
                if (date != lastSessionDate) {
                    item(key = "empty_${date}") {
                        EmptyStateCard()
                    }
                }
            } else {
                itemsIndexed(dayLessons) { index, (lesson, timeKey) ->
                    SessionLessonItem(
                        lesson = lesson,
                        lessonNumber = timeKey,
                        index = index,
                        totalLessonsInDay = dayLessons.size,
                        lessonDate = date,
                        onLessonClick = {
                            if (it.teacher.isNotBlank()) {
                                navController.navigate("teacherSchedule/${it.teacher}")
                            }
                        },
                        onExportToCalendar = { title, desc, loc, start, end ->
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, title)
                                putExtra(CalendarContract.Events.DESCRIPTION, desc)
                                putExtra(CalendarContract.Events.EVENT_LOCATION, loc)
                                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        if (lastSessionDate != null) {
            item(key = "holidays_block") {
                val holidaysStartDate = if (lastDayHasLessons) {
                    lastSessionDate.plusDays(1)
                } else {
                    lastSessionDate
                }

                val formattedDate = holidaysStartDate.format(dateNumFormatter)

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(Cookie9Sided.toShape())
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(32.dp).align(Alignment.Center)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text(
                                text = "Каникулы! 🎉",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Начинаются с $formattedDate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(getNavigationBarHeightDp() + 80.dp + 16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    painter = painterResource(R.drawable.book_spread_outline_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp).align(Alignment.Center)
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterVertically)) {
                Text(
                    text = "Выходной",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Время подготовки к экзаменам",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}