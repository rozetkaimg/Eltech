package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AuthModelState(
    val responseCode: Int,
    val authResponseModel: AuthResponseModel? = null
)

@Serializable
data class AuthResponseModel(
    @SerialName("guid")
    val guid: String,
    @SerialName("jwt")
    val jwt: String,
    @SerialName("jwt_refresh")
    val jwtRefresh: String,
    @SerialName("token")
    val token: String
)
