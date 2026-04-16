package com.rozetka.domain.repository

import com.rozetka.model.PDModel

interface ProjectActivityRepository {
    suspend fun getProjectActivity(token: String): PDModel
}
