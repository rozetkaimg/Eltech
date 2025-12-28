package com.rozetka.presentation.ui.teacherSchedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rozetka.model.LessonS
import com.rozetka.model.ScheduleByDay
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.UiSize
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class LessonWithDay(
    val dayKey: String,
    val lesson: LessonS
)

private object MonthSorter {
    val academicMonthOrder = listOf(
        "сен", "окт", "ноя", "дек",
        "янв", "фев", "мар", "апр", "май", "июн", "июл", "авг"
    )

    val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    private val fullMonthNames = mapOf(
        "сен" to "Сентябрь", "сент" to "Сентябрь",
        "окт" to "Октябрь",
        "ноя" to "Ноябрь", "ноябрь" to "Ноябрь",
        "дек" to "Декабрь",
        "янв" to "Январь",
        "фев" to "Февраль",
        "мар" to "Март",
        "апр" to "Апрель",
        "май" to "Май",
        "июн" to "Июнь",
        "июл" to "Июль",
        "авг" to "Август",
        "прочее" to "Прочее"
    )

    fun getMonthNameByKey(key: String): String {
        return fullMonthNames[key] ?: key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun extractMonthsFromInterval(dateInterval: String?): List<String> {
        if (dateInterval.isNullOrBlank()) return listOf("прочее")

        val normalizedInterval = dateInterval.lowercase(Locale.getDefault())
        val foundIndices = mutableListOf<Int>()
        val words = normalizedInterval.split(Regex("[^а-яa-z]+"))

        for (word in words) {
            val index = academicMonthOrder.indexOfFirst { key -> word.startsWith(key) }
            if (index != -1) {
                foundIndices.add(index)
            }
        }

        if (foundIndices.isEmpty()) return listOf("прочее")

        val minIndex = foundIndices.minOrNull() ?: return listOf("прочее")
        val maxIndex = foundIndices.maxOrNull() ?: return listOf("прочее")

        return academicMonthOrder.slice(minIndex..maxIndex)
    }
}

private sealed class TeacherDayViewItem {
    data class LessonData(val lesson: LessonS) : TeacherDayViewItem()
    data class BreakData(val startTime: LocalTime, val endTime: LocalTime) : TeacherDayViewItem()
}

private object TeacherLessonTimeUtil {
    fun getStartTime(timeInterval: String?): LocalTime? {
        if (timeInterval == null) return null
        return try {
            LocalTime.parse(timeInterval.split(" - ")[0])
        } catch (_: Exception) {
            null
        }
    }

    fun getEndTime(timeInterval: String?): LocalTime? {
        if (timeInterval == null) return null
        return try {
            LocalTime.parse(timeInterval.split(" - ")[1])
        } catch (_: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScheduleScreen(
    navController: NavHostController,
    fio: String,
    viewModel: TeacherScheduleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(fio) {
        viewModel.getSchedule(fio)
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
                    Text(text = fio, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            color = MaterialTheme.colorScheme.surface
        ) {
            when (val state = uiState) {
                is TeacherScheduleUiState.Loading -> {
                    LoadingState()
                }
                is TeacherScheduleUiState.Error -> ExpressiveErrorState("Ошибка", {})
                is TeacherScheduleUiState.Success -> {
                    TeacherScheduleContent(schedule = state.data)
                }
                is TeacherScheduleUiState.Initial -> {}
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeacherScheduleContent(schedule: ScheduleByDay) {
    val processedData = remember(schedule) {
        val allLessonsWithDays = schedule.flatMap { entry ->
            entry.value.map { lesson -> LessonWithDay(entry.key, lesson) }
        }

        val lessonsExpandedByMonth = allLessonsWithDays.flatMap { item ->
            val extractedMonths = MonthSorter.extractMonthsFromInterval(item.lesson.dateInterval)

            val months = if (extractedMonths.size > 1 && extractedMonths.contains("янв")) {
                extractedMonths.filter { it != "янв" }
            } else {
                extractedMonths
            }

            months.map { monthKey ->
                monthKey to item
            }
        }

        val groupedByMonth = lessonsExpandedByMonth.groupBy({ it.first }, { it.second })

        val sortedMonthKeys = groupedByMonth.keys.sortedBy { key ->
            val index = MonthSorter.academicMonthOrder.indexOf(key)
            if (index == -1) 99 else index
        }
        sortedMonthKeys to groupedByMonth
    }

    val (months, lessonsByMonthMap) = processedData
    var selectedLesson by remember { mutableStateOf<LessonS?>(null) }

    if (months.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Расписание отсутствует")
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { months.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            months.forEachIndexed { index, monthKey ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        Text(
                            text = MonthSorter.getMonthNameByKey(monthKey),
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val monthKey = months[pageIndex]
            val lessonsInMonth = lessonsByMonthMap[monthKey] ?: emptyList()

            val daysInMonth = remember(lessonsInMonth) {
                lessonsInMonth
                    .groupBy { it.dayKey }
                    .entries
                    .sortedBy { MonthSorter.dayOrder.indexOf(it.key) }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                daysInMonth.forEach { (dayKey, lessonsWithDayList) ->
                    item {
                        val lessons = lessonsWithDayList.map { it.lesson }
                        TeacherDaySchedule(
                            dayKey = dayKey,
                            lessons = lessons,
                            onLessonClick = { selectedLesson = it }
                        )
                    }
                }

                item { Spacer(Modifier.size(UiSize().getNavBarPaddingSize())) }
            }
        }
    }

    if (selectedLesson != null) {
        TeacherLessonDetailsDialog(
            lesson = selectedLesson!!,
            onDismissRequest = {  }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeacherDaySchedule(
    dayKey: String,
    lessons: List<LessonS>,
    onLessonClick: (LessonS) -> Unit
) {
    val dayName = when (dayKey) {
        "Monday" -> "Понедельник"
        "Tuesday" -> "Вторник"
        "Wednesday" -> "Среда"
        "Thursday" -> "Четверг"
        "Friday" -> "Пятница"
        "Saturday" -> "Суббота"
        "Sunday" -> "Воскресенье"
        else -> dayKey
    }

    val sortedLessons = lessons.sortedBy { TeacherLessonTimeUtil.getStartTime(it.timeInterval) }

    val dayViewItems = mutableListOf<TeacherDayViewItem>()
    var previousLessonEndTime: LocalTime? = null

    for (lesson in sortedLessons) {
        val currentLessonStartTime = TeacherLessonTimeUtil.getStartTime(lesson.timeInterval)
        val currentLessonEndTime = TeacherLessonTimeUtil.getEndTime(lesson.timeInterval)

        if (previousLessonEndTime != null && currentLessonStartTime != null) {
            val duration = Duration.between(previousLessonEndTime, currentLessonStartTime)
            if (duration.toMinutes() > 10) {
                dayViewItems.add(TeacherDayViewItem.BreakData(previousLessonEndTime, currentLessonStartTime))
            }
        }

        dayViewItems.add(TeacherDayViewItem.LessonData(lesson))
        previousLessonEndTime = currentLessonEndTime
    }

    Column {
        Text(
            text = dayName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (dayViewItems.isNotEmpty()) {
            dayViewItems.forEachIndexed { index, item ->
                when (item) {
                    is TeacherDayViewItem.LessonData -> {
                        TeacherLessonItem(
                            lesson = item.lesson,
                            index = index,
                            totalItemsInDay = dayViewItems.size,
                            onLessonClick = onLessonClick
                        )
                    }
                    is TeacherDayViewItem.BreakData -> {
                        TeacherBreakItem(
                            startTime = item.startTime,
                            endTime = item.endTime,
                            index = index,
                            totalItemsInDay = dayViewItems.size
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(MaterialShapes.Cookie9Sided.toShape())
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.home_outline),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Column(modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)) {
                        Text(
                            text = "Выходной",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "В этот день у сотрудника нету пар",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherBreakItem(
    startTime: LocalTime,
    endTime: LocalTime,
    index: Int,
    totalItemsInDay: Int
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val shape = when {
        totalItemsInDay == 1 -> RoundedCornerShape(28.dp)
        index == 0 -> RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomEnd = 8.dp,
            bottomStart = 8.dp
        )
        index == totalItemsInDay - 1 -> RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp,
            bottomEnd = 28.dp,
            bottomStart = 28.dp
        )
        else -> RoundedCornerShape(8.dp)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Перерыв",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.End
            )
        }
    }
}



