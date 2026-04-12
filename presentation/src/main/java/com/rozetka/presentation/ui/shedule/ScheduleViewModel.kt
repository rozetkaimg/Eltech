package com.rozetka.presentation.ui.shedule

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.data.SecureStorage
import com.rozetka.domain.repository.ScheduleRepository
import com.rozetka.domain.util.StringObject
import com.rozetka.domain.util.StringObject.campusToken
import com.rozetka.model.ScheduleModel
import com.rozetka.network.campus.CampusApi
import com.rozetka.presentation.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale


data class WeekInfo(
    val weekNumberInSemester: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val label: String
)


data class ScheduleScreenData(
    val fullSchedule: ScheduleModel,
    val weeks: List<WeekInfo>,
    val initialWeekIndex: Int,
    val isScheduleMissing: Boolean = false
)

class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
    val application: Application,
    private val campusApi: CampusApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Initial)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()
    private val secureStorage: SecureStorage = SecureStorage(application)

    init {
        viewModelScope.launch {
            try {
                campusToken = campusApi.getBearerToken().token
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Failed to get campus token", e)
            }
        }
        getSchedule(StringObject.groupName)
    }

    fun getSchedule(group: String) {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading

            val userOwnGroup = secureStorage.getGroupName().toString()
            val groupToFetch = if (group.isEmpty()) userOwnGroup else group

            if (groupToFetch.isEmpty()) {
                _uiState.value =
                    ScheduleUiState.Error(application.getString(R.string.error_group_not_found_prompt))
                return@launch
            }

            // Always use the robust getSchedule from repository
            scheduleRepository.getSchedule(groupToFetch)
                .catch { e ->
                    Log.e("ScheduleViewModel", "Flow error in getSchedule", e)
                    _uiState.value = ScheduleUiState.Error(
                        application.getString(
                            R.string.error_critical_prefix,
                            e.message ?: "Unknown error"
                        )
                    )
                }
                .collect { result ->
                    result.onSuccess { scheduleData ->
                        try {
                            val screenData = processScheduleData(scheduleData)
                            _uiState.value = ScheduleUiState.Success(screenData)
                        } catch (e: Exception) {
                            Log.e("ScheduleViewModel", "Error processing schedule data", e)
                            _uiState.value = ScheduleUiState.Error("Ошибка обработки данных расписания")
                        }
                    }
                    result.onFailure { throwable ->
                        Log.e("ScheduleViewModel", "Result failure in getSchedule", throwable)
                        _uiState.value = ScheduleUiState.Error(
                            application.getString(R.string.error_load_prefix, throwable.message ?: "Network error")
                        )
                    }
                }
        }
    }

    private fun processScheduleData(schedule: ScheduleModel): ScheduleScreenData {
        val group = schedule.group
        if (group == null || schedule.grid.isNullOrEmpty()) {
            return ScheduleScreenData(
                fullSchedule = schedule,
                weeks = emptyList(),
                initialWeekIndex = 0,
                isScheduleMissing = true
            )
        }

        // Safer parsing with fallback
        val startDate = runCatching { LocalDate.parse(group.dateFrom) }
            .getOrDefault(LocalDate.now())
        val endDate = runCatching { LocalDate.parse(group.dateTo) }
            .getOrDefault(startDate.plusMonths(4))

        val weeks = generateWeeks(startDate, endDate)
        val initialIndex = findCurrentWeekIndex(weeks)
        return ScheduleScreenData(schedule, weeks, initialIndex, isScheduleMissing = false)
    }

    private fun generateWeeks(start: LocalDate, end: LocalDate): List<WeekInfo> {
        val weeksList = mutableListOf<WeekInfo>()
        var currentStart = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        var weekCounter = 1

        while (!currentStart.isAfter(end)) {
            val currentEnd = currentStart.plusDays(6)
            currentStart.get(WeekFields.of(Locale.getDefault()).weekOfYear())
            val label = application.getString(R.string.week_counter_label, weekCounter.toString())

            weeksList.add(WeekInfo(weekCounter, currentStart, currentEnd, label))
            currentStart = currentStart.plusWeeks(1)
            weekCounter++
        }
        return weeksList
    }

    private fun findCurrentWeekIndex(weeks: List<WeekInfo>): Int {
        val today = LocalDate.now()
        val index =
            weeks.indexOfFirst { !today.isBefore(it.startDate) && !today.isAfter(it.endDate) }
        return if (index != -1) index else 0
    }

    fun getScheduleState(): Boolean = secureStorage.getScheduleState()
}

sealed interface ScheduleUiState {
    data class Success(val data: ScheduleScreenData) : ScheduleUiState
    data class Error(val message: String) : ScheduleUiState
    object Loading : ScheduleUiState
    object Initial : ScheduleUiState
}