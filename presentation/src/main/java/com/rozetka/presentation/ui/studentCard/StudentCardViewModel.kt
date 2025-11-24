package com.rozetka.presentation.ui.studentCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.UsersRepository
import com.rozetka.model.User
import com.rozetka.domain.util.StringObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StudentCardUiState {
    data class Success(val data: User) : StudentCardUiState
    data class Error(val message: String) : StudentCardUiState
    object Loading : StudentCardUiState
}

class StudentCardViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentCardUiState>(StudentCardUiState.Loading)
    val uiState: StateFlow<StudentCardUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            usersRepository.getUserProfile(token = StringObject.ApiToken)
                .collect { result ->
                    result.fold(
                        onSuccess = { userModel ->
                            _uiState.value = StudentCardUiState.Success(userModel)
                        },
                        onFailure = { throwable ->
                            _uiState.value = StudentCardUiState.Error("Ошибка: ${throwable.message}")
                        }
                    )
                }
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            try {
                usersRepository.changeEmail(token = StringObject.ApiToken, newEmail = newEmail)
                refreshData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePhone(newPhone: String) {
        viewModelScope.launch {
            try {
                usersRepository.changeNumber(token = StringObject.ApiToken, number = newPhone)
                refreshData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateAvatar(avatarBytes: ByteArray) {
        viewModelScope.launch {
            try {
                usersRepository.changeAvatar(token = StringObject.ApiToken, avatarBytes = avatarBytes)
                refreshData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}