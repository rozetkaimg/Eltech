package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DivisionsCrX(
    @SerialName("contact")
    val contact: String,
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String
)