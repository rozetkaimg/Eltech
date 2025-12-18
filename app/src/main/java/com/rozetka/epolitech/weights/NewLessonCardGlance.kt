package com.rozetka.epolitech.weights

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.rozetka.epolitech.R
import com.rozetka.epolitech.StartScheduleAction
import com.rozetka.model.Lesson

@SuppressLint("LocalContextResourcesRead")
@Composable
fun NewLessonCardGlance(lessonInfo: Pair<String, Lesson>) {
    val (lessonNumber, lesson) = lessonInfo
    val context = LocalContext.current
    val lessonTimes = context.resources.getStringArray(R.array.lesson_times)
    val lessonTime = lessonTimes.getOrElse(lessonNumber.toIntOrNull()?.minus(1) ?: -1) { "" }
    val (color, localizedTypeText) = when (lesson.type) {
        "Лаб. работа" -> Color(0xFFE57373) to context.getString(R.string.lesson_type_lab_short)
        "Лекция" -> Color(0xFFFFB74D) to context.getString(R.string.lesson_type_lecture_short)
        "Экзамен" -> Color(0xFF8698FF) to "Экзамен"
        "Зачет" -> Color(0xFFFFB74D) to "Зачет"
        "Диф. зачет" -> Color(0xFFFA5D34) to "Диф. Зачет"
        "Консультация" -> Color(0xFF81C784) to "Консультация"
        "Практика" -> Color(0xFF81C784) to context.getString(R.string.lesson_type_practice_short)
        else -> Color(0xFF81C784) to context.getString(R.string.lesson_type_unknown)
    }

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(12.dp)
                .padding(12.dp)
                .clickable(actionRunCallback<StartScheduleAction>())
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(24.dp)
                        .background(GlanceTheme.colors.primary)
                        .cornerRadius(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lessonNumber,
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = lessonTime,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = localizedTypeText,
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
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = lesson.sbj,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
            Spacer(GlanceModifier.height(4.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = lesson.auditories.joinToString { it.title.replace(Regex("<.*?>"), "") },
                    style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant)
                )
                Spacer(GlanceModifier.defaultWeight())
            }
        }
    }
}