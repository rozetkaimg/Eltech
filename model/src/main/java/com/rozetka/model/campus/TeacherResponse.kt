package com.rozetka.model.campus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeacherResponse(
    val teacher: Teacher,
    val reviews: List<Review>
)

@Serializable
data class Teacher(
    @SerialName("_id")
    val id: String,
    val name: String,
    val rating: Rating
)

@Serializable
data class Rating(
    val value: Double,
    val weight: Double,
    val count: Int,
    val submitted: Boolean,
    val criteria: List<Criterion>,
    val tags: List<TeacherTag>
)

@Serializable
data class Criterion(
    val title: String,
    val value: Double
)

@Serializable
data class TeacherTag(
    val title: String,
    val count: Int,
    val percentage: Int
)

@Serializable
data class Review(
    @SerialName("_id")
    val id: String,
    val author: String?,
    val content: String,
    val status: String,
    val createdAt: String,
    val value: Double,
    val tags: List<String>,
    val reactions: Reactions
)

@Serializable
data class Reactions(
    val likes: Int,
    val dislikes: Int
)