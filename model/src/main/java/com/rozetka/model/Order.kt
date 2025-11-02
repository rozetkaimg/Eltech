package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    @SerialName("comment")
    val comment: String,
    @SerialName("date")
    val date: String,
    @SerialName("fullname")
    val fullname: String,
    @SerialName("name")
    val name: String,
    @SerialName("number")
    val number: String,
    @SerialName("reason")
    val reason: String
)