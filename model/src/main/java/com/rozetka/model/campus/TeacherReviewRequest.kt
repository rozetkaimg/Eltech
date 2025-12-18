package com.rozetka.model.campus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class TeacherReviewRequest(
    @SerialName("content")
    val content: String,

    @SerialName("criteria")
    val criteria: List<ReviewCriteriaValue>,

    @SerialName("tags")
    val tags: List<String>
)

@Serializable
data class ReviewCriteriaValue(
    @SerialName("_id")
    val id: String,

    @SerialName("value")
    val value: Int
)