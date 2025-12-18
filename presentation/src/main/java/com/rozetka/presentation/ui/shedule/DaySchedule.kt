package com.rozetka.presentation.ui.shedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.rozetka.model.Lesson
import com.rozetka.presentation.R
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private sealed class DayViewItem {
    data class LessonData(val lessonNumber: String, val lesson: Lesson) : DayViewItem()
    data class BreakData(val startTime: LocalTime, val endTime: LocalTime) : DayViewItem()
}

private object LessonTimeUtil {
    fun getSlotStartTime(position: String): LocalTime {
        val timeString = when (position) {
            "1" -> "09:00"
            "2" -> "10:40"
            "3" -> "12:20"
            "4" -> "14:30"
            "5" -> "16:10"
            "6" -> "17:50"
            "7" -> "19:30"
            else -> "00:00"
        }
        return LocalTime.parse(timeString)
    }

    fun getSlotEndTime(position: String): LocalTime {
        val timeString = when (position) {
            "1" -> "10:30"
            "2" -> "12:10"
            "3" -> "13:50"
            "4" -> "16:00"
            "5" -> "17:40"
            "6" -> "19:20"
            "7" -> "21:00"
            else -> "23:59"
        }
        return LocalTime.parse(timeString)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DaySchedule(dayKey: String, lessonsByTime: Map<String, List<Lesson>>, week: WeekInfo, onLinkClick: (String) -> Unit) {
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }

    val dayName = when (dayKey) {
        "1" -> "Понедельник"
        "2" -> "Вторник"
        "3" -> "Среда"
        "4" -> "Четверг"
        "5" -> "Пятница"
        "6" -> "Суббота"
        else -> "Воскресенье"
    }

    val lessonDate = week.startDate.plusDays(dayKey.toLong() - 1)
    val formatter = DateTimeFormatter.ofPattern("dd.MM")
    val formattedDate = lessonDate.format(formatter)

    val filteredLessons = lessonsByTime.entries
        .flatMap { (lessonNumber, lessons) ->
            lessons.filter { lesson ->

                if (lesson.df.isBlank() && lesson.dt.isBlank()) {
                    true
                } else {
                    try {
                        val lessonStart = LocalDate.parse(lesson.df)
                        val lessonEnd = LocalDate.parse(lesson.dt)
                        !lessonDate.isBefore(lessonStart) && !lessonDate.isAfter(lessonEnd)

                    } catch (_: Exception) {
                        false
                    }
                }
            }
                .map { lesson -> lessonNumber to lesson }
        }
        .sortedBy { (lessonNumber, _) -> lessonNumber.toInt() }


    val dayViewItems = mutableListOf<DayViewItem>()
    var previousLessonNumber: Int? = null

    for ((lessonNumberStr, lesson) in filteredLessons) {
        val currentLessonNumber = lessonNumberStr.toInt()

        if (previousLessonNumber != null) {
            val prevLessonEndTime = LessonTimeUtil.getSlotEndTime(previousLessonNumber.toString())
            val currentLessonStartTime = LessonTimeUtil.getSlotStartTime(currentLessonNumber.toString())

            val duration = Duration.between(prevLessonEndTime, currentLessonStartTime)
            if (duration.toMinutes() > 30) {
                dayViewItems.add(DayViewItem.BreakData(prevLessonEndTime, currentLessonStartTime))
            }
        }

        dayViewItems.add(DayViewItem.LessonData(lessonNumberStr, lesson))
        previousLessonNumber = currentLessonNumber
    }

    Column {
        Text(
            text = "$dayName, $formattedDate",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (dayViewItems.isNotEmpty()) {
            dayViewItems.forEachIndexed { index, item ->
                when (item) {
                    is DayViewItem.LessonData -> {
                        LessonItem(
                            lesson = item.lesson,
                            lessonNumber = item.lessonNumber,
                            index = index,
                            totalLessonsInDay = dayViewItems.size,
                            lessonDate = lessonDate,
                            onLessonClick = { clickedLesson ->
                                selectedLesson = clickedLesson
                            }
                        )
                    }
                    is DayViewItem.BreakData -> {
                        BreakItem(
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
                    .padding(vertical = 1.dp)
                ,
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),

                ) {

                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(Cookie9Sided.toShape())
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
                    Column(modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterVertically)) {
                        Text(
                            text = stringResource(R.string.weekend_text_line1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(R.string.weekend_text_line2),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                }

            }

        }

        if (selectedLesson != null) {
            LessonDetailsDialog(
                lesson = selectedLesson!!,
                onDismissRequest = {
                    selectedLesson = null
                },
                onLinkClick = onLinkClick

            )
        }
    }
}

@Composable
fun BreakItem(
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