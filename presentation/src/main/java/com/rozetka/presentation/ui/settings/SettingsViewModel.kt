package com.rozetka.presentation.ui.settings

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.IntentSender
import android.os.Build
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
import com.rozetka.model.UserAccount

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val secureStorage = SecureStorage(application)
    private val _accounts = MutableStateFlow<List<UserAccount>>(secureStorage.getUserAccounts())
    val accounts: StateFlow<List<UserAccount>> = _accounts.asStateFlow()

    fun switchAccount(account: UserAccount, onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentAccounts = secureStorage.getUserAccounts()
            
            // Update active status in the list
            val updatedAccounts = currentAccounts.map {
                it.copy(isActive = it.login == account.login)
            }
            secureStorage.saveUserAccounts(updatedAccounts)
            _accounts.value = updatedAccounts

            // Set as active credentials
            secureStorage.saveLogin(account.login)
            secureStorage.savePassword(account.password)
            secureStorage.saveToken(account.token)
            secureStorage.saveName(account.name)
            secureStorage.saveGroupName(account.group)
            secureStorage.saveProfilePhoto(account.avatar)
            
            // Critical: Reset global StringObject to prevent residual data
            StringObject.ApiToken = account.token
            StringObject.groupName = account.group
            StringObject.Name = account.name.substringBefore(" ")
            StringObject.SurName = account.name.substringAfter(" ", "")
            StringObject.avatar = account.avatar
            StringObject.isGuest = false

            onComplete()
        }
    }

    fun removeAccount(account: UserAccount) {
        viewModelScope.launch {
            val updatedAccounts = _accounts.value.filter { it.login != account.login }
            secureStorage.saveUserAccounts(updatedAccounts)
            _accounts.value = updatedAccounts
            
            if (account.isActive && updatedAccounts.isNotEmpty()) {
                switchAccount(updatedAccounts.first()) {}
            } else if (updatedAccounts.isEmpty()) {
                logout {}
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private var settingsData: SettingsData = SettingsData(context)
    private val appUpdateManager: RuStoreAppUpdateManager =
        RuStoreAppUpdateManagerFactory.create(context)
// SettingsViewModel.kt

    fun logout(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val secureStorage = SecureStorage(context)
            val currentAccounts = secureStorage.getUserAccounts()
            val activeAccount = currentAccounts.find { it.isActive }
            
            // 1. Remove the active account from the list
            val updatedAccounts = currentAccounts.filter { !it.isActive }
            secureStorage.saveUserAccounts(updatedAccounts)
            _accounts.value = updatedAccounts

            if (updatedAccounts.isNotEmpty()) {
                // 2. Switch to the next available account
                switchAccount(updatedAccounts.first()) {
                    onComplete(true) // Stay on settings/main
                }
            } else {
                // 3. Last account logout - clean everything and go to login
                secureStorage.clearCredentials()
                secureStorage.saveToken("")
                secureStorage.saveGroupName("")

                StringObject.isGuest = true
                StringObject.ApiToken = ""
                StringObject.groupName = ""
                StringObject.userId = 0
                StringObject.Name = ""
                StringObject.SurName = ""
                StringObject.guid = ""

                onComplete(false) // Navigate to login
            }
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

    private val _notificationState = MutableStateFlow(secureStorage.getScheduleNotificationState())
    val notificationState: StateFlow<Boolean> = _notificationState.asStateFlow()

    private val _chatNotificationState = MutableStateFlow(secureStorage.getChatNotificationState())
    val chatNotificationState: StateFlow<Boolean> = _chatNotificationState.asStateFlow()

    fun saveScheduleNotificationState(state: Boolean) {
        secureStorage.saveScheduleNotificationState(state)
        _notificationState.value = state
        
        val intent = Intent()
        intent.setClassName(context.packageName, "com.rozetka.epolitech.services.LessonNotificationService")
        if (state) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            context.stopService(intent)
        }
    }

    fun saveChatNotificationState(state: Boolean) {
        secureStorage.saveChatNotificationState(state)
        _chatNotificationState.value = state
        
        val intent = Intent()
        intent.setClassName(context.packageName, "com.rozetka.epolitech.services.MessageNotificationService")
        if (state) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            context.stopService(intent)
        }
    }

    fun onNotificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            saveScheduleNotificationState(true)
            saveChatNotificationState(true)
        } else {
            Toast.makeText(context, context.getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    fun getScheduleState(): Boolean = secureStorage.getScheduleState()
    fun saveScheduleState(state: Boolean) {
        secureStorage.saveScheduleState(state)
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