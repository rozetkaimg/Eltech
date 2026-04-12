package com.rozetka.domain.repository

import com.rozetka.model.ActiveQuizAttempt
import com.rozetka.model.CourseSection
import com.rozetka.model.GradeItem
import com.rozetka.model.MoodleCourse
import com.rozetka.model.ParticipantItem
import com.rozetka.model.QuizInfoNative
import com.rozetka.model.MoodleDeadline

interface MoodleRepository {
    suspend fun getDeadlines(moodleSession: String, time: Long? = null): List<MoodleDeadline>
    suspend fun getCourses(moodleSession: String): List<MoodleCourse>
    suspend fun getCourseDetail(moodleSession: String, courseId: String): List<CourseSection>
    suspend fun getCourseGrades(moodleSession: String, courseId: String): List<GradeItem>
    suspend fun getCourseParticipants(moodleSession: String, courseId: String, page: Int = 0): Pair<List<ParticipantItem>, Boolean>
    suspend fun getQuizInfoNative(moodleSession: String, quizId: String): QuizInfoNative
    suspend fun getActiveAttempt(moodleSession: String, attemptUrl: String): ActiveQuizAttempt?
    suspend fun submitQuizAnswers(
        moodleSession: String,
        attemptId: String,
        answers: Map<String, String>,
        hiddenInputs: Map<String, String>,
        isFinish: Boolean = false
    ): Boolean
}
