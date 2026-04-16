package com.rozetka.data.repository

import com.rozetka.domain.repository.GroupJournalRepository
import com.rozetka.model.StudentResponse
import com.rozetka.network.MospolytechMethods

class GroupJournalRepositoryImpl(
    private val api: MospolytechMethods
) : GroupJournalRepository {
    override suspend fun getGroupJournal(group: String): StudentResponse {
        return api.getPhysedJournal(group = group, token = "")
    }
}
