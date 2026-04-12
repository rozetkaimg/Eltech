package com.rozetka.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Agreement(
    @SerialName("id")
    val id: String,
    @SerialName("status")
    val status: String,
    @SerialName("sign_variant")
    val signVariant: String,
    @SerialName("sides")
    val sides: String,
    @SerialName("user_fio")
    val userFio: String,
    @SerialName("user_email")
    val userEmail: String,
    @SerialName("client_fio")
    val clientFio: String,
    @SerialName("client_email")
    val clientEmail: String,
    @SerialName("code_sent")
    val codeSent: Boolean,
    @SerialName("reason")
    val reason: String,
    @SerialName("name")
    val name: String,
    @SerialName("type")
    val type: String,
    @SerialName("date")
    val date: String,
    @SerialName("file")
    val `file`: String,
    @SerialName("can_sign")
    val canSign: Boolean,
    @SerialName("signed_user")
    val signedUser: Boolean,
    @SerialName("signed_user_date")
    val signedUserDate: String,
    @SerialName("signed_user_time")
    val signedUserTime: String
)