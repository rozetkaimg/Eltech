package com.rozetka.model.campus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewOptions(
    @SerialName("criteria")
    val criteria: List<Criteria>,

    @SerialName("tags")
    val tags: List<Tag>
)

@Serializable
data class Criteria(
    @SerialName("_id")
    val id: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String
)

@Serializable
data class Tag(
    @SerialName("_id")
    val id: String,

    @SerialName("title")
    val title: String
)