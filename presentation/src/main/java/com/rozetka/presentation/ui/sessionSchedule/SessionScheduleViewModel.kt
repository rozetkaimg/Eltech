package com.rozetka.presentation.ui.sessionSchedule

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.model.local.CalendarAccount
import com.rozetka.domain.usecase.CalendarUseCase
import com.rozetka.model.ScheduleModel
import com.rozetka.network.MospolytechMethods
import com.rozetka.presentation.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionScheduleUiState {
    data class Success(val data: ScheduleModel) : SessionScheduleUiState
    data class Error(val message: String) : SessionScheduleUiState
    object Loading : SessionScheduleUiState
    object Initial : SessionScheduleUiState
}

sealed interface CalendarEffect {
    data class ShowNotification(val message: String, val type: NotificationType) : CalendarEffect
    data class ShowCalendarSelection(val calendars: List<CalendarAccount>) : CalendarEffect
}

class SessionScheduleViewModel(
    private val mospolytechMethods: MospolytechMethods,
    private val calendarUseCase: CalendarUseCase,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionScheduleUiState>(SessionScheduleUiState.Initial)
    val uiState: StateFlow<SessionScheduleUiState> = _uiState.asStateFlow()

    private val _calendarEffect = MutableSharedFlow<CalendarEffect>()
    val calendarEffect: SharedFlow<CalendarEffect> = _calendarEffect.asSharedFlow()

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

    fun onExportClicked() {
        viewModelScope.launch {
            val calendars = calendarUseCase.getCalendars()
            if (calendars.isEmpty()) {
                _calendarEffect.emit(CalendarEffect.ShowNotification("Календари не найдены", NotificationType.ERROR))
            } else if (calendars.size == 1) {
                exportToCalendar(calendars.first().id)
            } else {
                _calendarEffect.emit(CalendarEffect.ShowCalendarSelection(calendars))
            }
        }
    }

    fun exportToCalendar(calendarId: Long) {
        val currentState = _uiState.value
        if (currentState is SessionScheduleUiState.Success) {
            viewModelScope.launch {
                val count = calendarUseCase.exportSchedule(currentState.data, calendarId)
                if (count >= 0) {
                    _calendarEffect.emit(CalendarEffect.ShowNotification("Экспортировано событий: $count", NotificationType.SUCCESS))
                } else {
                    _calendarEffect.emit(CalendarEffect.ShowNotification("Ошибка экспорта", NotificationType.ERROR))
                }
            }
        }
    }
}