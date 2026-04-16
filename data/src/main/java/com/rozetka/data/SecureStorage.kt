package com.rozetka.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences.*
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.*
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.*
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKey.*
import kotlinx.serialization.json.Json
import java.lang.Exception

class SecureStorage(context: Context) {

    private companion object {
        const val FILE_NAME = "secure_prefs_file"
        const val NAVIGATION_STATE = "navbar_state"
        const val KEY_LOGIN = "login_key"
        const val KEY_PASSWORD = "user_password_key"
        const val KEY_TOKEN = "token_key"

        const val KEY_USER_NAME = "user_name_key"
        const val KEY_PROFILE_PHOTO = "user_Profile_photo"
        const val KEY_STUDENT_ID = "student_key"
        const val KEY_GROUP_ID = "group_key"
        const val KEY_GROUP_NAME = "group_name_key"

        const val KEY_SCHEDULE_STATE = "Schedule_key"
        const val KEY_SCHEDULE_NOTIFICATION_STATE = "ScheduleNotification_key"
        const val KEY_CHAT_NOTIFICATION_STATE = "ChatNotification_key"

        const val KEY_GROUP_NAMES_LIST = "group_names_list_key"
        const val KEY_USER_ACCOUNTS = "user_accounts_key"
    }

    private val masterKey: MasterKey = Builder(context)
        .setKeyScheme(KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = create(
        context,
        FILE_NAME,
        masterKey,
        AES256_SIV,
        AES256_GCM
    )

    private val json = Json

    private fun saveString(key: String, value: String?) {
        sharedPreferences.edit { putString(key, value) }
    }

    private fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    private fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }

    private fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    private fun remove(key: String) {
        sharedPreferences.edit { remove(key) }
    }

    fun saveLogin(login: String) = saveString(KEY_LOGIN, login)
    fun getLogin(): String? = getString(KEY_LOGIN)

    fun savePassword(password: String) = saveString(KEY_PASSWORD, password)
    fun getPassword(): String? = getString(KEY_PASSWORD)

    fun saveToken(token: String) = saveString(KEY_TOKEN, token)
    fun getToken(): String? = getString(KEY_TOKEN)

    fun clearLogin() = remove(KEY_LOGIN)
    fun clearPassword() = remove(KEY_PASSWORD)
    fun clearToken() = remove(KEY_TOKEN)

    fun clearCredentials() {
        sharedPreferences.edit {
            remove(KEY_LOGIN)
            remove(KEY_PASSWORD)
            remove(KEY_TOKEN)
        }
    }

    fun areCredentialsEmpty(): Boolean {
        return getLogin().isNullOrEmpty() ||
                getPassword().isNullOrEmpty() ||
                getToken().isNullOrEmpty()
    }

    fun saveProfilePhoto(photo: String) = saveString(KEY_PROFILE_PHOTO, photo)
    fun getProfilePhoto(): String? = getString(KEY_PROFILE_PHOTO)

    fun saveGroupName(group: String) = saveString(KEY_GROUP_NAME, group)
    fun getGroupName(): String? = getString(KEY_GROUP_NAME)

    fun saveName(name: String) = saveString(KEY_USER_NAME, name)
    fun getName(): String? = getString(KEY_USER_NAME)

    fun saveGroupID(groupId: String) = saveString(KEY_GROUP_ID, groupId)
    fun getGroupID(): String? = getString(KEY_GROUP_ID)

    fun saveStudentID(studentId: String) = saveString(KEY_STUDENT_ID, studentId)
    fun getStudentID(): String? = getString(KEY_STUDENT_ID)
    fun saveNavBarState(state: Boolean) = saveBoolean(NAVIGATION_STATE, state)
    fun getNavBarState(): Boolean = getBoolean(NAVIGATION_STATE, false)
    fun saveScheduleState(state: Boolean) = saveBoolean(KEY_SCHEDULE_STATE, state)
    fun getScheduleState(): Boolean = getBoolean(KEY_SCHEDULE_STATE, false)

    fun saveScheduleNotificationState(state: Boolean) =
        saveBoolean(KEY_SCHEDULE_NOTIFICATION_STATE, state)

    fun getScheduleNotificationState(): Boolean = getBoolean(KEY_SCHEDULE_NOTIFICATION_STATE, false)

    fun saveChatNotificationState(state: Boolean) =
        saveBoolean(KEY_CHAT_NOTIFICATION_STATE, state)

    fun getChatNotificationState(): Boolean = getBoolean(KEY_CHAT_NOTIFICATION_STATE, false)

    fun saveGroupNames(groupNames: List<String>) {
        val jsonString = json.encodeToString(groupNames)
        saveString(KEY_GROUP_NAMES_LIST, jsonString)
    }

    fun getGroupNames(): List<String> {
        val jsonString = getString(KEY_GROUP_NAMES_LIST)
        return if (jsonString != null) {
            try {
                json.decodeFromString<List<String>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun saveUserAccounts(accounts: List<com.rozetka.model.UserAccount>) {
        val jsonString = json.encodeToString(accounts)
        saveString(KEY_USER_ACCOUNTS, jsonString)
    }

    fun getUserAccounts(): List<com.rozetka.model.UserAccount> {
        val jsonString = getString(KEY_USER_ACCOUNTS)
        return if (jsonString != null) {
            try {
                json.decodeFromString<List<com.rozetka.model.UserAccount>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun clearAllData() {
        sharedPreferences.edit { clear() }
    }
}