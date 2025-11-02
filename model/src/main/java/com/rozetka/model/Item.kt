package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    @SerialName("avatar")
    val avatar: String,
    @SerialName("faculty")
    val faculty: String,
    @SerialName("fio")
    val fio: String,
    @SerialName("group")
    val group: String,
    @SerialName("id")
    val id: String
)