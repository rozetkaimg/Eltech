package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUserData(
    @SerialName("birthday")
    val birthday: String,
    @SerialName("code")
    val code: String,
    @SerialName("contracts")
    val contracts: List<String>,
    @SerialName("course")
    val course: String,
    @SerialName("degreeLength")
    val degreeLength: String,
    @SerialName("degreeLevel")
    val degreeLevel: String,
    @SerialName("divisions_all")
    val divisionsAll: List<DivisionsAll>,
    @SerialName("divisions_crs")
    val divisionsCrs: List<DivisionsCr>,
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
    @SerialName("guid_person")
    val guidPerson: String,
    @SerialName("has_hostel")
    val hasHostel: Boolean,
    @SerialName("hostel_num")
    val hostelNum: String,
    @SerialName("hostel_room")
    val hostelRoom: String,
    @SerialName("id")
    val id: String,
    @SerialName("learn_status")
    val learnStatus: String,
    @SerialName("name")
    val name: String,
    @SerialName("orders")
    val orders: List<Order>,
    @SerialName("PEP_status")
    val pEPStatus: Boolean,
    @SerialName("passDate")
    val passDate: String,
    @SerialName("passDiv")
    val passDiv: String,
    @SerialName("passNum")
    val passNum: String,
    @SerialName("passSer")
    val passSer: String,
    @SerialName("patronymic")
    val patronymic: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("sex")
    val sex: String,
    @SerialName("snils")
    val snils: String,
    @SerialName("specialization")
    val specialization: String,
    @SerialName("specialty")
    val specialty: String,
    @SerialName("specialty_code")
    val specialtyCode: String,
    @SerialName("specialty_name")
    val specialtyName: String,
    @SerialName("subdivisions")
    val subdivisions: List<String>,
    @SerialName("surname")
    val surname: String,
    @SerialName("user_status")
    val userStatus: String
)