package com.rozetka.presentation.ui.settings

import android.annotation.SuppressLint
import android.app.Application
import android.content.IntentSender
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.data.SecureStorage
import com.rozetka.domain.util.StringObject
import com.rozetka.localdata.SettingsData
import com.rozetka.presentation.R
import com.rozetka.presentation.util.ThemeObject.ColorThemeState
import com.rozetka.presentation.util.ThemeObject.NavBarType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.rustore.sdk.appupdate.listener.InstallStateUpdateListener
import ru.rustore.sdk.appupdate.manager.RuStoreAppUpdateManager
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.AppUpdateInfo
import ru.rustore.sdk.appupdate.model.AppUpdateOptions
import ru.rustore.sdk.appupdate.model.AppUpdateType
import ru.rustore.sdk.appupdate.model.InstallStatus
import ru.rustore.sdk.appupdate.model.UpdateAvailability

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private var settingsData: SettingsData = SettingsData(context)
    private val appUpdateManager: RuStoreAppUpdateManager =
        RuStoreAppUpdateManagerFactory.create(context)
// SettingsViewModel.kt

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            val secureStorage = SecureStorage(context)

            // 1. Очищаем сохраненные учетные данные и токен
            secureStorage.clearCredentials()
            secureStorage.saveToken("")
            secureStorage.saveGroupName("")

            // 2. Сбрасываем глобальное состояние приложения
            StringObject.isGuest = true
            StringObject.ApiToken = ""
            StringObject.groupName = ""
            StringObject.userId = 0
            StringObject.Name = ""
            StringObject.SurName = ""
            StringObject.guid = ""

            // 3. Выполняем колбэк для навигации
            onComplete()
        }
    }
    private val listener = InstallStateUpdateListener { state ->
        _installStatus.value = state.installStatus
        when (state.installStatus) {
            InstallStatus.DOWNLOADED -> {
                Toast.makeText(context, context.getString(R.string.update_downloaded), Toast.LENGTH_SHORT).show()
            }

            InstallStatus.DOWNLOADING -> {
                val totalBytes = state.totalBytesToDownload
                val bytesDownloaded = state.bytesDownloaded
                Log.d(TAG, "Загрузка: $bytesDownloaded / $totalBytes")
            }

            InstallStatus.FAILED -> {
                Toast.makeText(context, context.getString(R.string.update_download_error), Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Downloading error")
            }
        }
    }
    fun setNavBar(navbar: Boolean) {
        viewModelScope.launch {
            SecureStorage(context).saveNavBarState(navbar)
            NavBarType.value = navbar
        }
    }
    fun getNavBar(): Boolean {
        return SecureStorage(context).getNavBarState()
    }
    fun getPhoto(): String {
        return SecureStorage(context).getProfilePhoto().toString()
    }
    fun getName(): String {
        return SecureStorage(context).getName().toString()
    }
    fun getGroup(): String {
        return SecureStorage(context).getGroupName().toString()
    }

    private val _notificationState = MutableStateFlow(SecureStorage(context).getScheduleNotificationState())
    val notificationState: StateFlow<Boolean> = _notificationState.asStateFlow()

    fun saveScheduleNotificationState(state: Boolean) {
        SecureStorage(context).saveScheduleNotificationState(state)
        _notificationState.value = state
    }

    fun onNotificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            saveScheduleNotificationState(true)
        } else {
            Toast.makeText(context, context.getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    fun getScheduleState(): Boolean = SecureStorage(context).getScheduleState()
    fun saveScheduleState(state: Boolean) {
        SecureStorage(context).saveScheduleState(state)
    }
    init {
        appUpdateManager.registerListener(listener)
    }

    private val _updateIntentSender = MutableStateFlow<IntentSender?>(null)
    val updateIntentSender = _updateIntentSender.asStateFlow()

    private val _installStatus = MutableStateFlow<Int?>(null)
    val installStatus = _installStatus.asStateFlow()

    companion object {
        private const val TAG = "RuStoreUpdateViewModel"
    }


    fun checkForUpdate() {
        Toast.makeText(context, context.getString(R.string.checking_for_updates), Toast.LENGTH_SHORT).show()

        appUpdateManager.getAppUpdateInfo()
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        Log.d(TAG, "Доступно гибкое обновление.")
                        Toast.makeText(context, context.getString(R.string.update_found), Toast.LENGTH_SHORT).show()
                        startUpdate(appUpdateInfo)
                    } else {
                        Log.d(TAG, "Гибкое обновление не разрешено.")
                        Toast.makeText(context, context.getString(R.string.latest_version_installed), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "Обновление не найдено.")
                    Toast.makeText(context, context.getString(R.string.latest_version_installed), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { throwable ->
                val errorMessage = "Ошибка при проверке обновлений: ${throwable.message}"
                Log.e(TAG, errorMessage, throwable)
                Toast.makeText(context, context.getString(R.string.update_check_failed), Toast.LENGTH_LONG).show()
            }
    }
    private fun startUpdate(appUpdateInfo: AppUpdateInfo) {
        val options = AppUpdateOptions.Builder()
            .appUpdateType(AppUpdateType.FLEXIBLE)
            .build()

        appUpdateManager.startUpdateFlow(appUpdateInfo, options)
            .addOnSuccessListener { intentSender ->

            }
            .addOnFailureListener { throwable ->
                val errorMessage = "Ошибка при запуске процесса обновления: ${throwable.message}"
                Log.e(TAG, errorMessage, throwable)
                Toast.makeText(context, context.getString(R.string.update_start_failed), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun completeUpdate() {
        Log.d(TAG, "Попытка завершить обновление.")
        val options = AppUpdateOptions.Builder()
            .appUpdateType(AppUpdateType.FLEXIBLE)
            .build()
        appUpdateManager.completeUpdate(options)
    }

    fun resetUpdateIntentSender() {
        _updateIntentSender.value = null
    }
    fun setMonetState(monetState: Boolean) {
        viewModelScope.launch {
            settingsData.saveMonetState(monetState, context)
        }
    }

    fun setThemeState(themeState: Int) {
        viewModelScope.launch {
            ColorThemeState.value = themeState
            settingsData.saveThemeState(themeState, context)
        }
    }
}