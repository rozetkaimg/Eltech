package com.rozetka.domain.repository

import com.rozetka.model.StudentResponse

interface GroupJournalRepository {
    suspend fun getGroupJournal(group: String): StudentResponse
}
