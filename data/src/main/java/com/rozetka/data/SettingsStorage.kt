package com.rozetka.localdata

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsStorage(private val dataStore: DataStore<Preferences>) : SettingsStore {


    override suspend fun saveThemeState(themeState: Int) {
        saveData(DataStoreManager.THEME_STATE_KEY, themeState)
    }

    override suspend fun getThemeState(): Int =
        getData(DataStoreManager.THEME_STATE_KEY, 0)

    override suspend fun getMonetState(): Boolean =
        getData(DataStoreManager.MONET_STATE_KEY, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S))

    override suspend fun saveMonetState(monetState: Boolean) {
        saveData(DataStoreManager.MONET_STATE_KEY, monetState)
    }

    override suspend fun saveDynamicColorState(dynamicColorState: Boolean) {
        saveData(DataStoreManager.DYNAMIC_COLOR_STATE_KEY, dynamicColorState)
    }

    override suspend fun getDynamicColorState(): Boolean {
        return getData(DataStoreManager.DYNAMIC_COLOR_STATE_KEY, false)
    }

    private suspend fun <T> saveData(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    private suspend fun <T> getData(key: Preferences.Key<T>, defaultValue: T): T {
        return dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }.first()
    }

    override suspend fun <T> containsKey(key: Preferences.Key<T>): Boolean {
        return dataStore.data.map { preferences ->
            preferences[key] != null
        }.first()
    }

    override suspend fun saveMonetStaticColor(static: Int) {
        saveData(DataStoreManager.MONET_STATIC_COLOR_KEY, static)
    }

    override suspend fun getMonetStaticColor(context: Context): Int {
        return getData(DataStoreManager.MONET_STATIC_COLOR_KEY, 0)
    }
    override suspend fun saveGroupName(name: String) {
        saveData(DataStoreManager.GROUP_NAME, name)
    }

    override suspend fun getGroupName(): String {
        return getData(DataStoreManager.GROUP_NAME, "АБс-425")
    }


}


object DataStoreManager {
    private const val SETTINGS_PREFERENCES_NAME = "settings"
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = SETTINGS_PREFERENCES_NAME
    )

    fun getSettingsDataStore(context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    val THEME_STATE_KEY = intPreferencesKey("theme_state")
    val MONET_STATE_KEY = booleanPreferencesKey("monet_state")
    val DYNAMIC_COLOR_STATE_KEY = booleanPreferencesKey("dynamic_color_state")
    val MONET_STATIC_COLOR_KEY = intPreferencesKey("monet_color")
    val GROUP_ID = stringPreferencesKey("group_id")
    val GROUP_NAME = stringPreferencesKey("group_name")
}