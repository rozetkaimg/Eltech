package com.rozetka.presentation.ui.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject.ApiToken
import com.rozetka.model.MessageModelItem
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val repository: MospolytechMethods
) : ViewModel() {

    private val _uiState = MutableStateFlow<MessagesUiState>(MessagesUiState.Loading)
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        getMessages()
    }

    fun getMessages() {
        viewModelScope.launch {
            _uiState.value = MessagesUiState.Loading
            try {

                val messages = repository.getMsgDialogues(ApiToken)
                _uiState.value = MessagesUiState.Success(messages)
            } catch (e: Exception) {
                _uiState.value = MessagesUiState.Error(
                    e.message ?: "Неизвестная ошибка загрузки сообщений."
                )
            }
        }
    }
}

sealed interface MessagesUiState {
    data class Success(val data: List<MessageModelItem>) : MessagesUiState
    data class Error(val message: String) : MessagesUiState
    object Loading : MessagesUiState
}