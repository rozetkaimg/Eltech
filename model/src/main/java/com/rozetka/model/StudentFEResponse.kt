package com.rozetka.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class StudentResponse(
    val success: Boolean,
    val data: StudentData
)

@Serializable
data class StudentData(
    val students: List<Student>,
    val totalCount: Int
)

@Serializable
data class Student(
    val studentGuid: String,
    val fullName: String,
    val groupNumber: String,
    val course: Int,
    val visits: Int,
    val totalPoints: Int,
    val standardPoints: Int,
    val lmsPoints: Int,
    val hasDebt: Boolean,
    val healthGroup: String,
    val specialization: String
)