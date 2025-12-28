package com.rozetka.presentation.ui.projectActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject
import com.rozetka.model.PDModel
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed interface ProjectActivityUiState {
    data class Success(val projectData: PDModel) : ProjectActivityUiState
    data class Error(val message: String) : ProjectActivityUiState
    object Loading : ProjectActivityUiState
}

class ProjectActivityViewModel : ViewModel() {


    private val repository = MospolytechMethods()

    private val _uiState = MutableStateFlow<ProjectActivityUiState>(ProjectActivityUiState.Loading)
    val uiState: StateFlow<ProjectActivityUiState> = _uiState.asStateFlow()

    init {
        loadProjectData()
    }

    fun loadProjectData() {
        viewModelScope.launch {
            _uiState.value = ProjectActivityUiState.Loading
            try {

                val projectData = repository.getPDInfo(StringObject.ApiToken)
                _uiState.value = ProjectActivityUiState.Success(projectData)
            } catch (e: Exception) {
                _uiState.value = ProjectActivityUiState.Error("Ошибка загрузки данных: ${e.message}")
            }
        }
    }
}