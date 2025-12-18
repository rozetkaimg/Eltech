package com.rozetka.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchStudentResponse(
    @SerialName("pages")
    val pages: String,

    @SerialName("per_page")
    val perPage: String,

    @SerialName("current_page")
    val currentPage: String,

    @SerialName("items")
    val items: List<StudentR>
)

@Serializable
data class StudentR(
    @SerialName("fio")
    val fio: String,

    @SerialName("group")
    val group: String,

    @SerialName("faculty")
    val faculty: String,

    @SerialName("id")
    val id: String,

    @SerialName("avatar")
    val avatar: String
)