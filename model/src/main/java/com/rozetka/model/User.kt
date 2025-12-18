package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("avatar")
    val avatar: String,
    @SerialName("birthday")
    val birthday: String,
    @SerialName("code")
    val code: String,
    @SerialName("course")
    val course: String,
    @SerialName("degreeLength")
    val degreeLength: String,
    @SerialName("degreeLevel")
    val degreeLevel: String,
    @SerialName("educationForm")
    val educationForm: String,
    @SerialName("email")
    val email: String,
    @SerialName("enterYear")
    val enterYear: String,
    @SerialName("faculty")
    val faculty: String,
    @SerialName("finance")
    val finance: String,
    @SerialName("group")
    val group: String,
    @SerialName("hasAlerts")
    val hasAlerts: Boolean,
    @SerialName("id")
    val id: Int,
    @SerialName("lastaccess")
    val lastaccess: String,
    @SerialName("name")
    val name: String,
    @SerialName("patronymic")
    val patronymic: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("sex")
    val sex: String,
    @SerialName("specialization")
    val specialization: String,
    @SerialName("specialty")
    val specialty: String,
    @SerialName("status")
    val status: String,
    @SerialName("surname")
    val surname: String,
    @SerialName("user_status")
    val userStatus: String,
    @SerialName("vacation_end")
    val vacationEnd: String? = null,
    @SerialName("vacation_start")
    val vacationStart: String? = null
)