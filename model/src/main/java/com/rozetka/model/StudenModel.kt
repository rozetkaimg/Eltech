package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudenModel(
    @SerialName("current_page")
    val currentPage: String,
    @SerialName("items")
    val items: List<Item>,
    @SerialName("pages")
    val pages: String,
    @SerialName("per_page")
    val perPage: String
)