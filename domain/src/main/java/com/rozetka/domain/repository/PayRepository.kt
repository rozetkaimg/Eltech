package com.rozetka.domain.repository

import com.rozetka.model.PayModel

interface PayRepository {
    suspend fun getPayInfo(token: String): PayModel
}
