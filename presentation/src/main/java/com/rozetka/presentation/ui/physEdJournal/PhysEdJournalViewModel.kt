package com.rozetka.presentation.ui.physEdJournal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.model.PhysEdJournalResponse
import com.rozetka.network.MospolytechMethods
import com.rozetka.domain.util.StringObject.guid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed interface PhysEdJournalUiState {
    data class Success(val studentData: PhysEdJournalResponse) : PhysEdJournalUiState
    data class Error(val message: String) : PhysEdJournalUiState
    object Loading : PhysEdJournalUiState
}

class PhysEdJournalViewModel : ViewModel() {

    private val repository = MospolytechMethods()

    private val _uiState = MutableStateFlow<PhysEdJournalUiState>(PhysEdJournalUiState.Loading)
    val uiState: StateFlow<PhysEdJournalUiState> = _uiState.asStateFlow()

    init {

        loadStudentData()
    }


    fun loadStudentData() {
        viewModelScope.launch {
            _uiState.value = PhysEdJournalUiState.Loading
            try {
                val studentGuid = guid
                val studentData = repository.getPhysedjourna(studentGuid)
                _uiState.value = PhysEdJournalUiState.Success(studentData)
            } catch (e: Exception) {
                _uiState.value = PhysEdJournalUiState.Error("Ошибка загрузки данных: ${e.message}")
            }
        }
    }
}