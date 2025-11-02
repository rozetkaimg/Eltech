package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDialogItem(
    @SerialName("author_id")
    val authorId: String,
    @SerialName("author_name")
    val authorName: String,
    @SerialName("datetime")
    val datetime: String,
    @SerialName("files")
    val files: List<File>,
    @SerialName("html")
    val html: String,
    @SerialName("msg_id")
    val msgId: String,
    @SerialName("readed")
    val readed: Boolean,
    @SerialName("readed_opponent")
    val readedOpponent: Boolean
)