package com.rozetka.model

import kotlinx.serialization.Serializable

@Serializable
data class MoodleCourse(
    val id: String,
    val title: String,
    val category: String,
    val link: String,
    val grade: String? = null,
    val gradeMax: String? = null,
    val competencies: String? = null,
    val progress: String? = null,
    val status: String? = null
)

@Serializable
data class CourseSection(
    val id: String,
    val name: String,
    val modules: List<CourseModule>
)

@Serializable
data class CourseModule(
    val id: String,
    val name: String,
    val type: ModuleType,
    val link: String,
    val isCompleted: Boolean = false
)

@Serializable
data class GradeItem(
    val name: String,
    val weight: String,
    val grade: String,
    val range: String,
    val percentage: String
)

@Serializable
data class ParticipantItem(
    val id: String,
    val fullName: String,
    val avatarUrl: String,
    val roles: String,
    val lastAccess: String
)

@Serializable
data class QuizAttemptItem(
    val attemptNumber: String,
    val state: String,
    val marks: String,
    val grade: String
)

@Serializable
data class QuizInfoNative(
    val title: String,
    val description: String,
    val rules: List<String>,
    val attempts: List<QuizAttemptItem>,
    val canAttempt: Boolean,
    val attemptUrl: String
)

@Serializable
data class ActiveQuizAttempt(
    val attemptId: String,
    val cmid: String, // ID модуля курса
    val sesskey: String,
    val questions: List<QuizQuestion>,
    val hiddenInputs: Map<String, String> // Важные скрытые токены формы
)

@Serializable
data class QuizQuestion(
    val slot: String, // Номер вопроса (например, "1")
    val id: String, // Уникальный ID вопроса в Moodle
    val text: String, // Текст вопроса
    val sequenceCheck: String, // Защитный токен конкретного вопроса
    val type: QuestionType,
    val options: List<QuizOption>
)

@Serializable
data class QuizOption(
    val id: String, // ID html-элемента
    val name: String, // Имя поля для POST запроса (например, "q12345:1_answer")
    val value: String, // Значение (например, "0", "1")
    val text: String // Текст варианта ответа
)

enum class QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE, UNKNOWN
}

enum class ModuleType {
    RESOURCE, ASSIGN, QUIZ, FORUM, FOLDER, PAGE, URL, UNKNOWN
}
