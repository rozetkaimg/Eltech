package com.rozetka.presentation.ui.moodle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.MoodleRepository
import com.rozetka.domain.repository.UserRepository
import com.rozetka.model.MoodleDeadline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import java.util.Calendar

sealed interface DeadlinesUiState {
    object Loading : DeadlinesUiState
    data class Success(val deadlines: List<MoodleDeadline>, val isCurrentMonth: Boolean) : DeadlinesUiState
    data class Error(val message: String, val isCurrentMonth: Boolean) : DeadlinesUiState
}

class DeadlinesViewModel(
    private val repository: MoodleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeadlinesUiState>(DeadlinesUiState.Loading)
    val uiState: StateFlow<DeadlinesUiState> = _uiState.asStateFlow()

    private var monthOffset = 0

    init {
        loadDeadlines()
    }

    fun loadDeadlines() {
        viewModelScope.launch {
            _uiState.value = DeadlinesUiState.Loading
            val session = userRepository.getMoodleSession() ?: ""
            if (session.isEmpty()) {
                _uiState.value = DeadlinesUiState.Error("Сессия не найдена", monthOffset == 0)
                return@launch
            }

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, monthOffset)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val timeInSeconds = calendar.timeInMillis / 1000L

            val result = repository.getDeadlines(session, timeInSeconds)
            val isCurrent = monthOffset == 0

            if (result.isNotEmpty()) {
                _uiState.value = DeadlinesUiState.Success(result, isCurrent)
            } else {
                _uiState.value = DeadlinesUiState.Error("В этом месяце дедлайнов не найдено", isCurrent)
            }
        }
    }

    fun loadPreviousMonth() {
        monthOffset -= 1
        loadDeadlines()
    }

    fun loadNextMonth() {
        monthOffset += 1
        loadDeadlines()
    }

    fun loadCurrentMonth() {
        monthOffset = 0
        loadDeadlines()
    }
}
