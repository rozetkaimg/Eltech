package com.rozetka.presentation.ui.academicPerformance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.model.AcademicPerformanceItem
import com.rozetka.network.MospolytechMethods
import com.rozetka.domain.util.StringObject.ApiToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AcademicPerformanceUiState {
    data class Success(val data: List<AcademicPerformanceItem>) : AcademicPerformanceUiState
    data class Error(val message: String? = null) : AcademicPerformanceUiState
    object Loading : AcademicPerformanceUiState
}

class AcademicPerformanceViewModel(
    private val repository: MospolytechMethods
) : ViewModel() {

    private val _uiState = MutableStateFlow<AcademicPerformanceUiState>(AcademicPerformanceUiState.Loading)
    val uiState: StateFlow<AcademicPerformanceUiState> = _uiState.asStateFlow()

    private val performanceDataBySemester = mutableMapOf<String, List<AcademicPerformanceItem>>()

    fun loadAcademicPerformance() {
        if (performanceDataBySemester.containsKey("w")) {
            _uiState.value = AcademicPerformanceUiState.Success(performanceDataBySemester["2"]!!)
            return
        }

        viewModelScope.launch {
            _uiState.value = AcademicPerformanceUiState.Loading
            try {
                val result = repository.getAcademicPerformanceBySemestr(ApiToken, "s")
                performanceDataBySemester["2"] = result.academicPerformance
                _uiState.value = AcademicPerformanceUiState.Success(result.academicPerformance)
            } catch (e: Exception) {
                _uiState.value = AcademicPerformanceUiState.Error(e.message)
            }
        }
    }
}