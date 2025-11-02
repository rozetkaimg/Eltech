package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Contracts(
    @SerialName("dormitory")
    val dormitory: List<Dormitory>,
    @SerialName("education")
    val education: List<Dormitory>
)