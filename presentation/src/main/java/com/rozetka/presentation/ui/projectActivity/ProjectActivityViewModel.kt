package com.rozetka.presentation.ui.projectActivity

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.ProjectActivityRepository
import com.rozetka.domain.util.StringObject
import com.rozetka.model.PDModel
import com.rozetka.model.ProjectSheetItem
import com.rozetka.presentation.R
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProjectActivityUiState {
    data class Success(
        val projectData: PDModel,
        val scheduleItems: List<ProjectSheetItem> = emptyList(),
        val isLoadingSchedule: Boolean = false,
        val scheduleError: String? = null
    ) : ProjectActivityUiState

    data class Error(val message: String) : ProjectActivityUiState
    object Loading : ProjectActivityUiState
}

class ProjectActivityViewModel(
    private val repository: ProjectActivityRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectActivityUiState>(ProjectActivityUiState.Loading)
    val uiState: StateFlow<ProjectActivityUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDirection = MutableStateFlow<String?>(null)
    val selectedDirection: StateFlow<String?> = _selectedDirection.asStateFlow()

    init {
        loadProjectData()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDirectionSelect(direction: String?) {
        _selectedDirection.value = if (_selectedDirection.value == direction) null else direction
    }

    fun loadProjectData() {
        viewModelScope.launch {
            _uiState.value = ProjectActivityUiState.Loading
            try {
                val projectDataDeferred = async { repository.getProjectActivity(StringObject.ApiToken) }
                val scheduleItemsDeferred = async {
                    runCatching { repository.getProjectSchedule() }.getOrDefault(emptyList())
                }

                val projectData = projectDataDeferred.await()
                val scheduleItems = scheduleItemsDeferred.await()

                _uiState.value = ProjectActivityUiState.Success(
                    projectData = projectData,
                    scheduleItems = scheduleItems,
                    isLoadingSchedule = false
                )
            } catch (e: Exception) {
                _uiState.value = ProjectActivityUiState.Error(
                    application.getString(
                        R.string.error_load_prefix,
                        e.message ?: application.getString(R.string.error_unknown)
                    )
                )
            }
        }
    }

    fun refreshSchedule() {
        val currentState = _uiState.value
        if (currentState is ProjectActivityUiState.Success) {
            viewModelScope.launch {
                _uiState.value = currentState.copy(isLoadingSchedule = true, scheduleError = null)
                try {
                    val scheduleItems = repository.getProjectSchedule()
                    _uiState.value = currentState.copy(
                        scheduleItems = scheduleItems,
                        isLoadingSchedule = false
                    )
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(
                        isLoadingSchedule = false,
                        scheduleError = e.message ?: application.getString(R.string.error_unknown)
                    )
                }
            }
        }
    }
}
