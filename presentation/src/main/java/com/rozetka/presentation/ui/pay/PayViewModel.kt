package com.rozetka.presentation.ui.pay

import com.rozetka.domain.util.StringObject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.model.PayModel
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PayViewModel : ViewModel() {

    private val repository = MospolytechMethods()

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
                _uiState.value = PayUiState.Error("error: ${e.message}")
            }
        }
    }
}

sealed interface PayUiState {
    data class Success(val data: PayModel) : PayUiState
    data class Error(val message: String) : PayUiState
    object Loading : PayUiState
}