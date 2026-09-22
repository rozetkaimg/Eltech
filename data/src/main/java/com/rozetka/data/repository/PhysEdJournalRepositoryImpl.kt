package com.rozetka.data.repository

import com.rozetka.domain.repository.PhysEdJournalRepository
import com.rozetka.model.PhysEdJournalResponse
import com.rozetka.model.PhysEdScheduleResponse
import com.rozetka.network.MospolytechMethods

class PhysEdJournalRepositoryImpl(
    private val api: MospolytechMethods
) : PhysEdJournalRepository {

    override suspend fun getStudentJournal(studentGuid: String): PhysEdJournalResponse {
        return api.getPhysedjourna(studentGuid)
    }

    override suspend fun getPhysEdSchedule(): PhysEdScheduleResponse {
        return api.getPhysEdSchedule()
    }
}
