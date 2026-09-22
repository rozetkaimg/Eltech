package com.rozetka.domain.repository

import com.rozetka.model.PhysEdJournalResponse
import com.rozetka.model.PhysEdScheduleResponse

interface PhysEdJournalRepository {
    suspend fun getStudentJournal(studentGuid: String): PhysEdJournalResponse
    suspend fun getPhysEdSchedule(): PhysEdScheduleResponse
}
