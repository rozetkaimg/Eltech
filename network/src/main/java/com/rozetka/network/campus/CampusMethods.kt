package com.rozetka.network.campus

import com.rozetka.model.campus.ReviewOptions
import com.rozetka.model.campus.TeacherResponse
import com.rozetka.model.campus.TeacherReviewRequest
import com.rozetka.model.campus.UniversityData
import com.rozetka.model.campus.UserProfile

interface CampusMethods {
    suspend fun getBearerToken(): UserProfile
    suspend fun getTeacher(bearerToken: String, id: String): TeacherResponse
    suspend fun getTeachers(bearerToken: String): UniversityData
    suspend fun setReaction(bearerToken: String, reaction: String, id: String)
    suspend fun getTeacherRatingFields(bearerToken: String, teacherId: String): ReviewOptions
    suspend fun sendTeacherReview(bearerToken: String, teacherId: String, reviewBody: TeacherReviewRequest)
}