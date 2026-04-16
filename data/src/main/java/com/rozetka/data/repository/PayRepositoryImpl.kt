package com.rozetka.data.repository

import com.rozetka.domain.repository.PayRepository
import com.rozetka.model.PayModel
import com.rozetka.network.MospolytechMethods

class PayRepositoryImpl(
    private val api: MospolytechMethods
) : PayRepository {
    override suspend fun getPayInfo(token: String): PayModel {
        return api.getPayInfo(token)
    }
}
