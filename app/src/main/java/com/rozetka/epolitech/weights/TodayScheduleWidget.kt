package com.rozetka.epolitech.weights

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rozetka.epolitech.R
import com.rozetka.epolitech.StartScheduleAction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


class TodayScheduleWidget : GlanceAppWidget() {


    override val sizeMode = SizeMode.Exact

    companion object {
        val scheduleStateKey = stringPreferencesKey("schedule_state_key_v2")
        private const val TAG = "TodayScheduleWidget"

    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        requestDataUpdate(context)

        provideContent {
            val stateJson = currentState(key = scheduleStateKey)
            Log.d(TAG, context.getString(R.string.widget_log_json_read, stateJson ?: "null"))

            val state = if (!stateJson.isNullOrEmpty()) {
                try {
                    json.decodeFromString<ScheduleWidgetState>(stateJson)
                } catch (_: Exception) {
                    ScheduleWidgetState.Error(context.getString(R.string.widget_error_display))
                }
            } else {
                ScheduleWidgetState.Loading
            }

            GlanceTheme {
                WidgetContent(state = state)
            }
        }
    }

    @Composable
    private fun WidgetContent(state: ScheduleWidgetState) {
        val size = LocalSize.current
        val context = LocalContext.current
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(16.dp)
                .padding(0.dp).clickable(actionRunCallback<StartScheduleAction>())
        ) {
            val title = context.getString(R.string.widget_title_schedule)

            when (state) {
                is ScheduleWidgetState.Loading -> {
                    WidgetHeader(title = title, date = LocalDate.now())
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            context.getString(R.string.widget_status_loading),
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                    }
                }

                is ScheduleWidgetState.Success -> {

                    WidgetHeader(title = title, date = LocalDate.parse(state.dateString))
                    if (state.data.isEmpty()) {
                        Box(
                            modifier = GlanceModifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                context.getString(R.string.widget_status_no_lessons),
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = GlanceTheme.colors.onSurfaceVariant
                                )
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = GlanceModifier.fillMaxSize().padding(start = 8.dp, end = 8.dp,).cornerRadius(16.dp)
                        ) {
                            items(
                                state.data,
                                itemId = {
                                    (it.second.sbj + it.first).hashCode().toLong()
                                }) { item ->
                                if (size.width < 360.dp) {
                                    NewLessonCardGlanceSmall(
                                        lessonInfo = item,
                                        dateString = state.dateString
                                    )
                                } else {
                                    NewLessonCardGlance(
                                        lessonInfo = item,

                                    )
                                }
                            }
                            item {
                                Box(
                                    modifier = GlanceModifier.size(4.dp),
                                ) {

                                }

                            }
                        }
                    }
                }

                is ScheduleWidgetState.Error -> {
                    WidgetHeader(title = title, date = LocalDate.now())
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(context.getString(R.string.widget_error_prefix, state.message))
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetHeader(title: String, date: LocalDate?) {
    val currentLocale = Locale.getDefault()
    val formattedDate = date?.format(DateTimeFormatter.ofPattern("EE, d MMM", currentLocale))
        ?.replaceFirstChar { it.titlecase(currentLocale) }

    val context = LocalContext.current

    Row(
        modifier = GlanceModifier.fillMaxWidth().clickable(actionRunCallback<StartScheduleAction>())
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = GlanceTheme.colors.onSurface
            ),
        )
        Spacer(GlanceModifier.defaultWeight())
        Box(modifier = GlanceModifier.clickable(actionRunCallback<UpdateAction>())) {
            Image(
                provider = ImageProvider(com.rozetka.presentation.R.drawable.refresh_outline_28),
                contentDescription = context.getString(R.string.widget_acc_refresh),
                modifier = GlanceModifier.size(24.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
            )
        }
    }
    if (formattedDate != null) {
        Text(
            text = formattedDate,
            style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurfaceVariant),
            modifier = GlanceModifier.padding(horizontal = 12.dp)
        )
    }
}

class UpdateAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        requestDataUpdate(context)
    }
}



fun requestDataUpdate(context: Context) {
    val workManager = WorkManager.getInstance(context)
    val request = OneTimeWorkRequestBuilder<ScheduleWidgetWorker>().build()
    workManager.enqueueUniqueWork(
        ScheduleWidgetWorker.WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        request
    )
}