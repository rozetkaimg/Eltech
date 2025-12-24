package com.rozetka.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Curator(
    @SerialName("guid")
    val guid: String,

    @SerialName("fullName")
    val fullName: String
)

@Serializable
data class PointsHistory(
    @SerialName("id")
    val id: Int,

    @SerialName("date")
    val date: String,

    @SerialName("points")
    val points: Int,

    @SerialName("type")
    val type: String,

    @SerialName("comment")
    val comment: String,

    @SerialName("teacherGuid")
    val teacherGuid: String,

    @SerialName("teacherFullName")
    val teacherFullName: String
)

@Serializable
data class VisitsHistory(
    @SerialName("id")
    val id: Int,

    @SerialName("date")
    val date: String,

    @SerialName("teacherGuid")
    val teacherGuid: String,

    @SerialName("teacherFullName")
    val teacherFullName: String
)

@Serializable
data class PhysEdJournalResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: FKStudentData? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("detail")
    val detail: String? = null
)

@Serializable
data class FKStudentData(
    @SerialName("studentGuid")
    val studentGuid: String,

    @SerialName("fullName")
    val fullName: String,

    @SerialName("groupNumber")
    val groupNumber: String,

    @SerialName("hasDebt")
    val hasDebt: Boolean,

    @SerialName("hadDebtInSemester")
    val hadDebtInSemester: Boolean,

    @SerialName("totalPoints")
    val totalPoints: Int,

    @SerialName("lmsPoints")
    val lmsPoints: Int,

    @SerialName("course")
    val course: Int,

    @SerialName("curator")
    val curator: Curator? = null,

    @SerialName("healthGroupTeacher")
    val healthGroupTeacher: Curator? = null,

    @SerialName("healthGroup")
    val healthGroup: String,

    @SerialName("specialization")
    val specialization: String,

    @SerialName("pointsHistory")
    val pointsHistory: List<PointsHistory>,

    @SerialName("visitsHistory")
    val visitsHistory: List<VisitsHistory>,

    @SerialName("standardsHistory")
    val standardsHistory: List<PointsHistory> = emptyList()
)