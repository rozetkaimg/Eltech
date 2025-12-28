package com.rozetka.model.campus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("_id")
    val id: String,
    val token: String,
    val isAnonymous: Boolean,
    val promocodes: List<String>,
    val phone: String?,
    val email: String?,
    val telegramId: String?,
    val nickname: String?,
    val course: String?,
    val program: String?
)