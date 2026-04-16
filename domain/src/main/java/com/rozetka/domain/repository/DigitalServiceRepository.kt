package com.rozetka.domain.repository

import com.rozetka.model.DigitalServiceModelItem

interface DigitalServiceRepository {
    suspend fun getAppRequests(token: String): List<DigitalServiceModelItem>
}
