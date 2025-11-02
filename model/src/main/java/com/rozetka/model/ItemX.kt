package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemX(
    @SerialName("avatar")
    val avatar: String,
    @SerialName("division")
    val division: String,
    @SerialName("email")
    val email: String,
    @SerialName("fio")
    val fio: String,
    @SerialName("id")
    val id: String,
    @SerialName("post")
    val post: String
)