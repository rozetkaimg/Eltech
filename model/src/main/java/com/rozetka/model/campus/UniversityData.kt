package com.rozetka.model.campus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UniversityData(
    @SerialName("reviews")
    val reviews: List<ReviewSmall>,
    @SerialName("teachers")
    val teachers: List<TeacherSmall>
)


@Serializable
data class ReviewSmall(
    @SerialName("_id")
    val id: String,
    val content: String,
    val status: String,
    val value: Double,
    val tags: List<ReviewTag>,
    val teacher: ShortTeacher
)

@Serializable
data class ReviewTag(
    @SerialName("_id")
    val id: String,
    val title: String
)

@Serializable
data class ShortTeacher(
    @SerialName("_id")
    val id: String,
    val name: String
)


@Serializable
data class TeacherSmall(
    @SerialName("_id")
    val id: String,
    val name: String,
    val rating: TeacherRating
)

@Serializable
data class TeacherRating(
    val value: Double,
    val weight: Double,
    val count: Int
)