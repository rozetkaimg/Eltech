package com.rozetka.presentation.ui.moodle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.MoodleRepository
import com.rozetka.domain.repository.UserRepository
import com.rozetka.model.ModuleContentNative
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ModuleNativeUiState {
    object Loading : ModuleNativeUiState
    data class Success(val data: ModuleContentNative, val moodleSession: String) : ModuleNativeUiState
    data class Error(val message: String) : ModuleNativeUiState
}

class ModuleNativeViewModel(
    private val repository: MoodleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ModuleNativeUiState>(ModuleNativeUiState.Loading)
    val uiState: StateFlow<ModuleNativeUiState> = _uiState.asStateFlow()

    fun loadModuleContent(url: String) {
        viewModelScope.launch {
            _uiState.value = ModuleNativeUiState.Loading
            try {
                val session = userRepository.getMoodleSession() ?: ""
                val content = repository.getModuleContentNative(session, url)
                _uiState.value = ModuleNativeUiState.Success(content, session)
            } catch (e: Exception) {
                _uiState.value = ModuleNativeUiState.Error("Ошибка загрузки: ${e.localizedMessage}")
            }
        }
    }
}
