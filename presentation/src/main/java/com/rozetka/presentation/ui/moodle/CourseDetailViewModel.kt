package com.rozetka.presentation.ui.moodle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.MoodleRepository
import com.rozetka.model.CourseSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CourseDetailUiState {
    object Loading : CourseDetailUiState
    data class Success(val sections: List<CourseSection>) : CourseDetailUiState
    data class Error(val message: String) : CourseDetailUiState
}

class CourseDetailViewModel(
    private val repository: MoodleRepository,
    private val moodleSession: String,
    private val courseId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<CourseDetailUiState>(CourseDetailUiState.Loading)
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        loadCourseDetail()
    }

    private fun loadCourseDetail() {
        viewModelScope.launch {
            _uiState.value = CourseDetailUiState.Loading
            val result = repository.getCourseDetail(moodleSession, courseId)
            if (result.isNotEmpty()) {
                _uiState.value = CourseDetailUiState.Success(result)
            } else {
                _uiState.value = CourseDetailUiState.Error("Не удалось загрузить содержимое курса")
            }
        }
    }
}