package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PayModel(
    @SerialName("contracts")
    val contracts: Contracts
)