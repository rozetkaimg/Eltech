package com.rozetka.presentation.ui.projectActivity

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.ProjectActivityRepository
import com.rozetka.model.ProjectSheetItem
import com.rozetka.presentation.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AllProjectsUiState {
    data class Success(
        val scheduleItems: List<ProjectSheetItem> = emptyList()
    ) : AllProjectsUiState

    data class Error(val message: String) : AllProjectsUiState
    object Loading : AllProjectsUiState
}

class AllProjectsViewModel(
    private val repository: ProjectActivityRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<AllProjectsUiState>(AllProjectsUiState.Loading)
    val uiState: StateFlow<AllProjectsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDirection = MutableStateFlow<String?>(null)
    val selectedDirection: StateFlow<String?> = _selectedDirection.asStateFlow()

    init {
        loadSchedule()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDirectionSelect(direction: String?) {
        _selectedDirection.value = if (_selectedDirection.value == direction) null else direction
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.value = AllProjectsUiState.Loading
            try {
                val scheduleItems = repository.getProjectSchedule()
                _uiState.value = AllProjectsUiState.Success(scheduleItems)
            } catch (e: Exception) {
                _uiState.value = AllProjectsUiState.Error(
                    application.getString(
                        R.string.error_load_prefix,
                        e.message ?: application.getString(R.string.error_unknown)
                    )
                )
            }
        }
    }
}
