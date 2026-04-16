package com.rozetka.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dormitory(
    @SerialName("id")
    val id: String,
    @SerialName("contragent")
    val contragent: String,
    @SerialName("student")
    val student: String,
    @SerialName("number")
    val number: String,
    @SerialName("name")
    val name: String,
    @SerialName("type")
    val type: String,
    @SerialName("level")
    val level: String,
    @SerialName("sides")
    val sides: String,
    @SerialName("status1c")
    val status1c: String? = null,
    @SerialName("admission")
    val admission: String,
    @SerialName("user_fio")
    val userFio: String,
    @SerialName("user_email")
    val userEmail: String,
    @SerialName("client_fio")
    val clientFio: String,
    @SerialName("client_email")
    val clientEmail: String,
    @SerialName("dorm_num")
    val dormNum: String,
    @SerialName("dorm_room")
    val dormRoom: String,
    @SerialName("file")
    val `file`: String,
    @SerialName("bill")
    val bill: String,
    @SerialName("bill_next")
    val billNext: String,
    @SerialName("can_sign")
    val canSign: Boolean,
    @SerialName("sign_text")
    val signText: String,
    @SerialName("sign_variant")
    val signVariant: String,
    @SerialName("signed_user")
    val signedUser: Boolean,
    @SerialName("signed_user_date")
    val signedUserDate: String,
    @SerialName("signed_user_time")
    val signedUserTime: String,
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDatePlan")
    val endDatePlan: String,
    @SerialName("endDateFact")
    val endDateFact: String,
    @SerialName("createDate")
    val createDate: String,
    @SerialName("qr_current")
    val qrCurrent: String,
    @SerialName("qr_total")
    val qrTotal: String,
    @SerialName("sum")
    val sum: String,
    @SerialName("balance")
    val balance: String,
    @SerialName("balance_currdate")
    val balanceCurrdate: String,
    @SerialName("lastPaymentDate")
    val lastPaymentDate: String,
    @SerialName("payments")
    val payments: List<Payment> = emptyList(),
    @SerialName("agreements")
    val agreements: List<Agreement> = emptyList(),
    @SerialName("acts")
    val acts: List<Act> = emptyList(),
    @SerialName("paygraph")
    val paygraph: List<Paygraph> = emptyList()
)