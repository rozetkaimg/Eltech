package com.rozetka.domain.usecase


import com.rozetka.domain.repository.CalendarRepository
import com.rozetka.model.ScheduleModel
import com.rozetka.model.Lesson
import com.rozetka.model.local.CalendarAccount
import java.time.LocalDate

class CalendarUseCase(private val repository: CalendarRepository) {

    suspend fun getCalendars(): List<CalendarAccount> {
        return repository.getAvailableCalendars()
    }

    suspend fun exportSchedule(schedule: ScheduleModel, calendarId: Long): Int {
        return repository.exportSchedule(schedule, calendarId)
    }

}