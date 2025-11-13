package com.rozetka.presentation.ui.teacherSchedule


import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rozetka.model.LessonS
import com.rozetka.model.ScheduleByDay
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.getNavigationBarHeightDp
import com.rozetka.presentation.util.getRandomRoundedCornerShape
import com.rozetka.presentation.util.removeEmojis
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter


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
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.surface
        ) {
            when (val state = uiState) {
                is TeacherScheduleUiState.Loading -> {
                    LoadingState()
                }
                is TeacherScheduleUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is TeacherScheduleUiState.Success -> {
                    TeacherScheduleContent(schedule = state.data)
                }
                is TeacherScheduleUiState.Initial -> {}
            }
        }
    }
}

@Composable
fun TeacherScheduleContent(schedule: ScheduleByDay) {
    val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val sortedDays = schedule.entries.sortedBy { dayOrder.indexOf(it.key) }
    var selectedLesson by remember { mutableStateOf<LessonS?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        sortedDays.forEach { (dayKey, lessons) ->
            item {
                TeacherDaySchedule(
                    dayKey = dayKey,
                    lessons = lessons,
                    onLessonClick = { lesson ->
                        selectedLesson = lesson
                    }
                )
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }

    if (selectedLesson != null) {
        TeacherLessonDetailsDialog(
            lesson = selectedLesson!!,
            onDismissRequest = {
                selectedLesson = null
            }
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
                            contentDescription = stringResource(R.string.academic_year_content_description),
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

@Composable
fun TeacherLessonItem(
    lesson: LessonS,
    index: Int,
    totalItemsInDay: Int,
    onLessonClick: (LessonS) -> Unit
) {
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
        onClick = { onLessonClick(lesson) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.education_outline_28),
                        contentDescription = "Lesson",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                lesson.timeInterval?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))


            lesson.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val auditory = (lesson.place ?: "") +
                        if (lesson.rooms.isNotEmpty()) " (${lesson.rooms.joinToString()})" else ""
                if (auditory.isNotBlank()) {
                    Text(
                        text = removeEmojis(auditory),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(Modifier.weight(1f))

                lesson.groups?.let {
                    if (it.isNotBlank()) {
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherLessonDetailsDialog(
    lesson: LessonS,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(shape = RoundedCornerShape(28.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(getRandomRoundedCornerShape())
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.education_outline_28),
                        contentDescription = stringResource(R.string.academic_year_content_description),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.size(16.dp))

                lesson.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    lesson.groups?.let {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.group_name_label))
                                }
                                append(it)
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (lesson.teachers.isNotEmpty()) {
                        Text(
                            text = if (lesson.teachers.size > 1) stringResource(R.string.label_teachers) else stringResource(R.string.label_teacher_single),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        lesson.teachers.forEach { teacher ->
                            Text(
                                text = teacher,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }


                    lesson.dateInterval?.let {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.label_dates))
                                }
                                append(it)
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }


                    AuditoryInfoS(lesson = lesson)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.close_button))
                }
            }
        }
    }
}

@Composable
fun AuditoryInfoS(
    lesson: LessonS,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val auditory = (lesson.place ?: "") +
            if (lesson.rooms.isNotEmpty()) " (${lesson.rooms.joinToString()})" else ""

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (auditory.isNotBlank()) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.label_auditorium))
                    }
                    append(auditory)
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }

        lesson.link?.let { url ->
            if (url.isNotBlank()) {
                Spacer(Modifier.size(4.dp))
                Button(
                    onClick = {
                        try {
                            uriHandler.openUri(url.trim())
                        } catch (_: Exception) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_open_link_no_app),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.error_open_link_no_app))
                }
            }
        }
    }
}