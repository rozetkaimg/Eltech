package com.rozetka.data.repository



import com.rozetka.domain.repository.UserRepository
import com.rozetka.model.AppUserData
import com.rozetka.model.AuthResponseModel
import com.rozetka.model.User
import com.rozetka.network.Methods
import com.rozetka.storage.repository.UserStorage
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val apiMethods: Methods,
    private val userStorage: UserStorage
) : UserRepository {

    override suspend fun signIn(login: String, password: String): AuthResponseModel {
        val response = apiMethods.singIn(login, password)
        userStorage.saveAuthToken(response.authResponseModel?.token ?: "")
        return response.authResponseModel?: AuthResponseModel("", "", "", "")
    }

    override suspend fun getUserData(token: String): AppUserData {
        return apiMethods.getAppUserData(token)
    }

    override fun saveCredentials(login: String, password: String) {
        userStorage.saveLogin(login)
        userStorage.savePassword(password)
    }

    override fun getAuthToken(): String? {
        return userStorage.getAuthToken()
    }

    override fun clearUserData() {
        userStorage.clear()
    }

    override fun getUser(): Flow<User?> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshUser(): Result<Unit> {
        TODO("Not yet implemented")
    }
}