package com.rozetka.presentation.ui.guestSearchGroup



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed interface GuestGroupUiState {
    data class Success(val groups: List<String>) : GuestGroupUiState
    data class Error(val message: String) : GuestGroupUiState
    object Loading : GuestGroupUiState
}

class GuestSearchGroupViewModel(
    private val repository: MospolytechMethods
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuestGroupUiState>(GuestGroupUiState.Loading)
    val uiState: StateFlow<GuestGroupUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()


    private var allGroupsCache: List<String> = emptyList()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = GuestGroupUiState.Loading
            try {
                // Вызываем ваш новый метод
                val groups = repository.getGroupsList()

                if (groups.isNotEmpty()) {
                    allGroupsCache = groups.sorted() // Сортируем по алфавиту
                    _uiState.value = GuestGroupUiState.Success(allGroupsCache)
                } else {
                    _uiState.value = GuestGroupUiState.Error("Список групп пуст")
                }
            } catch (e: Exception) {
                _uiState.value = GuestGroupUiState.Error(
                    e.message ?: "Ошибка загрузки списка групп"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        filterGroups(query)
    }

    private fun filterGroups(query: String) {
        val currentState = _uiState.value
        // Фильтруем только если у нас уже есть загруженные данные или мы показываем результат поиска
        if (allGroupsCache.isNotEmpty()) {
            val filtered = if (query.isBlank()) {
                allGroupsCache
            } else {
                allGroupsCache.filter {
                    it.contains(query, ignoreCase = true)
                }
            }
            _uiState.value = GuestGroupUiState.Success(filtered)
        }
    }

    fun onGroupSelected(groupName: String) {
        StringObject.groupName = groupName
    }

    fun retry() {
        loadGroups()
    }
}