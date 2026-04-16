package com.rozetka.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject
import com.rozetka.model.ExternalNewsItem
import com.rozetka.model.NewsModelItem
import com.rozetka.model.NotificationModelItem
import com.rozetka.model.PolytechEvent
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.async
import com.rozetka.domain.repository.MoodleRepository
import com.rozetka.domain.repository.UserRepository
import com.rozetka.model.MoodleDeadline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data class Success(
        val news: List<NewsModelItem>,
        val events: List<PolytechEvent>,
        val externalNews: List<ExternalNewsItem>,
        val notifications: List<NotificationModelItem> = emptyList(),
        val deadlines: List<MoodleDeadline> = emptyList()
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
    object Loading : HomeUiState
}

class HomeViewModel(
    private val userRepository: UserRepository,
    private val moodleRepository: MoodleRepository
) : ViewModel() {
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

                val newsDeferred = async {
                    try { repository.getLastNews(StringObject.ApiToken) } catch (e: Exception) { emptyList() }
                }
                val eventsDeferred = async {
                    try { repository.getEventsList(currentEventsPage) } catch (e: Exception) { emptyList() }
                }
                val externalDeferred = async {
                    try { repository.getExternalNewsList(currentExternalPage) } catch (e: Exception) { emptyList() }
                }
                val notificationsDeferred = async {
                    try { repository.getNotifications(StringObject.ApiToken) } catch (e: Exception) { emptyList() }
                }
                val deadlinesDeferred = async {
                    try {
                        val session = userRepository.getMoodleSession() ?: ""
                        if (session.isNotEmpty()) moodleRepository.getDeadlines(session) else emptyList()
                    } catch (e: Exception) { emptyList() }
                }

                _uiState.value = HomeUiState.Success(
                    news = newsDeferred.await(),
                    events = eventsDeferred.await(),
                    externalNews = externalDeferred.await(),
                    notifications = notificationsDeferred.await(),
                    deadlines = deadlinesDeferred.await()
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Load home data failed", e)
                _uiState.value = HomeUiState.Error("Ошибка загрузки данных")
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