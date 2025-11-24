package com.rozetka.presentation.ui.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject.ApiToken
import com.rozetka.model.Student
import com.rozetka.model.StudentR
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch





sealed interface MessageUiState {
    object Idle : MessageUiState
    object Loading : MessageUiState
    data class Success(val dialogId: String) : MessageUiState
    data class Error(val message: String) : MessageUiState
}

sealed interface StudentsUiState {
    object Initial : StudentsUiState
    object Loading : StudentsUiState
    data class Empty(val query: String) : StudentsUiState
    data class Error(val message: String) : StudentsUiState
    data class Content(
        val items: List<StudentR>,
        val isLoadingMore: Boolean
    ) : StudentsUiState
}

class StudentsViewModel(
    private val repository: MospolytechMethods
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentsUiState>(StudentsUiState.Initial)
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _messageUiState = MutableStateFlow<MessageUiState>(MessageUiState.Idle)
    val messageUiState: StateFlow<MessageUiState> = _messageUiState.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1
    private val perPage = 50
    private val currentItems = mutableListOf<StudentR>()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun searchStudents() {
        val query = _searchQuery.value.trim()
        if (query.isBlank()) return

        currentPage = 1
        currentItems.clear()
        _uiState.value = StudentsUiState.Loading
        _messageUiState.value = MessageUiState.Idle

        loadData(query)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state is StudentsUiState.Content && !state.isLoadingMore && currentPage < totalPages) {
            currentPage++
            loadData(_searchQuery.value, isNextPage = true)
        }
    }

    fun resetMessageState() {
        _messageUiState.value = MessageUiState.Idle
    }

    fun sendMessageToStudent(studentId: String, message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _messageUiState.value = MessageUiState.Loading
            try {
                val response = repository.sendMessageNoFilesByID(
                    iD = studentId,
                    token = ApiToken,
                    newMessage = message
                )

                if (response.id.isNotBlank()) {
                    _messageUiState.value = MessageUiState.Success(dialogId = response.id)
                } else {
                    _messageUiState.value = MessageUiState.Error("Не удалось получить ID диалога")
                }

            } catch (e: Exception) {
                _messageUiState.value = MessageUiState.Error(e.message ?: "Ошибка отправки")
            }
        }
    }

    private fun loadData(query: String, isNextPage: Boolean = false) {
        viewModelScope.launch {
            if (isNextPage) {
                _uiState.value = (_uiState.value as? StudentsUiState.Content)?.copy(isLoadingMore = true)
                    ?: StudentsUiState.Loading
            }

            try {
                val isGroupSearch = query.any { it.isDigit() } || query.contains("-")

                val result = repository.getStudents(
                    token = ApiToken,
                    search = if (isGroupSearch) "" else query,
                    group = if (isGroupSearch) query else "",
                    page = currentPage,
                    perPage = perPage
                )

                totalPages = result.pages.toIntOrNull() ?: 1
                val newItems = result.items

                if (newItems.isEmpty() && !isNextPage) {
                    _uiState.value = StudentsUiState.Empty(query)
                } else {
                    currentItems.addAll(newItems)
                    _uiState.value = StudentsUiState.Content(
                        items = currentItems.toList(),
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                if (!isNextPage) {
                    _uiState.value = StudentsUiState.Error(e.message ?: "Ошибка поиска студентов")
                } else {
                    _uiState.value = StudentsUiState.Content(currentItems.toList(), isLoadingMore = false)
                }
            }
        }
    }
}
