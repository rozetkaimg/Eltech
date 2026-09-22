package com.rozetka.presentation.ui.physEdSchedule

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.PhysEdJournalRepository
import com.rozetka.model.PhysEdScheduleResponse
import com.rozetka.presentation.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.rozetka.data.SecureStorage
import com.rozetka.model.PhysEdClass
import com.rozetka.model.PhysEdLocation
import com.rozetka.model.PhysEdSubscribedClass
import com.rozetka.model.PhysEdTimeSlot

sealed interface PhysEdScheduleUiState {
    data class Success(val data: PhysEdScheduleResponse) : PhysEdScheduleUiState
    data class Error(val message: String) : PhysEdScheduleUiState
    object Loading : PhysEdScheduleUiState
}

class PhysEdScheduleViewModel(
    private val repository: PhysEdJournalRepository,
    private val application: Application
) : ViewModel() {

    private val secureStorage = SecureStorage(application)

    private val _uiState = MutableStateFlow<PhysEdScheduleUiState>(PhysEdScheduleUiState.Loading)
    val uiState: StateFlow<PhysEdScheduleUiState> = _uiState.asStateFlow()

    private val _subscriptions = MutableStateFlow<List<PhysEdSubscribedClass>>(secureStorage.getPhysEdSubscriptions())
    val subscriptions: StateFlow<List<PhysEdSubscribedClass>> = _subscriptions.asStateFlow()

    private val _selectedDiscipline = MutableStateFlow<String?>(null)
    val selectedDiscipline: StateFlow<String?> = _selectedDiscipline.asStateFlow()

    private val _selectedLocationId = MutableStateFlow<String?>(null)
    val selectedLocationId: StateFlow<String?> = _selectedLocationId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadSchedule()
    }

    fun toggleSubscription(classItem: PhysEdClass, timeSlot: PhysEdTimeSlot, location: PhysEdLocation?) {
        val discName = classItem.disciplines.firstOrNull() ?: "Физическая культура"
        val locName = location?.name.orEmpty()
        val locAddr = location?.address.orEmpty()
        val slotTime = timeSlot.timeSlot.trim()
        val subId = "$discName|$slotTime|$locName"

        val currentList = secureStorage.getPhysEdSubscriptions().toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == subId }

        if (existingIndex != -1) {
            currentList.removeAt(existingIndex)
        } else {
            currentList.add(
                PhysEdSubscribedClass(
                    id = subId,
                    discipline = discName,
                    timeSlot = slotTime,
                    locationName = locName,
                    locationAddress = locAddr
                )
            )
        }

        secureStorage.savePhysEdSubscriptions(currentList)
        _subscriptions.value = currentList
    }

    fun onDisciplineSelect(discipline: String?) {
        _selectedDiscipline.value = if (_selectedDiscipline.value == discipline) null else discipline
    }

    fun onLocationSelect(locationId: String?) {
        _selectedLocationId.value = if (_selectedLocationId.value == locationId) null else locationId
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.value = PhysEdScheduleUiState.Loading
            try {
                val scheduleResponse = repository.getPhysEdSchedule()
                _uiState.value = PhysEdScheduleUiState.Success(scheduleResponse)
            } catch (e: Exception) {
                _uiState.value = PhysEdScheduleUiState.Error(
                    application.getString(
                        R.string.error_load_prefix,
                        e.message ?: application.getString(R.string.error_unknown)
                    )
                )
            }
        }
    }
}
