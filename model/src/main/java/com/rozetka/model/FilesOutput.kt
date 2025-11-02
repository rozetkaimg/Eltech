package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FilesOutput(
    @SerialName("fname")
    val fname: String,
    @SerialName("url")
    val url: String
)