package com.rozetka.domain.repository

import com.rozetka.model.AppUserData
import com.rozetka.model.AuthResponseModel
import com.rozetka.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun signIn(login: String, password: String): AuthResponseModel
    suspend fun getUserData(token: String): AppUserData

    fun saveCredentials(login: String, password: String)
    fun getAuthToken(): String?
    fun clearUserData()
    fun getUser(): Flow<User?>
    suspend fun refreshUser(): Result<Unit>
    suspend fun getMoodleSession(): String?
    suspend fun saveMoodleSession(session: String)
}
