package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UserStudentCard(
    @SerialName("guid_person")
    val guidPerson: String,

    @SerialName("PEP_status")
    val pepStatus: Boolean,

    @SerialName("has_hostel")
    val hasHostel: Boolean,

    val birthday: String? = null,
    val snils: String? = null,

    @SerialName("hostel_num")
    val hostelNum: String? = null,

    @SerialName("hostel_room")
    val hostelRoom: String? = null,

    val passSer: String? = null,
    val passNum: String? = null,
    val passDiv: String? = null,
    val passDate: String? = null,

    @SerialName("learn_status")
    val learnStatus: String? = null,

    @SerialName("specialty_code")
    val specialtyCode: String? = null,

    @SerialName("specialty_name")
    val specialtyName: String? = null,

    @SerialName("divisions_all")
    val divisionsAll: List<DivisionsAllX> = emptyList(),

    @SerialName("divisions_crs")
    val divisionsCrs: List<DivisionsCrX> = emptyList()
)

