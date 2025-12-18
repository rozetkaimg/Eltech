package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDataModelItem(
    @SerialName("bdate")
    val bdate: String,
    @SerialName("clients")
    val clients: List<String>,
    @SerialName("date")
    val date: String,
    @SerialName("email")
    val email: String,
    @SerialName("file")
    val `file`: String,
    @SerialName("fio")
    val fio: String,
    @SerialName("login")
    val login: String,
    @SerialName("passDate")
    val passDate: String,
    @SerialName("passDiv")
    val passDiv: String,
    @SerialName("passNum")
    val passNum: String,
    @SerialName("passSer")
    val passSer: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("status")
    val status: Boolean,
    @SerialName("time")
    val time: String
)