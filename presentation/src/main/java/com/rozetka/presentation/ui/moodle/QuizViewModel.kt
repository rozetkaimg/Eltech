package com.rozetka.presentation.ui.moodle


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.MoodleRepository
import com.rozetka.domain.repository.UserRepository
import com.rozetka.model.QuizInfoNative
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface QuizUiState {
    object Loading : QuizUiState
    data class Success(val data: QuizInfoNative) : QuizUiState
    data class Error(val message: String) : QuizUiState
}

class QuizViewModel(
    private val repository: MoodleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun loadQuizInfo(quizId: String) {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            try {
                val session = userRepository.getMoodleSession() ?: ""
                val data = repository.getQuizInfoNative(session, quizId)
                if (data.title == "Ошибка") {
                    _uiState.value = QuizUiState.Error("Не удалось загрузить тест")
                } else {
                    _uiState.value = QuizUiState.Success(data)
                }
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}