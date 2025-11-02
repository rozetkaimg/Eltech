package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FilesInput(
    @SerialName("name")
    val name: String,
    @SerialName("url")
    val url: String
)