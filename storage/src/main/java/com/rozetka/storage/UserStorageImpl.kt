package com.rozetka.storage

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
import androidx.security.crypto.EncryptedSharedPreferences.create
import androidx.security.crypto.MasterKey
import com.rozetka.storage.repository.UserStorage

class UserStorageImpl(context: Context) : UserStorage {


    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = create(
        context,
        "user_secure_prefs",
        masterKey,
        PrefKeyEncryptionScheme.AES256_SIV,
        AES256_GCM
    )

    companion object {
        private const val KEY_LOGIN = "LOGIN"
        private const val KEY_PASSWORD = "PASSWORD"
        private const val KEY_AUTH_TOKEN = "AUTH_TOKEN"
    }

    override fun saveLogin(login: String) {
        sharedPreferences.edit { putString(KEY_LOGIN, login) }
    }

    override fun getLogin(): String? {
        return sharedPreferences.getString(KEY_LOGIN, null)
    }

    override fun savePassword(password: String) {
        sharedPreferences.edit { putString(KEY_PASSWORD, password) }
    }

    override fun getPassword(): String? {
        return sharedPreferences.getString(KEY_PASSWORD, null)
    }

    override fun saveAuthToken(token: String) {
        sharedPreferences.edit { putString(KEY_AUTH_TOKEN, token) }
    }

    override fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    override fun clear() {
        sharedPreferences.edit { clear() }
    }
}