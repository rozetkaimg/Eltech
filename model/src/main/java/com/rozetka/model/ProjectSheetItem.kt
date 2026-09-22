package com.rozetka.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectSheetItem(
    @SerialName("number")
    val number: String = "",

    @SerialName("direction")
    val direction: String = "",

    @SerialName("project_title")
    val projectTitle: String = "",

    @SerialName("teacher")
    val teacher: String = "",

    @SerialName("contact_info")
    val contactInfo: String = "",

    @SerialName("schedule_by_days")
    val scheduleByDays: Map<String, String> = emptyMap()
)
