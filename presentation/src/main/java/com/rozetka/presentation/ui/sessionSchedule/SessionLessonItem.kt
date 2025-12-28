package com.rozetka.presentation.ui.sessionSchedule

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rozetka.model.Lesson
import com.rozetka.presentation.util.getAcademicEventColor
import com.rozetka.presentation.util.removeEmojis
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun SessionLessonItem(
    lesson: Lesson,
    lessonNumber: String,
    index: Int,
    totalLessonsInDay: Int,
    lessonDate: LocalDate,
    onLessonClick: (Lesson) -> Unit,

    onExportToCalendar: (title: String, description: String, location: String, startMillis: Long, endMillis: Long) -> Unit
) {
    fun getLessonTime(position: String): String {
        return when (position) {
            "1" -> "09:00 - 10:30"
            "2" -> "10:40 - 12:10"
            "3" -> "12:20 - 13:50"
            "4" -> "14:30 - 16:00"
            "5" -> "16:10 - 17:40"
            "6" -> "17:50 - 19:20"
            "7" -> "19:30 - 21:00"
            else -> ""
        }
    }

    fun getLessonStartTime(position: String): LocalTime {
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

    fun getLessonEndTime(position: String): LocalTime {
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

    val shape = when {
        totalLessonsInDay == 1 -> RoundedCornerShape(28.dp)
        index == 0 -> RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomEnd = 8.dp,
            bottomStart = 8.dp
        )
        index == totalLessonsInDay - 1 -> RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp,
            bottomEnd = 28.dp,
            bottomStart = 28.dp
        )
        else -> RoundedCornerShape(8.dp)
    }

    val lessonStartTime = getLessonStartTime(lessonNumber)
    val lessonEndTime = getLessonEndTime(lessonNumber)
    val lessonStartDateTime = LocalDateTime.of(lessonDate, lessonStartTime)
    val lessonEndDateTime = LocalDateTime.of(lessonDate, lessonEndTime)
    val now = LocalDateTime.now()

    val isLessonPassed = now.isAfter(lessonEndDateTime)
    val isLessonInProgress = now.isAfter(lessonStartDateTime) && now.isBefore(lessonEndDateTime)

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
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lessonNumber,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = getLessonTime(lessonNumber),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.weight(1f))


                IconButton(
                    onClick = {
                        val zoneId = ZoneId.systemDefault()
                        val startMillis = lessonStartDateTime.atZone(zoneId).toInstant().toEpochMilli()
                        val endMillis = lessonEndDateTime.atZone(zoneId).toInstant().toEpochMilli()

                        val location = removeEmojis(lesson.auditories.joinToString {
                            it.title.replace(Regex("<.*?>"), "")
                        })

                        onExportToCalendar(
                            "${lesson.type}: ${lesson.sbj}",
                            "Преподаватель: ${lesson.teacher}",
                            location,
                            startMillis,
                            endMillis
                        )
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id =com.rozetka.presentation.R.drawable.calendar_add_outline_24 ),
                        contentDescription = "Export to Calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))


                val colorType = if (isLessonPassed) {
                    Color.Gray.copy(alpha = 0.5f)
                } else {
                    getAcademicEventColor(lesson.type)
                }
                val infiniteTransition = rememberInfiniteTransition(label = "size_transition")

                val scale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 750, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale_animation"
                )

                Box(
                    Modifier.background(
                        color = colorType,
                        shape = RoundedCornerShape(if (isLessonInProgress) 20.dp / scale else 20.dp)
                    )
                ) {
                    Text(
                        text = lesson.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // ... (Остальной код карточки без изменений) ...
            Spacer(Modifier.height(8.dp))

            Text(
                text = lesson.sbj,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = removeEmojis(lesson.auditories.joinToString {
                        it.title.replace(Regex("<.*?>"), "")
                    }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(Modifier.weight(1f))

                if (lesson.teacher.isNotBlank()) {
                    val validTeachers = lesson.teacher
                        .split(',')
                        .map { it.trim() }
                        .filter { !it.startsWith("Вакансия", ignoreCase = true) && it.isNotBlank() }

                    if (validTeachers.isNotEmpty()) {
                        val formattedTeachers = when {
                            validTeachers.size == 1 -> {
                                validTeachers.first()
                            }
                            else -> {
                                validTeachers
                                    .take(2).joinToString(", ") { fullName ->
                                        val parts = fullName.split(' ').filter { it.isNotBlank() }
                                        when {
                                            parts.size >= 3 -> {
                                                val surname = parts[0]
                                                val firstInitial = parts[1].first()
                                                val middleInitial = parts[2].first()
                                                "$surname $firstInitial.$middleInitial"
                                            }
                                            parts.size == 2 -> {
                                                val surname = parts[0]
                                                val firstInitial = parts[1].first()
                                                "$surname $firstInitial."
                                            }
                                            else -> {
                                                fullName
                                            }
                                        }
                                    }
                            }
                        }

                        if (formattedTeachers.isNotBlank()) {
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = formattedTeachers,
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
}