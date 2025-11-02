package com.rozetka.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject
import com.rozetka.model.NewsModelItem
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data class Success( val news: List<NewsModelItem>) : HomeUiState
    data class Error(val message: String) : HomeUiState
    object Loading : HomeUiState
}

class HomeViewModel : ViewModel() {

    private val repository = MospolytechMethods()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val token = StringObject.ApiToken

                val newsDataDeferred = async { repository.getLastNews(token) }

                val newsData = newsDataDeferred.await()

                _uiState.value = HomeUiState.Success( newsData)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Ошибка загрузки данных: ${e.message}")
            }
        }
    }
}