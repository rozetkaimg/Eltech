package com.rozetka.presentation.ui.pay

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.PayRepository
import com.rozetka.domain.util.StringObject
import com.rozetka.model.PayModel
import com.rozetka.presentation.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PayViewModel(
    private val repository: PayRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<PayUiState>(PayUiState.Loading)
    val uiState: StateFlow<PayUiState> = _uiState.asStateFlow()

    init {
        getPayInfo()
    }

    fun getPayInfo() {
        viewModelScope.launch {
            _uiState.value = PayUiState.Loading
            try {
                val token = StringObject.ApiToken
                val payData = repository.getPayInfo(token)
                _uiState.value = PayUiState.Success(payData)
            } catch (e: Exception) {
                _uiState.value = PayUiState.Error(
                    application.getString(
                        R.string.error_load_prefix,
                        e.message ?: application.getString(R.string.error_unknown)
                    )
                )
            }
        }
    }
}

sealed interface PayUiState {
    data class Success(val data: PayModel) : PayUiState
    data class Error(val message: String) : PayUiState
    object Loading : PayUiState
}
