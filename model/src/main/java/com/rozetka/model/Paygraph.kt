package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Paygraph(
    @SerialName("date_end")
    val dateEnd: String,
    @SerialName("date_plan")
    val datePlan: String,
    @SerialName("date_start")
    val dateStart: String,
    @SerialName("semestr")
    val semestr: String,
    @SerialName("sum")
    val sum: String,
    @SerialName("sum_pay")
    val sumPay: String,
    @SerialName("sum_price")
    val sumPrice: String,
    @SerialName("year")
    val year: String
)