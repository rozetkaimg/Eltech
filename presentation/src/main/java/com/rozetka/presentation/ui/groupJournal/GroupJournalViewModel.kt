package com.rozetka.presentation.ui.groupJournal



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject
import com.rozetka.model.StudentResponse
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



sealed interface GroupJournalUiState {
    data class Success(val response: StudentResponse) : GroupJournalUiState
    data class Error(val message: String) : GroupJournalUiState
    object Loading : GroupJournalUiState
}

class GroupJournalViewModel(
) : ViewModel() {

    private val repository = MospolytechMethods()

    private val _uiState = MutableStateFlow<GroupJournalUiState>(GroupJournalUiState.Loading)
    val uiState: StateFlow<GroupJournalUiState> = _uiState.asStateFlow()

    private val currentGroup = StringObject.groupName


    init {
        loadGroupJournal()
    }

    fun loadGroupJournal() {
        viewModelScope.launch {
            _uiState.value = GroupJournalUiState.Loading
            try {
                val data = repository.getPhysedJournal(currentGroup,"")
                _uiState.value = GroupJournalUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = GroupJournalUiState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }
}