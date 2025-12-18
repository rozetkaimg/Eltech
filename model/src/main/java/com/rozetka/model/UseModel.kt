package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UseModel(
    @SerialName("user")
    val user: User
)