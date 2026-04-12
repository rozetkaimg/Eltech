package com.rozetka.presentation.ui.moodle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.MoodleRepository
import com.rozetka.domain.repository.UserRepository
import com.rozetka.model.CourseSection
import com.rozetka.model.GradeItem
import com.rozetka.model.MoodleCourse
import com.rozetka.model.ParticipantItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MoodleUiState {
    object Initial : MoodleUiState
    object Loading : MoodleUiState
    data class Success(val courses: List<MoodleCourse>) : MoodleUiState
    data class Error(val message: String) : MoodleUiState
    object Authenticating : MoodleUiState
    data class DetailSuccess(
        val sections: List<CourseSection>,
        val grades: List<GradeItem>,
        val participants: List<ParticipantItem>,
        val participantsPage: Int = 0,
        val hasMoreParticipants: Boolean = false,
        val isLoadingMore: Boolean = false
    ) : MoodleUiState
}

class MoodleViewModel(
    private val repository: MoodleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MoodleUiState>(MoodleUiState.Initial)
    val uiState: StateFlow<MoodleUiState> = _uiState.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val session = userRepository.getMoodleSession()
            if (!session.isNullOrEmpty()) {
                loadCourses(session)
            }
        }
    }

    private suspend fun loadCourses(session: String) {
        _uiState.value = MoodleUiState.Loading
        try {
            val courses = repository.getCourses(session)
            if (courses.isNotEmpty()) {
                _uiState.value = MoodleUiState.Success(courses)
            } else {
                _uiState.value = MoodleUiState.Error("Курсы не найдены. Возможно, сессия истекла.")
            }
        } catch (e: Exception) {
            _uiState.value = MoodleUiState.Error("Ошибка загрузки курсов: ${e.message}")
        }
    }

    fun onSessionCaptured(moodleSession: String) {
        viewModelScope.launch {
            userRepository.saveMoodleSession(moodleSession)
            loadCourses(moodleSession)
        }
    }

    fun startAuth() {
        _uiState.value = MoodleUiState.Authenticating
    }

    fun loadCourseDetail(courseId: String) {
        viewModelScope.launch {
            _uiState.value = MoodleUiState.Loading
            try {
                val moodleSession = userRepository.getMoodleSession() ?: ""
                val sections = repository.getCourseDetail(moodleSession, courseId)
                val grades = repository.getCourseGrades(moodleSession, courseId)
                val (participants, hasNext) = repository.getCourseParticipants(moodleSession, courseId, 0)

                _uiState.value = MoodleUiState.DetailSuccess(
                    sections = sections,
                    grades = grades,
                    participants = participants,
                    participantsPage = 0,
                    hasMoreParticipants = hasNext,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                _uiState.value = MoodleUiState.Error("Ошибка загрузки данных курса: ${e.message}")
            }
        }
    }

    fun loadNextParticipantsPage(courseId: String) {
        val currentState = _uiState.value
        if (currentState is MoodleUiState.DetailSuccess && currentState.hasMoreParticipants && !currentState.isLoadingMore) {
            _uiState.value = currentState.copy(isLoadingMore = true)

            viewModelScope.launch {
                val nextPage = currentState.participantsPage + 1
                try {
                    val moodleSession = userRepository.getMoodleSession() ?: ""
                    val (newParticipants, hasNext) = repository.getCourseParticipants(moodleSession, courseId, nextPage)

                    _uiState.value = currentState.copy(
                        participants = currentState.participants + newParticipants,
                        participantsPage = nextPage,
                        hasMoreParticipants = hasNext,
                        isLoadingMore = false
                    )
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(isLoadingMore = false)
                }
            }
        }
    }
}