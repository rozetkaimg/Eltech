package com.rozetka.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Act(
    @SerialName("id")
    val id: String,
    @SerialName("actId")
    val actId: String,
    @SerialName("status")
    val status: String,
    @SerialName("sign_variant")
    val signVariant: String,
    @SerialName("sides")
    val sides: String,
    @SerialName("reason")
    val reason: String,
    @SerialName("number")
    val number: String,
    @SerialName("date")
    val date: String,
    @SerialName("type")
    val type: String,
    @SerialName("file")
    val `file`: String,
    @SerialName("can_sign")
    val canSign: Boolean,
    @SerialName("signed_user")
    val signedUser: Boolean,
    @SerialName("signed_user_date")
    val signedUserDate: String
)