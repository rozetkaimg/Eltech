package com.rozetka.presentation.ui.shedule

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.rozetka.domain.util.StringObject
import com.rozetka.model.Lesson
import com.rozetka.model.ScheduleModel
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.new.CalendarOutline28
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.generateColorFromHash
import com.rozetka.presentation.util.getNavigationBarHeightDp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavHostController,
    viewModel: ScheduleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.schedule),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }

                    MaterialTheme(
                        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
                    ) {
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(12.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            offset = DpOffset(x = 0.dp, y = 8.dp),
                            modifier = Modifier.widthIn(min = 200.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.select_group),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.SearchGroupScreen.route)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                    leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.employees),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.Employees.route)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                    leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.session_schedule_title),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    navController.navigate("SessionSchedule/${StringObject.groupName}")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                    leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            when (val state = uiState) {
                is ScheduleUiState.Loading -> LoadingState()
                is ScheduleUiState.Error -> ExpressiveErrorState(
                    "Расписание недоступно",
                    { viewModel.getSchedule(StringObject.groupName) })

                is ScheduleUiState.Success -> {
                    if (state.data.isScheduleMissing) {
                        MissingScheduleView(
                            groupName = StringObject.groupName,
                            onNavigateToSession = {
                                navController.navigate("SessionSchedule/${StringObject.groupName}")
                            }
                        )
                    } else {
                        if (viewModel.getScheduleState()) {
                            WeekSchedule(state.data, paddingValues, navController)
                        } else {
                            DaySchedule(state.data, paddingValues) {
                                navController.navigate("teacherSchedule/${it}")
                            }
                        }
                    }
                }

                is ScheduleUiState.Initial -> {}
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekSchedule(
    state: ScheduleScreenData,
    paddingValues: PaddingValues,
    navController: NavHostController
) {
    val screenData = state
    val pagerState = rememberPagerState(
        initialPage = screenData.initialWeekIndex,
        pageCount = { screenData.weeks.size }
    )
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
    ) {
        SecondaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            screenData.weeks.forEachIndexed { index, weekInfo ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(text = weekInfo.label) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val selectedWeek = screenData.weeks[pageIndex]
            WeekScheduleContent(
                schedule = screenData.fullSchedule,
                week = selectedWeek,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DaySchedule(
    state: ScheduleScreenData,
    paddingValues: PaddingValues,
    onLinkClick: (String) -> Unit
) {
    val screenData = state
    val startDate = screenData.weeks.first().startDate
    val endDate = screenData.weeks.last().endDate
    val allDays =
        (0L until ChronoUnit.DAYS.between(startDate, endDate) + 1).map { startDate.plusDays(it) }

    val initialPage = allDays.indexOf(LocalDate.now())
    val todayIndex = if (initialPage == -1) 0 else initialPage

    val pagerState = rememberPagerState(
        initialPage = todayIndex,
        pageCount = { allDays.size }
    )
    val coroutineScope = rememberCoroutineScope()

    val dayOfWeekFormatter = remember {
        SimpleDateFormat("EEE", Locale.getDefault())
    }

    val showFab by remember {
        derivedStateOf {
            pagerState.currentPage != todayIndex && initialPage != -1
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            SecondaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val dateParser =
                    remember { java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd") }
                val today = LocalDate.now()
                val currentTime = java.time.LocalTime.now()

                allDays.forEachIndexed { index, date ->
                    val dayKey = date.dayOfWeek.value.toString()
                    val lessonsMap = screenData.fullSchedule.grid?.get(dayKey) ?: emptyMap()
                    val dotsData = remember(lessonsMap, date) {
                        lessonsMap.entries
                            .sortedBy { it.key }
                            .mapNotNull { entry ->
                                val slotKey = entry.key
                                val lessonsInSlot = entry.value
                                val activeLesson = lessonsInSlot.firstOrNull { lesson ->
                                    val name = lesson.sbj.lowercase()
                                    val dateFrom =
                                        runCatching { LocalDate.parse(lesson.df, dateParser) }.getOrNull()
                                    val dateTo =
                                        runCatching { LocalDate.parse(lesson.dt, dateParser) }.getOrNull()
                                    val isWithinRange = dateFrom != null && dateTo != null &&
                                            !date.isBefore(dateFrom) && !date.isAfter(dateTo)
                                    val isRealLesson = lesson.sbj.isNotBlank() &&
                                            !name.contains("перерыв") &&
                                            !name.contains("окно") &&
                                            !name.contains("window")

                                    isWithinRange && isRealLesson
                                }

                                if (activeLesson != null) {
                                    val isPassed = when {
                                        date.isBefore(today) -> true
                                        date.isAfter(today) -> false
                                        else -> {
                                            runCatching {
                                                val times = slotKey.split("-")
                                                if (times.size > 1) {
                                                    val endTimeStr = times[1].trim()
                                                    val endTime = java.time.LocalTime.parse(endTimeStr)
                                                    currentTime.isAfter(endTime)
                                                } else false
                                            }.getOrDefault(false)
                                        }
                                    }
                                    activeLesson to isPassed
                                } else null
                            }
                            .take(4)
                    }

                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            val timestamp = date.toEpochDay() * 24 * 60 * 60 * 1000L
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${date.dayOfMonth}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (pagerState.currentPage == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = dayOfWeekFormatter.format(timestamp).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (pagerState.currentPage == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Box(
                                    modifier = Modifier.height(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dotsData.isNotEmpty()) {
                                        androidx.compose.foundation.layout.Row(
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            dotsData.forEach { (lesson, isPassed) ->
                                                Surface(
                                                    modifier = Modifier.size(5.dp),
                                                    shape = androidx.compose.foundation.shape.CircleShape,
                                                    color = if (isPassed) {
                                                        MaterialTheme.colorScheme.outlineVariant
                                                    } else {
                                                        generateColorFromHash(lesson.sbj)
                                                    }
                                                ) {}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val selectedDate = allDays[pageIndex]
                val dayKey = selectedDate.dayOfWeek.value.toString()
                val lessonsForDay = screenData.fullSchedule.grid?.get(dayKey) ?: emptyMap()
                val weekInfo = screenData.weeks.firstOrNull { week ->
                    !selectedDate.isBefore(week.startDate) && !selectedDate.isAfter(week.endDate)
                } ?: screenData.weeks.first()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (lessonsForDay.isEmpty()) {
                        item {
                            ScheduleDayHeader(dayKey, weekInfo)
                            WeekEndCard()
                        }
                    } else {
                        item {
                            DaySchedule(dayKey, lessonsForDay, weekInfo, onLinkClick)
                        }
                    }
                    item { Spacer(Modifier.size(getNavigationBarHeightDp() + 80.dp)) }
                }
            }
        }

        val density = LocalDensity.current
        val translationX by animateFloatAsState(
            targetValue = if (showFab) 0f else with(density) { 150.dp.toPx() },
            animationSpec = tween(durationMillis = 300)
        )
        val alpha by animateFloatAsState(
            targetValue = if (showFab) 1f else 0f,
            animationSpec = tween(durationMillis = 300)
        )
        val scale by animateFloatAsState(
            targetValue = if (showFab) 1f else 0f,
            animationSpec = tween(durationMillis = 300)
        )

        FloatingActionButton(
            onClick = {
                if (!showFab) return@FloatingActionButton
                coroutineScope.launch {
                    pagerState.animateScrollToPage(todayIndex)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
                .navigationBarsPadding()
                .graphicsLayer {
                    this.translationX = translationX
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                }
        ) {
            Icon(
                imageVector = CalendarOutline28,
                contentDescription = stringResource(R.string.return_to_today)
            )
        }
    }
}

@Composable
fun WeekScheduleContent(schedule: ScheduleModel, week: WeekInfo, navController: NavController) {
    val today = LocalDate.now()
    val isCurrentWeek = !today.isBefore(week.startDate) && !today.isAfter(week.endDate)
    val todayDayKey = today.dayOfWeek.value.toString()

    val displayElements = remember(schedule, isCurrentWeek, todayDayKey) {
        val days = (schedule.grid ?: emptyMap()).entries
            .sortedBy { it.key.toInt() }
            .map { it.key to (it.value as Map<String, List<Lesson>>?) }
            .toMutableList()

        val hasTodayInGrid = days.any { it.first == todayDayKey }

        if (isCurrentWeek && !hasTodayInGrid) {
            val insertIndex = days.indexOfFirst { it.first.toInt() > todayDayKey.toInt() }
            if (insertIndex == -1) {
                days.add(todayDayKey to null)
            } else {
                days.add(insertIndex, todayDayKey to null)
            }
        }
        days
    }

    val listState = rememberLazyListState()

    LaunchedEffect(week) {
        if (isCurrentWeek) {
            val targetIndex = displayElements.indexOfFirst { it.first == todayDayKey }
            if (targetIndex != -1) {
                listState.scrollToItem(targetIndex)
            } else if (today.dayOfWeek == DayOfWeek.SUNDAY) {
                listState.scrollToItem(displayElements.size)
            }
        } else {
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(displayElements, key = { it.first }) { (dayKey, lessonsByTime) ->
            if (lessonsByTime == null) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ScheduleDayHeader(dayKey, week)
                    WeekEndCard()
                }
            } else {
                DaySchedule(dayKey, lessonsByTime, week) { teacherId ->
                    navController.navigate("teacherSchedule/${teacherId}")
                }
            }
        }
        item { Spacer(Modifier.size(getNavigationBarHeightDp() + 80.dp)) }
    }
}

@Composable
fun ScheduleDayHeader(dayKey: String, week: WeekInfo) {
    val date = remember(dayKey, week) {
        week.startDate.plusDays(dayKey.toLong() - 1)
    }
    val dayOfWeekFormatter = remember { SimpleDateFormat("EEEE", Locale.getDefault()) }
    val dayName = dayOfWeekFormatter.format(date.toEpochDay() * 24 * 60 * 60 * 1000L)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    Text(
        text = "$dayName, ${date.dayOfMonth}.${date.monthValue}",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MissingScheduleView(
    groupName: String,
    onNavigateToSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = Cookie9Sided.toShape()
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(com.rozetka.presentation.R.drawable.education_outline_28),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Расписание отсутствует",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Для группы $groupName не найдено регулярного расписания.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToSession,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Проверить расписание сессии")
        }
    }
}