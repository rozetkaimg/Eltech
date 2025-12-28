package com.rozetka.localdata

import android.content.Context

class SettingsData(context: Context): SettingsStoreInt {
    val dataStore = DataStoreManager.getSettingsDataStore(context)
    val settingsStorage = SettingsStorage(dataStore)
    override suspend fun saveThemeState(themeState: Int, context: Context) {
        settingsStorage.saveThemeState(themeState)
    }

    override suspend fun getThemeState(context: Context): Int {
 return settingsStorage.getThemeState()
    }

    override suspend fun getMonetState(context: Context): Boolean {
        return settingsStorage.getMonetState()
    }

    override suspend fun saveMonetState(monetState: Boolean, context: Context) {
       settingsStorage.saveMonetState(monetState)
    }

    override suspend fun saveDynamicColorState(dynamicColorState: Boolean) {
        return settingsStorage.saveDynamicColorState(dynamicColorState)
    }

    override suspend fun saveMonetStaticColor(static: Int) {
        settingsStorage.saveMonetStaticColor(static)
    }

    override suspend fun getMonetStaticColor(context: Context): Int {
        return settingsStorage.getMonetStaticColor(context)
    }
    suspend fun saveGroupName(groupName: String) {
        settingsStorage.saveGroupName(groupName)

    }

    suspend fun getGroupName(): String {
        return settingsStorage.getGroupName()
    }
}