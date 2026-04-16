package com.rozetka.data.repository

import com.rozetka.domain.repository.PhysEdJournalRepository
import com.rozetka.model.PhysEdJournalResponse
import com.rozetka.network.MospolytechMethods

class PhysEdJournalRepositoryImpl(
    private val api: MospolytechMethods
) : PhysEdJournalRepository {
    override suspend fun getStudentJournal(studentGuid: String): PhysEdJournalResponse {
        return api.getPhysedjourna(studentGuid)
    }
}
