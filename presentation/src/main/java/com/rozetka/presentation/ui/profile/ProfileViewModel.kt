package com.rozetka.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.UsersRepository
import com.rozetka.model.User
import com.rozetka.domain.util.StringObject.ApiToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()


    init {
        getProfile()
    }

    fun getProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            usersRepository.getUserProfile(ApiToken)
                .collect { result ->
                    result.fold(
                        onSuccess = { payData ->
                            _uiState.value = ProfileUiState.Success(payData)
                        },
                        onFailure = { throwable ->
                            _uiState.value = ProfileUiState.Error("error: ${throwable.message}")
                        }
                    )
                }
        }
    }
}


sealed interface ProfileUiState {
    data class Success(val data: User) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    object Loading : ProfileUiState
}