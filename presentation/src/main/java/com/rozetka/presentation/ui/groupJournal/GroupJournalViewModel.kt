package com.rozetka.presentation.ui.groupJournal

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.GroupJournalRepository
import com.rozetka.domain.util.StringObject
import com.rozetka.model.StudentResponse
import com.rozetka.presentation.R
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
    private val repository: GroupJournalRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<GroupJournalUiState>(GroupJournalUiState.Loading)
    val uiState: StateFlow<GroupJournalUiState> = _uiState.asStateFlow()

    init {
        loadGroupJournal()
    }

    fun loadGroupJournal() {
        viewModelScope.launch {
            _uiState.value = GroupJournalUiState.Loading
            val groupToFetch = StringObject.groupName.ifBlank {
                _uiState.value = GroupJournalUiState.Error(application.getString(R.string.error_group_not_found_prompt))
                return@launch
            }
            try {
                val data = repository.getGroupJournal(groupToFetch)
                _uiState.value = GroupJournalUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = GroupJournalUiState.Error(
                    application.getString(
                        R.string.error_load_prefix,
                        e.message ?: application.getString(R.string.error_unknown)
                    )
                )
            }
        }
    }
}
