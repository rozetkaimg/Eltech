package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicPerformanceItem(
    @SerialName("bill_num")
    val billNum: String,
    @SerialName("bill_type")
    val billType: String,
    @SerialName("chair")
    val chair: String,
    @SerialName("course")
    val course: String,
    @SerialName("doc_type")
    val docType: String,
    @SerialName("exam_date")
    val examDate: String,
    @SerialName("exam_time")
    val examTime: String,
    @SerialName("exam_type")
    val examType: String,
    @SerialName("grade")
    val grade: String,
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("semestr")
    val semestr: String,
    @SerialName("teacher")
    val teacher: String,
    @SerialName("ticket_num")
    val ticketNum: String,
    @SerialName("year")
    val year: String
)