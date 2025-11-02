package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Lastmessage(
    @SerialName("datetime")
    val datetime: String,
    @SerialName("files")
    val files: List<File>,
    @SerialName("from")
    val from: String,
    @SerialName("html")
    val html: String,
    @SerialName("readed")
    val readed: Boolean,
    @SerialName("readed_opponent")
    val readedOpponent: Boolean,
    @SerialName("text")
    val text: String
)