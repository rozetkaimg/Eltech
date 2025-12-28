package com.rozetka.model

import kotlinx.serialization.Serializable

@Serializable
data class RaspData(
    val groups: Map<String, Boolean>
)