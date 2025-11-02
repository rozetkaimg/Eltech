package com.rozetka.presentation.ui.dialog



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.model.MessageDialogItem
import com.rozetka.network.MospolytechMethods
import com.rozetka.domain.util.StringObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DialogViewModel(
    private val repository: MospolytechMethods
) : ViewModel() {

    private val _uiState = MutableStateFlow<DialogUiState>(DialogUiState.Loading)
    val uiState: StateFlow<DialogUiState> = _uiState.asStateFlow()

    fun getDialogMessages(userId: String) {
        viewModelScope.launch {
            _uiState.value = DialogUiState.Loading
            try {
                val messages = repository.getDialogByID(StringObject.ApiToken, userId)
                _uiState.value = DialogUiState.Success(messages)
            } catch (e: Exception) {
                _uiState.value = DialogUiState.Error(
                    e.message ?: "Неизвестная ошибка загрузки диалога."
                )
            }
        }
    }
}

sealed interface DialogUiState {
    data class Success(val data: List<MessageDialogItem>) : DialogUiState
    data class Error(val message: String) : DialogUiState
    object Loading : DialogUiState
}