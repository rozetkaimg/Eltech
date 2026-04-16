package com.rozetka.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val login: String,
    val password: String,
    val token: String,
    val name: String,
    val group: String,
    val avatar: String,
    val userId: Int = 0,
    val guid: String = "",
    val isActive: Boolean = false
)