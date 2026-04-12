package com.rozetka.presentation.ui.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject.ApiToken
import com.rozetka.model.MessageModelItem
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val repository: MospolytechMethods
) : ViewModel() {

    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        getMessages()
    }

    fun getMessages() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _uiState.value = MessagesUiState.Loading
            while (isActive) {
                try {
                    val newMessages = repository.getMsgDialogues(ApiToken)
                    val currentState = _uiState.value

                    if (currentState is MessagesUiState.Success) {
                        if (currentState.data != newMessages) {
                            _uiState.value = MessagesUiState.Success(newMessages)
                        }
                    } else {
                        _uiState.value = MessagesUiState.Success(newMessages)
                    }
                } catch (e: Exception) {
                    if (_uiState.value !is MessagesUiState.Success) {
                        _uiState.value = MessagesUiState.Error(
                            e.message ?: "Неизвестная ошибка загрузки сообщений."
                        )
                    }
                }
                delay(5000L)
            }
        }
    }
}

sealed interface MessagesUiState {
    data class Success(val data: List<MessageModelItem>) : MessagesUiState
    data class Error(val message: String) : MessagesUiState
    object Loading : MessagesUiState
}