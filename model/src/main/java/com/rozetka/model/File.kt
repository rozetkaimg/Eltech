package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class File(
    @SerialName("name")
    val name: String,
    @SerialName("url")
    val url: String
)