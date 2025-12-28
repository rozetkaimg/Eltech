package com.rozetka.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


typealias ScheduleByDay = Map<String, List<LessonS>>

@Serializable
data class LessonS(
    val name: String? = null,
    @SerialName("timeInterval")
    val timeInterval: String? = null,
    val place: String? = null,
    val rooms: List<String> = emptyList(),
    val teachers: List<String> = emptyList(),
    val groups: String? = null,
    @SerialName("dateInterval")
    val dateInterval: String? = null,
    val link: String? = null
)