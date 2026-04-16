package com.rozetka.data.repository

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract

import com.rozetka.domain.repository.CalendarRepository
import com.rozetka.model.Lesson
import com.rozetka.model.ScheduleModel
import com.rozetka.model.local.CalendarAccount

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.TimeZone

class CalendarRepositoryImpl(private val context: Context) : CalendarRepository {

    // Указываем, что данные API всегда приходят по Москве
    private val moscowZone = ZoneId.of("Europe/Moscow")

    override suspend fun getAvailableCalendars(): List<CalendarAccount> = withContext(Dispatchers.IO) {
        val calendars = mutableListOf<CalendarAccount>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )
        try {
            val selection = "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?"
            val selectionArgs = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection, selection, selectionArgs, null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                val nameIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val accountIndex = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val displayName = cursor.getString(nameIndex) ?: "Unknown"
                    val accountName = cursor.getString(accountIndex) ?: "Local"
                    calendars.add(CalendarAccount(id, displayName, accountName))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        calendars
    }

    override suspend fun exportSchedule(schedule: ScheduleModel, calendarId: Long): Int = withContext(Dispatchers.IO) {
        try {
            var eventCount = 0
            schedule.grid?.forEach { (dateString, timeMap) ->
                val date = try { LocalDate.parse(dateString) } catch (e: Exception) { null } ?: return@forEach
                timeMap.forEach { (lessonNumber, lessons) ->
                    lessons.forEach { lesson ->
                        val startTime = getLessonStartTime(lessonNumber)
                        val endTime = getLessonEndTime(lessonNumber)

                        // 1. Создаем точку во времени относительно Москвы
                        val startZoned = LocalDateTime.of(date, startTime).atZone(moscowZone)
                        val endZoned = LocalDateTime.of(date, endTime).atZone(moscowZone)

                        // 2. Получаем абсолютные миллисекунды (UTC) для базы данных
                        val startMillis = startZoned.toInstant().toEpochMilli()
                        val endMillis = endZoned.toInstant().toEpochMilli()

                        val eventTitle = "${lesson.type}: ${lesson.sbj}"

                        if (!doesEventExist(calendarId, eventTitle, startMillis, endMillis)) {
                            val values = ContentValues().apply {
                                put(CalendarContract.Events.DTSTART, startMillis)
                                put(CalendarContract.Events.DTEND, endMillis)
                                put(CalendarContract.Events.TITLE, eventTitle)
                                put(CalendarContract.Events.DESCRIPTION, "Преподаватель: ${lesson.teacher}")
                                put(CalendarContract.Events.EVENT_LOCATION, lesson.auditories.joinToString { it.title })
                                put(CalendarContract.Events.CALENDAR_ID, calendarId)

                                // Важно: указываем таймзону события как Москву.
                                // Календарь будет знать, что событие происходит в MSK.
                                put(CalendarContract.Events.EVENT_TIMEZONE, moscowZone.id)

                                val color = getLessonColor(lesson.type)
                                if (color != null) {
                                    put(CalendarContract.Events.EVENT_COLOR, color)
                                }
                            }
                            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                            eventCount++
                        }
                    }
                }
            }
            eventCount
        } catch (e: Exception) {
            -1
        }
    }

    override suspend fun exportSingleLesson(lesson: Lesson, lessonDate: LocalDate, lessonNumber: String, calendarId: Long): Boolean {

        return true
    }

    private fun doesEventExist(calendarId: Long, title: String, startMillis: Long, endMillis: Long): Boolean {
        val projection = arrayOf(CalendarContract.Events._ID)
        val selection = "(" +
                "${CalendarContract.Events.CALENDAR_ID} = ? AND " +
                "${CalendarContract.Events.TITLE} = ? AND " +
                "${CalendarContract.Events.DTSTART} = ? AND " +
                "${CalendarContract.Events.DTEND} = ?" +
                ")"
        val selectionArgs = arrayOf(calendarId.toString(), title, startMillis.toString(), endMillis.toString())

        return try {
            context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection, selection, selectionArgs, null
            )?.use { it.count > 0 } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun getLessonColor(type: String): Int? {
        return when (type) {
            "Экзамен" -> 0xFF8698FF.toInt()
            "Зачет" -> 0xFFFFB74D.toInt()
            "Диф. зачет" -> 0xFFFA5D34.toInt()
            "Консультация" -> 0xFF81C784.toInt()
            else -> null
        }
    }

    private fun getLessonStartTime(position: String): LocalTime {
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

    private fun getLessonEndTime(position: String): LocalTime {
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