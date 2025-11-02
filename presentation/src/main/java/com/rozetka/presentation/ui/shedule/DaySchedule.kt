package com.rozetka.presentation.ui.shedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rozetka.model.Lesson
import com.rozetka.presentation.R
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DaySchedule(dayKey: String, lessonsByTime: Map<String, List<Lesson>>, week: WeekInfo) {
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

    val filteredLessons = lessonsByTime.entries
        .flatMap { (lessonNumber, lessons) ->
            lessons.filter { lesson -> isLessonInWeek(lesson, week) }
                .map { lesson -> lessonNumber to lesson }
        }
        .sortedBy { (lessonNumber, _) -> lessonNumber.toInt() }

    val lessonDate = week.startDate.plusDays(dayKey.toLong() - 1)
    val formatter = DateTimeFormatter.ofPattern("dd.MM")
    val formattedDate = lessonDate.format(formatter)

    Column {
        Text(
            text = "$dayName, $formattedDate",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (filteredLessons.isNotEmpty()) {
            filteredLessons.forEachIndexed { index, (lessonNumber, lesson) ->
                LessonItem(
                    lesson = lesson,
                    lessonNumber = lessonNumber,
                    index = index,
                    totalLessonsInDay = filteredLessons.size,
                    lessonDate = lessonDate,
                    onLessonClick = { clickedLesson ->
                        selectedLesson = clickedLesson
                    }
                )
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
                }
            )
        }
    }
}