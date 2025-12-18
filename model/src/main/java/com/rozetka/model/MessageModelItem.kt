package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageModelItem(
    @SerialName("id")
    val id: String,
    @SerialName("lastmessage")
    val lastmessage: Lastmessage,
    @SerialName("opponent")
    val opponent: Opponent,
    @SerialName("subject")
    val subject: String
)