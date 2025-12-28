package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dormitory(
    @SerialName("admission")
    val admission: String,
    @SerialName("agreements")
    val agreements: List<String>,
    @SerialName("balance")
    val balance: String,
    @SerialName("balance_currdate")
    val balanceCurrdate: String,
    @SerialName("bill")
    val bill: String,
    @SerialName("bill_next")
    val billNext: String,
    @SerialName("can_sign")
    val canSign: Boolean,
    @SerialName("client_email")
    val clientEmail: String,
    @SerialName("client_fio")
    val clientFio: String,
    @SerialName("contragent")
    val contragent: String,
    @SerialName("createDate")
    val createDate: String,
    @SerialName("dorm_num")
    val dormNum: String,
    @SerialName("dorm_room")
    val dormRoom: String,
    @SerialName("endDateFact")
    val endDateFact: String,
    @SerialName("endDatePlan")
    val endDatePlan: String,
    @SerialName("file")
    val `file`: String,
    @SerialName("id")
    val id: String,
    @SerialName("lastPaymentDate")
    val lastPaymentDate: String,
    @SerialName("level")
    val level: String,
    @SerialName("name")
    val name: String,
    @SerialName("number")
    val number: String,
    @SerialName("paygraph")
    val paygraph: List<Paygraph>,
    @SerialName("qr_current")
    val qrCurrent: String,
    @SerialName("qr_total")
    val qrTotal: String,
    @SerialName("sides")
    val sides: String,
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
    @SerialName("student")
    val student: String,
    @SerialName("sum")
    val sum: String,
    @SerialName("type")
    val type: String,
    @SerialName("user_email")
    val userEmail: String,
    @SerialName("user_fio")
    val userFio: String
)