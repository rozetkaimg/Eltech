package com.rozetka.data.repository

import com.rozetka.domain.repository.ProjectActivityRepository
import com.rozetka.model.PDModel
import com.rozetka.network.MospolytechMethods

class ProjectActivityRepositoryImpl(
    private val api: MospolytechMethods
) : ProjectActivityRepository {
    override suspend fun getProjectActivity(token: String): PDModel {
        return api.getPDInfo(token)
    }
}
