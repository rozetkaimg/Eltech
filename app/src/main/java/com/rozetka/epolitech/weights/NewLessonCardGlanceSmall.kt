package com.rozetka.epolitech.weights

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
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
import com.rozetka.data.SecureStorage // Добавлен импорт
import com.rozetka.epolitech.R
import com.rozetka.epolitech.StartScheduleAction
import com.rozetka.epolitech.services.StartLessonNotificationAction
import com.rozetka.model.Lesson
import java.text.SimpleDateFormat
import java.util.Locale

private val lessonNameKey = ActionParameters.Key<String>("lessonNameKey")
private val lessonTimeKey = ActionParameters.Key<String>("lessonTimeKey")
private val lessonAuditoryKey = ActionParameters.Key<String>("lessonAuditoryKey")
private val lessonStartTimeKey = ActionParameters.Key<Long>("lessonStartTimeKey")
private val lessonEndTimeKey = ActionParameters.Key<Long>("lessonEndTimeKey")



private fun parseLessonTimestamps(dateString: String, timeString: String): Pair<Long, Long> {
    val parts = timeString.split('-')
    if (parts.size != 2 || dateString.isBlank() || timeString.isBlank()) {
        Log.e("NewLessonCardGlance", "Invalid date/time string format: $dateString, $timeString")
        return 0L to 0L
    }

    val startTimeStr = parts[0].trim()
    val endTimeStr = parts[1].trim()

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    try {

        val startDate = sdf.parse("$dateString $startTimeStr")
        val endDate = sdf.parse("$dateString $endTimeStr")

        if (startDate == null || endDate == null) {
            Log.e("NewLessonCardGlance", "Parsing returned null date for: $dateString, $timeString")
            return 0L to 0L
        }


        return startDate.time to endDate.time
    } catch (e: Exception) {
        Log.e("NewLessonCardGlance", "Failed to parse date/time", e)
        return 0L to 0L
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
        "Практика" -> Color(0xFF81C784) to context.getString(R.string.lesson_type_practice_short)
        else -> Color(0xFF81C784) to context.getString(R.string.lesson_type_unknown)
    }
    val auditoryText = lesson.auditories.joinToString { it.title.replace(Regex("<.*?>"), "") }
    val (lessonStartTimeMillis, lessonEndTimeMillis) = parseLessonTimestamps(dateString, lessonTime)

    val clickableAction: Action = if (!SecureStorage(context).getScheduleNotificationState()) {
        Log.d("info",SecureStorage(context).getScheduleNotificationState().toString() )
        actionRunCallback<StartScheduleAction>()
    } else {

        actionRunCallback<StartLessonNotificationAction>(
            parameters = actionParametersOf(
                lessonNameKey to lesson.sbj,
                lessonTimeKey to lessonTime,
                lessonAuditoryKey to auditoryText,
                lessonStartTimeKey to lessonStartTimeMillis,
                lessonEndTimeKey to lessonEndTimeMillis
            )
        )
    }

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 0.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surfaceVariant)
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
                        color = GlanceTheme.colors.onSurface
                    )
                )

            }
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = lesson.sbj,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 2
            )
            Spacer(GlanceModifier.height(4.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = lesson.auditories.joinToString { it.title.replace(Regex("<.*?>"), "") },
                    style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant)
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = text,
                    modifier = GlanceModifier
                        .background(color)
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
