package com.rozetka.network.campus

import com.rozetka.model.campus.TeacherResponse
import com.rozetka.model.campus.UniversityData
import com.rozetka.model.campus.UserProfile

interface CampusMethods {
    suspend fun getBearerToken(): UserProfile
    suspend fun  getTeacher(bearerToken: String, id: String): TeacherResponse
    suspend fun  getTeachers(bearerToken: String): UniversityData
}