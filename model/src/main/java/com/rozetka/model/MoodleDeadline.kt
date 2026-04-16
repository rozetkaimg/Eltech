package com.rozetka.model

import kotlinx.serialization.Serializable

@Serializable
data class MoodleDeadline(
    val id: String,
    val courseName: String,
    val name: String,
    val formattedDate: String,
    val url: String,
    val type: String = "event"
)
