package com.rozetka.presentation.ui.teacherSchedule



import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.ScheduleRepository
import com.rozetka.domain.util.StringObject
import com.rozetka.model.ScheduleByDay
import com.rozetka.network.MospolytechMethods
import com.rozetka.presentation.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TeacherScheduleUiState {
    data class Success(val data: ScheduleByDay) : TeacherScheduleUiState
    data class Error(val message: String) : TeacherScheduleUiState
    object Loading : TeacherScheduleUiState
    object Initial : TeacherScheduleUiState
}
class TeacherScheduleViewModel(
    private val scheduleRepository: MospolytechMethods,
    val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeacherScheduleUiState>(TeacherScheduleUiState.Initial)
    val uiState: StateFlow<TeacherScheduleUiState> = _uiState.asStateFlow()

    fun getSchedule(fio: String) {
        viewModelScope.launch {
            _uiState.value = TeacherScheduleUiState.Loading

            if (fio.isBlank()) {
                _uiState.value = TeacherScheduleUiState.Error("")
                return@launch
            }

            try {
                val scheduleData = scheduleRepository.getScheduleTeacher(
                    fio = fio,
                    session = "",
                    token = StringObject.ApiToken
                )

                if (scheduleData.isEmpty()) {
                    _uiState.value = TeacherScheduleUiState.Error(application.getString(R.string.no_schedule_placeholder))
                } else {
                    _uiState.value = TeacherScheduleUiState.Success(scheduleData)
                }

            } catch (e: Exception) {

                _uiState.value = TeacherScheduleUiState.Error(
                    application.getString(R.string.error_load_prefix, e.message)
                )
            }
        }
    }
}