package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    @SerialName("comment")
    val comment: String,
    @SerialName("course")
    val course: Int,
    @SerialName("dateFrom")
    val dateFrom: String,
    @SerialName("dateTo")
    val dateTo: String,
    @SerialName("evening")
    val evening: Int,
    @SerialName("title")
    val title: String
)