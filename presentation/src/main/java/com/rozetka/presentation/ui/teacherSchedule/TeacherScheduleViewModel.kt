package com.rozetka.presentation.ui.teacherSchedule



import android.app.Application
import androidx.compose.runtime.Composable
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
import java.util.Locale

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
object ScheduleUtils {
    private val monthIndices = mapOf(
        "сен" to 0, "сент" to 0,
        "окт" to 1,
        "ноя" to 2, "ноябрь" to 2,
        "дек" to 3,
        "янв" to 4,
        "фев" to 5,
        "мар" to 6,
        "апр" to 7,
        "май" to 8,
        "июн" to 9,
        "июл" to 10,
        "авг" to 11
    )


    private val fullMonthNames = mapOf(
        "сен" to "Сентябрь", "сент" to "Сентябрь",
        "окт" to "Октябрь",
        "ноя" to "Ноябрь", "ноябрь" to "Ноябрь",
        "дек" to "Декабрь",
        "янв" to "Январь",
        "фев" to "Февраль",
        "мар" to "Март",
        "апр" to "Апрель",
        "май" to "Май",
        "июн" to "Июнь"
    )

    fun getMonthSortIndex(rawMonth: String): Int {
        val key = rawMonth.lowercase(Locale.getDefault()).take(3)
        return monthIndices[key] ?: 99
    }

    fun getFullMonthName(rawMonth: String): String {
        val key = rawMonth.lowercase(Locale.getDefault()).take(3)
        return fullMonthNames[key] ?: rawMonth
    }

    fun extractMonth(dateInterval: String?): String {
        if (dateInterval.isNullOrBlank()) return "Без даты"
        val regex = Regex("\\d+\\s+([А-Яа-я]+)")
        val match = regex.find(dateInterval)

        return match?.groupValues?.get(1) ?: "Прочее"
    }
}