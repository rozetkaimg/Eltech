package com.rozetka.localdata

import android.content.Context

class InterfaceSettingsData(context: Context) {
    val dataStore = DataStoreManager.getSettingsDataStore(context)
    val settingsStorage = SettingsStorage(dataStore)

    suspend fun saveThemeState(themeState: Int) {
        settingsStorage.saveThemeState(themeState)
    }

    suspend fun getThemeState(): Int {
        return settingsStorage.getThemeState()
    }

    suspend fun getMonetState(): Boolean {
        return settingsStorage.getMonetState()
    }

    suspend fun saveMonetState(monetState: Boolean) {
        settingsStorage.saveMonetState(monetState)
    }

    suspend fun saveGroupName(groupName: String) {
        settingsStorage.saveGroupName(groupName)

    }


}