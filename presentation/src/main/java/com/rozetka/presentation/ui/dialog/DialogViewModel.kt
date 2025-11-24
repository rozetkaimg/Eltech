package com.rozetka.presentation.ui.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject
import com.rozetka.model.MessageDialogItem
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class DialogViewModel(
    private val repository: MospolytechMethods
) : ViewModel() {

    private val _uiState = MutableStateFlow<DialogUiState>(DialogUiState.Loading)
    val uiState: StateFlow<DialogUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    fun startPolling(userId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            loadMessages(userId, isSilent = false)
            while (isActive) {
                delay(5000)
                loadMessages(userId, isSilent = true)
            }
        }
    }

    fun sendMessage(text: String, userId: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                repository.sendMessageNoFiles(
                    toDialog = userId,
                    token = StringObject.ApiToken,
                    newMessage = text
                )
                loadMessages(userId, isSilent = true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun sendFiles(files: List<File>, userId: String, messageText: String) {
        if (files.isEmpty()) return

        viewModelScope.launch {
            try {
                repository.sendMessageWithFiles(
                    toDialog = userId,
                    token = StringObject.ApiToken,
                    message = messageText,
                    files = files
                )
                loadMessages(userId, isSilent = true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadMessages(userId: String, isSilent: Boolean = false) {
        viewModelScope.launch {
            if (!isSilent) {
                _uiState.value = DialogUiState.Loading
            }

            try {
                val messages = repository.getDialogByID(StringObject.ApiToken, userId)
                _uiState.value = DialogUiState.Success(messages)
            } catch (e: Exception) {
                if (!isSilent) {
                    _uiState.value = DialogUiState.Error(
                        e.message ?: "Неизвестная ошибка загрузки диалога."
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

sealed interface DialogUiState {
    data class Success(val data: List<MessageDialogItem>) : DialogUiState
    data class Error(val message: String) : DialogUiState
    object Loading : DialogUiState
}