package com.rozetka.storage.repository


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_FILENAME = "com.rozetka.prefs"
        private const val KEY_USER_ID = "user_id"
    }

    fun saveUserId(userId: String) {
        prefs.edit {
            putString(KEY_USER_ID, userId)
        }
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun clearSession() {
        prefs.edit {
            clear()
        }
    }
}