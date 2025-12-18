
package com.rozetka.presentation.ui.searchGroup

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.data.SecureStorage
import com.rozetka.domain.util.StringObject.ApiToken
import com.rozetka.localdata.SettingsData
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchGroupViewModel(
    private val repository: MospolytechMethods,
    application: Application
) : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private val context = application

    private val _uiState = MutableStateFlow<SearchGroupUiState>(SearchGroupUiState.Initial)
    val uiState: StateFlow<SearchGroupUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoritesFlow = MutableStateFlow<List<String>>(emptyList())
    val favoritesFlow: StateFlow<List<String>> = _favoritesFlow.asStateFlow()

    init {

        viewModelScope.launch {
            _favoritesFlow.value = SecureStorage(context).getGroupNames()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun searchGroups() {
        val group = _searchQuery.value.trim()
        if (group.isBlank()) {
            _uiState.value = SearchGroupUiState.Initial
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchGroupUiState.Loading
            try {
                val result = repository.getGroups(group, ApiToken)

                if (result.items.isEmpty()) {
                    _uiState.value = SearchGroupUiState.Empty(group)
                } else {
                    _uiState.value = SearchGroupUiState.Success(result.items)
                }
            } catch (e: Exception) {
                _uiState.value = SearchGroupUiState.Error(
                    e.message ?: "Неизвестная ошибка поиска групп."
                )
            }
        }
    }

    fun addToFavorites(groupName: String) {
        viewModelScope.launch {
            if (groupName !in _favoritesFlow.value) {
                val newFavorites = _favoritesFlow.value + groupName
                _favoritesFlow.value = newFavorites
                SecureStorage(context).saveGroupNames( _favoritesFlow.value)

            }
        }
    }

    fun removeFromFavorites(groupName: String) {
        viewModelScope.launch {
            val newFavorites = _favoritesFlow.value.filter { it != groupName }
            _favoritesFlow.value = newFavorites

            SecureStorage(context).saveGroupNames( _favoritesFlow.value)
        }
    }
}

sealed interface SearchGroupUiState {
    data class Success(val groups: List<String>) : SearchGroupUiState
    data class Error(val message: String) : SearchGroupUiState
    data class Empty(val query: String) : SearchGroupUiState
    object Loading : SearchGroupUiState
    object Initial : SearchGroupUiState
}