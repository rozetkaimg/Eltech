package com.rozetka.presentation.ui.digitalService


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.model.DigitalServiceModelItem
import com.rozetka.network.MospolytechMethods
import com.rozetka.domain.util.StringObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DigitalServiceUiState {
    data class Success(val requests: List<DigitalServiceModelItem>) : DigitalServiceUiState
    data class Error(val message: String) : DigitalServiceUiState
    object Loading : DigitalServiceUiState
}

class DigitalServiceViewModel : ViewModel() {

    private val repository = MospolytechMethods()

    private val _uiState = MutableStateFlow<DigitalServiceUiState>(DigitalServiceUiState.Loading)
    val uiState: StateFlow<DigitalServiceUiState> = _uiState.asStateFlow()

    init {
        loadAppRequests()
    }

    fun loadAppRequests() {
        viewModelScope.launch {
            _uiState.value = DigitalServiceUiState.Loading
            try {
                val token = StringObject.ApiToken
                val requestsData = repository.getAppRequests(token)
                _uiState.value = DigitalServiceUiState.Success(requestsData)
            } catch (e: Exception) {
                _uiState.value = DigitalServiceUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
