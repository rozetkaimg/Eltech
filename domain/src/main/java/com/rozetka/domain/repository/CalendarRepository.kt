package com.rozetka.domain.repository


import com.rozetka.model.ScheduleModel
import com.rozetka.model.Lesson
import com.rozetka.model.local.CalendarAccount

interface CalendarRepository {
    suspend fun getAvailableCalendars(): List<CalendarAccount>


    suspend fun exportSchedule(schedule: ScheduleModel, calendarId: Long): Int
    suspend fun exportSingleLesson(lesson: Lesson, lessonDate: java.time.LocalDate, lessonNumber: String, calendarId: Long): Boolean
}