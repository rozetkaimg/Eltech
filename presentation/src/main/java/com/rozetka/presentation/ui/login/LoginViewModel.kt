package com.rozetka.presentation.ui.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.data.SecureStorage
import com.rozetka.domain.ErrorType
import com.rozetka.domain.ResultWrapper
import com.rozetka.domain.ResultWrapperLogin
import com.rozetka.domain.UserDataHolder
import com.rozetka.domain.repository.AppRepository
import com.rozetka.domain.repository.LoginRepository
import com.rozetka.domain.repository.UsersRepository
import com.rozetka.domain.util.StringObject
import com.rozetka.domain.util.StringObject.ApiToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class LoginUiState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val error: String? = null,
    val showUnofficialAppWarning: Boolean = false
)

class LoginViewModel(
    private val loginRepository: LoginRepository,
    application: Application,
    private val userRepository: AppRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {
    private val secureStorage: SecureStorage = SecureStorage(application)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onLoginChanged(login: String) {
        _uiState.update { it.copy(login = login, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onErrorDialogDismissed() {
        _uiState.update { it.copy(error = null) }
    }

    fun onLoginClicked() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val login = _uiState.value.login
        val password = _uiState.value.password

        when (val loginResult = loginRepository.signIn(login, password)) {
            is ResultWrapperLogin.Success -> {
                val token = loginResult.data.token
                secureStorage.saveLogin(login)
                secureStorage.savePassword(password)
                secureStorage.saveToken(token)
                ApiToken = token

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showUnofficialAppWarning = true
                    )
                }
            }
            is ResultWrapperLogin.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = mapLoginErrorTypeToMessage(loginResult.errorType)
                    )
                }
            }
        }
    }

    fun onWarningAccepted() = viewModelScope.launch {
        _uiState.update { it.copy(showUnofficialAppWarning = false, isLoading = true) }

        val finalResult = fetchUserDataAndCompleteLogin()

        _uiState.update { currentState ->
            when (finalResult) {
                is ResultWrapper.Success -> {
                    currentState.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        error = null
                    )
                }
                is ResultWrapper.Error -> {
                    currentState.copy(
                        isLoading = false,
                        error = finalResult.message
                    )
                }
            }
        }
    }

    fun onWarningDismissed() {
        _uiState.update { it.copy(showUnofficialAppWarning = false) }
        secureStorage.saveToken("")
        secureStorage.saveLogin("")
        secureStorage.savePassword("")
        ApiToken = ""
    }

    private suspend fun fetchUserDataAndCompleteLogin(): ResultWrapper<Unit> {
        var profileLoaded = false
        var studentDataLoaded = false
        var errorMessage: String? = null

        try {
            coroutineScope {
                val profileJob = launch {
                    usersRepository.getUserProfileOnlyNetwork(ApiToken).first()
                        .onSuccess { data ->
                            secureStorage.saveGroupName(data.group)
                            StringObject.groupName = data.group
                            StringObject.userId = data.id
                            StringObject.Name = data.name
                            StringObject.SurName = data.surname
                            UserDataHolder().saveUserData(data.group, data.name, data.surname, "")
                            profileLoaded = true
                        }
                        .onFailure {
                            if (errorMessage == null) errorMessage = it.message ?: "Ошибка загрузки профиля"
                        }
                }

                val studentDataJob = launch {
                    userRepository.getStudentDataOnlyNetwork(ApiToken).first()
                        .onSuccess { data ->
                            StringObject.guid = data.guidPerson
                            studentDataLoaded = true
                        }
                        .onFailure {
                            if (errorMessage == null) errorMessage = it.message ?: "Ошибка данных студента"
                        }
                }
            }

            return if (profileLoaded && studentDataLoaded) {
                ResultWrapper.Success(Unit)
            } else {
                ResultWrapper.Error(errorMessage ?: "Не удалось загрузить данные")
            }

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val message = if (e is IOException) "Нет подключения к сети" else (e.message ?: "Неизвестная ошибка")
            return ResultWrapper.Error(message)
        }
    }

    private fun mapLoginErrorTypeToMessage(errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.Auth -> "Неверный логин или пароль"
            ErrorType.Network -> "Нет подключения к сети"
            ErrorType.Server -> "Ошибка на стороне сервера"
            is ErrorType.Unknown -> errorType.message
        }
    }
}