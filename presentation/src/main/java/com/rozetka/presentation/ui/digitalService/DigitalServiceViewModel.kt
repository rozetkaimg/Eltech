package com.rozetka.presentation.ui.digitalService

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.DigitalServiceRepository
import com.rozetka.domain.util.StringObject
import com.rozetka.model.DigitalServiceModelItem
import com.rozetka.presentation.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DigitalServiceUiState {
    data class Success(val requests: List<DigitalServiceModelItem>) : DigitalServiceUiState
    data class Error(val message: String) : DigitalServiceUiState
    object Loading : DigitalServiceUiState
}

class DigitalServiceViewModel(
    private val repository: DigitalServiceRepository,
    private val application: Application
) : ViewModel() {

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
                _uiState.value = DigitalServiceUiState.Error(
                    application.getString(
                        R.string.error_load_prefix,
                        e.message ?: application.getString(R.string.error_unknown)
                    )
                )
            }
        }
    }
}
