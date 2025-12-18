package com.rozetka.domain.repository

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.rozetka.domain.util.NetworkUtils
import com.rozetka.model.UserStudentCard
import com.rozetka.network.MospolytechMethods
import com.rozetka.storage.database.userCard.UserStudentCardStorage

import com.rozetka.storage.repository.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class AppRepository(
    private val userStudentCardStorage: UserStudentCardStorage,
    private val sessionManager: SessionManager,
    private val context: Context,
    private val mospolytechMethods: MospolytechMethods
) {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getStudentData(token: String): Flow<Result<UserStudentCard>> = flow {
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                val networkResponse = fetchFromNetworkAndUpdateCache(token)
                emit(Result.success(networkResponse))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w("AppRepository", "Сетевой запрос не удался, пробуем загрузить из кэша", e)
                emit(loadDataFromCache())
            }
        } else {
            emit(loadDataFromCache())
        }
    }

    fun getStudentDataOnlyNetwork(token: String): Flow<Result<UserStudentCard>> = flow {
        try {
            val networkResponse = fetchFromNetworkAndUpdateCache(token)
            emit(Result.success(networkResponse))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("AppRepository", "Не удалось принудительно загрузить данные из сети", e)
            emit(Result.failure(e))
        }
    }

    private suspend fun fetchFromNetworkAndUpdateCache(token: String): UserStudentCard {
        val networkResponse = mospolytechMethods.getAppData(token)

        val newUserId = networkResponse.guidPerson
        sessionManager.saveUserId(newUserId)

        userStudentCardStorage.saveUserStudentCard(networkResponse)

        return networkResponse
    }

    private suspend fun loadDataFromCache(): Result<UserStudentCard> {
        val userId = sessionManager.getUserId()
            ?: return Result.failure(Exception("Нет кэшированных данных (ID пользователя не найден)."))

        return try {
            val cachedData = userStudentCardStorage.getUserStudentCard(userId)

            if (cachedData != null) {
                Result.success(cachedData)
            } else {
                Result.failure(Exception("Локальные данные не найдены."))
            }
        } catch (e: Exception) {
            Log.w("AppRepository", "Не удалось прочитать кэш в оффлайн-режиме", e)
            Result.failure(e)
        }
    }
}