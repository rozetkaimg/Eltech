package com.rozetka.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject
import com.rozetka.model.NewsModelItem
import com.rozetka.model.PolytechEvent
import com.rozetka.model.ExternalNewsItem // ИСПОЛЬЗУЙТЕ ЭТОТ ИМПОРТ
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data class Success(
        val news: List<NewsModelItem>,
        val events: List<PolytechEvent>,
        val externalNews: List<ExternalNewsItem>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
    object Loading : HomeUiState
}

class HomeViewModel : ViewModel() {
    private val repository = MospolytechMethods()
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentEventsPage = 1
    private var currentExternalPage = 1
    private var isNextPageLoading = false

    init { loadHomeData() }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                currentEventsPage = 1
                currentExternalPage = 1
                val newsDeferred = async { repository.getLastNews(StringObject.ApiToken) }
                val eventsDeferred = async { repository.getEventsList(currentEventsPage) }
                val externalDeferred = async { repository.getExternalNewsList(currentExternalPage) }

                _uiState.value = HomeUiState.Success(
                    news = newsDeferred.await(),
                    events = eventsDeferred.await(),
                    externalNews = externalDeferred.await()
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun loadMoreEvents() {
        val currentState = _uiState.value
        if (isNextPageLoading || currentState !is HomeUiState.Success) return
        viewModelScope.launch {
            isNextPageLoading = true
            try {
                val nextPage = currentEventsPage + 1
                val newEvents = repository.getEventsList(nextPage)
                if (newEvents.isNotEmpty()) {
                    currentEventsPage = nextPage
                    _uiState.value = currentState.copy(events = currentState.events + newEvents)
                }
            } finally { isNextPageLoading = false }
        }
    }

    fun loadMoreExternalNews() {
        val currentState = _uiState.value
        if (isNextPageLoading || currentState !is HomeUiState.Success) return
        viewModelScope.launch {
            isNextPageLoading = true
            try {
                val nextPage = currentExternalPage + 1
                val newItems = repository.getExternalNewsList(nextPage)
                if (newItems.isNotEmpty()) {
                    currentExternalPage = nextPage
                    _uiState.value = currentState.copy(externalNews = currentState.externalNews + newItems)
                }
            } finally { isNextPageLoading = false }
        }
    }
}