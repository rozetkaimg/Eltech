package com.rozetka.presentation.ui.sessionSchedule

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.model.ScheduleModel
import com.rozetka.network.MospolytechMethods
import com.rozetka.presentation.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Состояния UI для экрана сессии, повторяющие логику TeacherScheduleUiState.
 */
sealed interface SessionScheduleUiState {
    data class Success(val data: ScheduleModel) : SessionScheduleUiState
    data class Error(val message: String) : SessionScheduleUiState
    object Loading : SessionScheduleUiState
    object Initial : SessionScheduleUiState
}

class SessionScheduleViewModel(
    private val mospolytechMethods: MospolytechMethods,
    val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionScheduleUiState>(SessionScheduleUiState.Initial)
    val uiState: StateFlow<SessionScheduleUiState> = _uiState.asStateFlow()
    fun getSessionSchedule(group: String) {
        viewModelScope.launch {
            _uiState.value = SessionScheduleUiState.Loading

            if (group.isBlank()) {
                _uiState.value = SessionScheduleUiState.Error("")
                return@launch
            }

            try {
                val scheduleData = mospolytechMethods.getSessionSchedule(group)
                if (scheduleData.grid.isEmpty()) {
                    _uiState.value = SessionScheduleUiState.Error(
                        application.getString(R.string.no_schedule_placeholder)
                    )
                } else {
                    _uiState.value = SessionScheduleUiState.Success(scheduleData)
                }

            } catch (e: Exception) {
                _uiState.value = SessionScheduleUiState.Error(
                    application.getString(R.string.error_load_prefix, e.message)
                )
            }
        }
    }
}