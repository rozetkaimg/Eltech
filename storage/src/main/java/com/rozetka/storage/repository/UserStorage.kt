package com.rozetka.storage.repository

interface UserStorage {
    fun saveLogin(login: String)
    fun getLogin(): String?

    fun savePassword(password: String)
    fun getPassword(): String?

    fun saveAuthToken(token: String)
    fun getAuthToken(): String?

    fun clear()
}