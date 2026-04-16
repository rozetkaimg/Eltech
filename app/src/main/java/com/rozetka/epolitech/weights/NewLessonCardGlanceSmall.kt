package com.rozetka.epolitech.weights

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rozetka.data.SecureStorage
import com.rozetka.epolitech.R
import com.rozetka.epolitech.StartScheduleAction
import com.rozetka.epolitech.services.LessonNotificationService
import com.rozetka.model.Lesson
import java.text.SimpleDateFormat
import java.util.Locale

private fun parseLessonTimestamps(dateString: String, timeString: String): Pair<Long, Long> {
    val parts = timeString.split('-')
    if (parts.size != 2 || dateString.isBlank() || timeString.isBlank()) {
        Log.e("NewLessonCardGlance", "Invalid date/time string format: $dateString, $timeString")
        return 0L to 0L
    }

    val startTimeStr = parts[0].trim()
    val endTimeStr = parts[1].trim()
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    return try {
        val startDate = sdf.parse("$dateString $startTimeStr")
        val endDate = sdf.parse("$dateString $endTimeStr")
        (startDate?.time ?: 0L) to (endDate?.time ?: 0L)
    } catch (e: Exception) {
        Log.e("NewLessonCardGlance", "Failed to parse date/time", e)
        0L to 0L
    }
}

@SuppressLint("LocalContextResourcesRead")
@Composable
fun NewLessonCardGlanceSmall(
    lessonInfo: Pair<String, Lesson>,
    dateString: String
) {
    val (lessonNumber, lesson) = lessonInfo
    val context = LocalContext.current

    val lessonTimes = context.resources.getStringArray(R.array.lesson_times)
    val lessonTime = lessonTimes.getOrElse(lessonNumber.toIntOrNull()?.minus(1) ?: -1) { "" }
    val (color, text) = when (lesson.type) {
        "Лаб. работа" -> Color(0xFFE57373) to context.getString(R.string.lesson_type_lab_short)
        "Лекция" -> Color(0xFFFFB74D) to context.getString(R.string.lesson_type_lecture_short)
        "Экзамен" -> Color(0xFF8698FF) to "Экз."
        "Зачет" -> Color(0xFFFFB74D) to "Зач."
        "Диф. зачет" -> Color(0xFFFA5D34) to "Д.Зач"
        "Консультация" -> Color(0xFF81C784) to "Конс."
        "Практика" -> Color(0xFF81C784) to context.getString(R.string.lesson_type_practice_short)
        else -> Color(0xFF81C784) to context.getString(R.string.lesson_type_unknown)
    }
    val auditoryText = lesson.auditories.joinToString { it.title.replace(Regex("<.*?>"), "") }
    val (lessonStartTimeMillis, lessonEndTimeMillis) = parseLessonTimestamps(dateString, lessonTime)

    val currentTime = System.currentTimeMillis()
    val isValidTime = lessonStartTimeMillis != 0L && lessonEndTimeMillis != 0L
    val isActive = isValidTime && currentTime in lessonStartTimeMillis..lessonEndTimeMillis
    val isPast = isValidTime && currentTime > lessonEndTimeMillis

    val cardBackground = when {
        isActive -> GlanceTheme.colors.secondaryContainer
        isPast -> GlanceTheme.colors.surface
        else -> GlanceTheme.colors.surfaceVariant
    }

    val mainTextColor = when {
        isPast -> GlanceTheme.colors.outline
        else -> GlanceTheme.colors.onSurface
    }

    val clickableAction: Action = if (!SecureStorage(context).getScheduleNotificationState()) {
        actionRunCallback<StartScheduleAction>()
    } else {
        val serviceIntent = Intent(context, LessonNotificationService::class.java).apply {
            action = LessonNotificationService.ACTION_START
            putExtra(LessonNotificationService.EXTRA_LESSSON_NAME, lesson.sbj)
            putExtra(LessonNotificationService.EXTRA_LESSSON_TIME, lessonTime)
            putExtra(LessonNotificationService.EXTRA_LESSSON_AUDITORY, auditoryText)
            putExtra(LessonNotificationService.EXTRA_LESSSON_START_TIME, lessonStartTimeMillis)
            putExtra(LessonNotificationService.EXTRA_LESSSON_END_TIME, lessonEndTimeMillis)
        }
        actionStartService(intent = serviceIntent, isForegroundService = true)
    }

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 0.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(cardBackground)
                .cornerRadius(16.dp)
                .padding(8.dp)
                .clickable(clickableAction)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lessonTime,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = mainTextColor
                    )
                )
            }
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = lesson.sbj,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = mainTextColor
                ),
                maxLines = 2
            )
            Spacer(GlanceModifier.height(4.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = auditoryText,
                    style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier.defaultWeight(),
                    maxLines = 1,
                )

                Text(
                    text = text,
                    modifier = GlanceModifier
                        .background(if (isPast) Color.Gray.copy(alpha = 0.5f) else color)
                        .cornerRadius(16.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onPrimaryContainer
                    )
                )
            }
        }
    }
}