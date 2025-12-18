package com.rozetka.localdata

import android.content.Context
import androidx.datastore.preferences.core.Preferences

interface SettingsStore {
    suspend fun saveThemeState(themeState: Int)
    suspend fun getThemeState(): Int
    suspend fun getMonetState(): Boolean
    suspend fun saveMonetState(monetState: Boolean)
    suspend fun saveDynamicColorState(dynamicColorState: Boolean)
    suspend fun getDynamicColorState(): Boolean
    suspend fun <T> containsKey(key: Preferences.Key<T>): Boolean
    suspend fun saveMonetStaticColor(static: Int)
    suspend fun getMonetStaticColor(context: Context): Int
    suspend fun saveGroupName(name: String)
    suspend fun getGroupName(): String

}