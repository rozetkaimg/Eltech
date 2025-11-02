package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsModelItem(
    @SerialName("content")
    val content: String,
    @SerialName("date")
    val date: String,
    @SerialName("id")
    val id: String,
    @SerialName("time")
    val time: String,
    @SerialName("title")
    val title: String
)