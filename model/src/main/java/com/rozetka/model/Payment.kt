package com.rozetka.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    @SerialName("date")
    val date: String,
    @SerialName("value")
    val value: String
)