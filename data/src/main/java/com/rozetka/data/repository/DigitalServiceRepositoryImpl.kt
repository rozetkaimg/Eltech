package com.rozetka.data.repository

import com.rozetka.domain.repository.DigitalServiceRepository
import com.rozetka.model.DigitalServiceModelItem
import com.rozetka.network.MospolytechMethods

class DigitalServiceRepositoryImpl(
    private val api: MospolytechMethods
) : DigitalServiceRepository {
    override suspend fun getAppRequests(token: String): List<DigitalServiceModelItem> {
        return api.getAppRequests(token)
    }
}
