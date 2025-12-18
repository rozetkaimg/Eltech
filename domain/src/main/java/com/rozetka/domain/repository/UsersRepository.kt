package com.rozetka.domain.repository

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.rozetka.domain.util.NetworkUtils
import com.rozetka.model.User
import com.rozetka.network.MospolytechMethods
import com.rozetka.storage.database.userProfile.UserProfileStorage
import com.rozetka.storage.repository.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class UsersRepository(
    private val userStorage: UserProfileStorage,
    private val sessionManager: SessionManager,
    private val context: Context,
    private val mospolytechMethods: MospolytechMethods
) {

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getUserProfile(token: String): Flow<Result<User>> = flow {
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                val networkResponse = fetchProfileFromNetwork(token)
                emit(Result.success(networkResponse))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w("UserRepository", "Сетевой запрос не удался, пробуем загрузить из кэша", e)
                emit(loadProfileFromCache())
            }
        } else {
            emit(loadProfileFromCache())
        }
    }

    fun getUserProfileOnlyNetwork(token: String): Flow<Result<User>> = flow {
        try {
            val networkResponse = fetchProfileFromNetwork(token)
            emit(Result.success(networkResponse))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("UserRepository", "Не удалось принудительно загрузить данные из сети", e)
            emit(Result.failure(e))
        }
    }

    suspend fun changeEmail(token: String, newEmail: String): String {
        return mospolytechMethods.changeEmail(token, newEmail)
    }

    suspend fun changeNumber(token: String, number: String): String {
        return mospolytechMethods.changeNumber(token, number)
    }

    suspend fun changeAvatar(token: String, avatarBytes: ByteArray): String {
        return mospolytechMethods.changeAvatar(token, avatarBytes)
    }

    private suspend fun fetchProfileFromNetwork(token: String): User {
        val networkResponse = mospolytechMethods.getUserInfo(token)
        val newUserId = networkResponse.user.id.toString()
        sessionManager.saveUserId(newUserId)

        userStorage.saveUser(networkResponse.user)

        return networkResponse.user
    }

    private suspend fun loadProfileFromCache(): Result<User> {
        val userIdString = sessionManager.getUserId()
            ?: return Result.failure(Exception("Нет кэшированных данных (ID пользователя не найден)."))
        val userIdInt = userIdString.toIntOrNull()
            ?: return Result.failure(Exception("Кэшированный ID пользователя поврежден."))

        return try {
            val cachedData = userStorage.getUser(userIdInt)

            if (cachedData != null) {
                Result.success(cachedData)
            } else {
                Result.failure(Exception("Локальные данные не найдены."))
            }
        } catch (e: Exception) {
            Log.w("UserRepository", "Не удалось прочитать кэш в оффлайн-режиме", e)
            Result.failure(e)
        }
    }
}