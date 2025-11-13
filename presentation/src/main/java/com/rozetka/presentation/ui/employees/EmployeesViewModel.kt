package com.rozetka.presentation.ui.employees



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject.ApiToken
import com.rozetka.model.ItemX
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmployeesViewModel(
    private val repository: MospolytechMethods
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmployeesUiState>(EmployeesUiState.Initial)
    val uiState: StateFlow<EmployeesUiState> = _uiState.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private var currentPage = 1
    private var totalPages = 1
    private val perPage = 50
    private val currentItems = mutableListOf<ItemX>()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun searchEmployees() {
        val query = _searchQuery.value.trim()
        if (query.isBlank()) {
            _uiState.value = EmployeesUiState.Initial
            return
        }
        currentPage = 1
        currentItems.clear()
        _uiState.value = EmployeesUiState.Loading

        loadData(query)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state is EmployeesUiState.Content && !state.isLoadingMore && currentPage < totalPages) {
            currentPage++
            loadData(_searchQuery.value, isNextPage = true)
        }
    }

    private fun loadData(query: String, isNextPage: Boolean = false) {
        viewModelScope.launch {
            if (isNextPage) {
                _uiState.value = (_uiState.value as? EmployeesUiState.Content)?.copy(isLoadingMore = true)
                    ?: EmployeesUiState.Loading
            }

            try {
                val result = repository.getStaff(
                    token = ApiToken,
                    division = query,
                    page = currentPage,
                    perpage = perPage
                )

                totalPages = result.pages.toIntOrNull() ?: 1
                val newItems = result.items

                if (newItems.isEmpty() && !isNextPage) {
                    _uiState.value = EmployeesUiState.Empty(query)
                } else {
                    currentItems.addAll(newItems)
                    _uiState.value = EmployeesUiState.Content(
                        items = currentItems.toList(),
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                if (!isNextPage) {
                    _uiState.value = EmployeesUiState.Error(e.message ?: "Ошибка поиска сотрудников")
                } else {
                    _uiState.value = EmployeesUiState.Content(currentItems.toList(), isLoadingMore = false)
                }
            }
        }
    }
}

sealed interface EmployeesUiState {
    object Initial : EmployeesUiState
    object Loading : EmployeesUiState
    data class Empty(val query: String) : EmployeesUiState
    data class Error(val message: String) : EmployeesUiState

    data class Content(
        val items: List<ItemX>,
        val isLoadingMore: Boolean
    ) : EmployeesUiState
}