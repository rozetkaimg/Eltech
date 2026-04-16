package com.rozetka.epolitech.weights

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rozetka.epolitech.R
import com.rozetka.data.SecureStorage
import com.rozetka.domain.repository.ScheduleRepository
import com.rozetka.model.Lesson
import com.rozetka.model.ScheduleModel
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class WeekInfo(
    val startDate: LocalDate,
    val endDate: LocalDate
)

class ScheduleWidgetWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), KoinComponent {

    companion object {
        const val WORK_NAME = "com.rozetka.epolitech.widgets.ScheduleWidgetWorker"
        private const val TAG = "ScheduleWidgetWorker"
    }

    private val scheduleRepository: ScheduleRepository by inject()
    private val secureStorage: SecureStorage by inject()
    private val mospolytechMethods: MospolytechMethods by inject()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        Log.d(TAG, context.getString(R.string.worker_log_started))
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(TodayScheduleWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[TodayScheduleWidget.scheduleStateKey] = json.encodeToString(
                    ScheduleWidgetState.serializer(),
                    ScheduleWidgetState.Loading
                )
            }
        }
        TodayScheduleWidget().updateAll(context)

        try {
            val groupName = secureStorage.getGroupName()
            if (groupName.isNullOrBlank()) {
                throw IllegalStateException(context.getString(R.string.worker_error_group_not_found))
            }

            var workerResult: Result = Result.failure()

            scheduleRepository.getSchedule(groupName)
                .map { result ->
                    result.onSuccess { scheduleData ->
                        val today = LocalDate.now()
                        val weekInfo = findCurrentWeek(scheduleData)
                        val todayKey = today.dayOfWeek.value.toString()
                        var lessonsForToday = scheduleData.grid[todayKey]?.flatMap { (lessonNumber, lessons) ->
                            lessons
                                .filter { lesson -> isLessonInWeek(lesson, weekInfo) }
                                .map { lesson -> lessonNumber to lesson }
                        }?.sortedBy { (lessonNumber, _) -> lessonNumber.toIntOrNull() ?: 0 } ?: emptyList()

                        if (lessonsForToday.isEmpty()) {
                            try {
                                val sessionSchedule = mospolytechMethods.getSessionSchedule(groupName)
                                val sessionLessonsMap = sessionSchedule.grid.entries.find { (dateKey, _) ->
                                    try {
                                        LocalDate.parse(dateKey).isEqual(today)
                                    } catch (e: Exception) {
                                        dateKey == today.toString()
                                    }
                                }?.value

                                if (!sessionLessonsMap.isNullOrEmpty()) {
                                    lessonsForToday = sessionLessonsMap.flatMap { (timeKey, lessons) ->
                                        lessons.map { lesson -> timeKey to lesson }
                                    }.sortedBy { (timeKey, _) -> timeKey }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to load session schedule for widget", e)
                            }
                        }

                        val successState = ScheduleWidgetState.Success(lessonsForToday, today.toString())

                        glanceIds.forEach { glanceId ->
                            updateAppWidgetState(context, glanceId) { prefs ->
                                prefs[TodayScheduleWidget.scheduleStateKey] = json.encodeToString(
                                    ScheduleWidgetState.serializer(), successState
                                )
                            }
                        }
                        TodayScheduleWidget().updateAll(context)
                        workerResult = Result.success()
                    }
                    result.onFailure { throwable ->
                        throw throwable
                    }
                }
                .catch { e ->
                    throw e
                }
                .first()

            return workerResult

        } catch (e: Exception) {
            val defaultError = context.getString(R.string.worker_error_loading_schedule)
            val errorState = ScheduleWidgetState.Error(e.message ?: defaultError)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[TodayScheduleWidget.scheduleStateKey] = json.encodeToString(
                        ScheduleWidgetState.serializer(), errorState
                    )
                }
            }
            TodayScheduleWidget().updateAll(context)
            return Result.failure()
        }
    }

    private fun findCurrentWeek(schedule: ScheduleModel): WeekInfo {
        val today = LocalDate.now()
        val group = schedule.group

        if (group != null) {
            try {
                val semesterStart = LocalDate.parse(group.dateFrom)
                val semesterEnd = LocalDate.parse(group.dateTo)

                var currentStart = semesterStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val maxWeeks = 52
                var weekCount = 0

                while (!currentStart.isAfter(semesterEnd) && weekCount < maxWeeks) {
                    val currentEnd = currentStart.plusDays(6)
                    if (!today.isBefore(currentStart) && !today.isAfter(currentEnd)) {
                        return WeekInfo(currentStart, currentEnd)
                    }
                    currentStart = currentStart.plusWeeks(1)
                    weekCount++
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating week", e)
            }
        }

        val fallbackStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return WeekInfo(
            fallbackStart,
            fallbackStart.plusDays(6)
        )
    }

    private fun isLessonInWeek(lesson: Lesson, week: WeekInfo): Boolean {
        return try {
            val lessonStart = LocalDate.parse(lesson.df)
            val lessonEnd = LocalDate.parse(lesson.dt)
            !lessonStart.isAfter(week.endDate) && !lessonEnd.isBefore(week.startDate)
        } catch (_: Exception) {
            true
        }
    }
}