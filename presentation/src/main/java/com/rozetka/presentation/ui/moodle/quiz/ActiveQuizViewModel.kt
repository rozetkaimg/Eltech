package com.rozetka.presentation.ui.moodle.quiz



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.repository.MoodleRepository
import com.rozetka.domain.repository.UserRepository
import com.rozetka.model.ActiveQuizAttempt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ActiveQuizUiState {
    object Loading : ActiveQuizUiState
    data class Active(
        val attempt: ActiveQuizAttempt,
        val selectedAnswers: Map<String, String> // name -> value
    ) : ActiveQuizUiState
    object Finished : ActiveQuizUiState
    data class Error(val message: String) : ActiveQuizUiState
}

class ActiveQuizViewModel(
    private val repository: MoodleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActiveQuizUiState>(ActiveQuizUiState.Loading)
    val uiState: StateFlow<ActiveQuizUiState> = _uiState.asStateFlow()

    fun loadAttempt(attemptUrl: String) {
        viewModelScope.launch {
            _uiState.value = ActiveQuizUiState.Loading
            val session = userRepository.getMoodleSession() ?: ""
            val attempt = repository.getActiveAttempt(session, attemptUrl)
            if (attempt != null) {
                _uiState.value = ActiveQuizUiState.Active(attempt, emptyMap())
            } else {
                // Если не удалось загрузить, возможно сессия истекла
                if (session.isEmpty()) {
                    _uiState.value = ActiveQuizUiState.Error("Сессия не найдена. Пожалуйста, авторизуйтесь в Moodle.")
                } else {
                    _uiState.value = ActiveQuizUiState.Error("Не удалось загрузить тест. Возможно, сессия истекла или тип вопросов не поддерживается.")
                }
            }
        }
    }

    fun selectAnswer(inputName: String, inputValue: String, sequenceCheckName: String, sequenceCheckValue: String) {
        val currentState = _uiState.value
        if (currentState is ActiveQuizUiState.Active) {
            val newAnswers = currentState.selectedAnswers.toMutableMap()
            newAnswers[inputName] = inputValue

            // Moodle требует отправлять token sequencecheck вместе с каждым измененным ответом
            newAnswers[sequenceCheckName] = sequenceCheckValue

            _uiState.value = currentState.copy(selectedAnswers = newAnswers)
        }
    }

    fun submitTest() {
        val currentState = _uiState.value
        if (currentState is ActiveQuizUiState.Active) {
            _uiState.value = ActiveQuizUiState.Loading
            viewModelScope.launch {
                val session = userRepository.getMoodleSession() ?: ""
                val success = repository.submitQuizAnswers(
                    moodleSession = session,
                    attemptId = currentState.attempt.attemptId,
                    answers = currentState.selectedAnswers,
                    hiddenInputs = currentState.attempt.hiddenInputs,
                    isFinish = true // Завершаем попытку
                )

                if (success) {
                    _uiState.value = ActiveQuizUiState.Finished
                } else {
                    _uiState.value = ActiveQuizUiState.Error("Ошибка при отправке ответов.")
                }
            }
        }
    }
}