package com.rozetka.presentation.ui.physEdJournal

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.PhysEdJournalRepository
import com.rozetka.domain.util.StringObject.guid
import com.rozetka.model.PhysEdJournalResponse
import com.rozetka.presentation.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PhysEdJournalUiState {
    data class Success(val studentData: PhysEdJournalResponse) : PhysEdJournalUiState
    data class Error(val message: String) : PhysEdJournalUiState
    object Loading : PhysEdJournalUiState
}

class PhysEdJournalViewModel(
    private val repository: PhysEdJournalRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhysEdJournalUiState>(PhysEdJournalUiState.Loading)
    val uiState: StateFlow<PhysEdJournalUiState> = _uiState.asStateFlow()

    init {
        loadStudentData()
    }

    fun loadStudentData() {
        viewModelScope.launch {
            _uiState.value = PhysEdJournalUiState.Loading
            try {
                val studentData = repository.getStudentJournal(guid)
                _uiState.value = PhysEdJournalUiState.Success(studentData)
            } catch (e: Exception) {
                _uiState.value = PhysEdJournalUiState.Error(
                    application.getString(
                        R.string.error_load_prefix,
                        e.message ?: application.getString(R.string.error_unknown)
                    )
                )
            }
        }
    }
}
