package com.rozetka.model

import kotlinx.serialization.Serializable

@Serializable
data class SignInRequest(
    val ulogin: String,
    val upassword: String
)