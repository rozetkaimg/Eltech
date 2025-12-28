package com.rozetka.presentation.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.data.SecureStorage
import com.rozetka.domain.ErrorType
import com.rozetka.domain.ResultWrapperLogin
import com.rozetka.domain.UserDataHolder
import com.rozetka.domain.repository.AppRepository
import com.rozetka.domain.repository.LoginRepository
import com.rozetka.domain.repository.UsersRepository
import com.rozetka.domain.util.StringObject
import com.rozetka.domain.util.StringObject.ApiToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class RootViewModel(
    private val loginRepository: LoginRepository,
    private val userRepository: AppRepository,
    private val usersRepository: UsersRepository,
    application: Application
) : ViewModel() {
    private val _startDestination = MutableStateFlow("")
    val startDestination = _startDestination.asStateFlow()
    private val secureStorage: SecureStorage = SecureStorage(application)

    companion object {
        const val LOGIN_ROUTE = "login_graph"
        const val MAIN_ROUTE = "main_graph"
    }

    init {
        checkAuthenticationState()
    }

    private fun checkAuthenticationState() {
        viewModelScope.launch {
            val savedLogin = secureStorage.getLogin()
            val savedPassword = secureStorage.getPassword()
            val savedToken = secureStorage.getToken()
            if (!secureStorage.getGroupName().isNullOrEmpty()) {
                StringObject.groupName = secureStorage.getGroupName().orEmpty()
                _startDestination.value = MAIN_ROUTE
            }
            if (savedLogin.isNullOrBlank() || savedPassword.isNullOrBlank()) {
                StringObject.isGuest = true
                _startDestination.value = MAIN_ROUTE
                return@launch
            }

            var authFailed = false
            val loginResult = loginRepository.signIn(savedLogin, savedPassword)

            val tokenToUse: String? = when (loginResult) {
                is ResultWrapperLogin.Success -> {
                    val newToken = loginResult.data?.token
                    if (newToken != null) {
                        secureStorage.saveToken(newToken)
                        ApiToken = newToken
                        newToken
                    } else {
                        authFailed = true
                        null
                    }
                }

                is ResultWrapperLogin.Error -> {
                    when (loginResult.errorType) {
                        is ErrorType.Auth -> {
                            authFailed = true
                            null
                        }
                        is ErrorType.Network, is ErrorType.Server, is ErrorType.Unknown -> {
                            if (!savedToken.isNullOrBlank()) {
                                ApiToken = savedToken
                                savedToken
                            } else {
                                null
                            }
                        }
                    }
                }
            }

            if (tokenToUse != null) {
                var profileLoaded = false
                var studentDataLoaded = false

                val profileJob = launch {
                    usersRepository.getUserProfile(tokenToUse).firstOrNull()
                        ?.onSuccess { data ->
                            secureStorage.saveGroupName(data.group)
                            StringObject.groupName = data.group
                            StringObject.userId = data.id
                            StringObject.Name = data.name
                            StringObject.SurName = data.surname
                            UserDataHolder().saveUserData(data.group, data.name, data.surname, "")
                            profileLoaded = true
                        }
                }

                val studentDataJob = launch {
                    userRepository.getStudentData(tokenToUse).firstOrNull()
                        ?.onSuccess { data ->
                            StringObject.guid = data.guidPerson
                            studentDataLoaded = true
                        }
                }

                joinAll(profileJob, studentDataJob)

                if (profileLoaded) {
                    StringObject.isGuest = false
                    _startDestination.value = MAIN_ROUTE
                } else {
                    _startDestination.value = LOGIN_ROUTE
                }

            } else {
                if (authFailed) {
                    secureStorage.clearCredentials()
                }
                StringObject.isGuest = true
                _startDestination.value = MAIN_ROUTE
            }
        }
    }
}