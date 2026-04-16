package com.rozetka.domain.repository

import com.rozetka.model.PhysEdJournalResponse

interface PhysEdJournalRepository {
    suspend fun getStudentJournal(studentGuid: String): PhysEdJournalResponse
}
