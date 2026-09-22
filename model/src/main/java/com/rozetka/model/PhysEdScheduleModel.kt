package com.rozetka.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhysEdScheduleResponse(
    @SerialName("metadata")
    val metadata: PhysEdMetadata? = null,

    @SerialName("locations")
    val locations: List<PhysEdLocation> = emptyList(),

    @SerialName("schedule")
    val schedule: List<PhysEdTimeSlot> = emptyList()
)

@Serializable
data class PhysEdMetadata(
    @SerialName("title")
    val title: String = "",

    @SerialName("semester")
    val semester: String = "",

    @SerialName("source_file")
    val sourceFile: String = ""
)

@Serializable
data class PhysEdLocation(
    @SerialName("id")
    val id: String = "",

    @SerialName("name")
    val name: String = "",

    @SerialName("address")
    val address: String = ""
)

@Serializable
data class PhysEdTimeSlot(
    @SerialName("time_slot")
    val timeSlot: String = "",

    @SerialName("classes")
    val classes: List<PhysEdClass> = emptyList()
)

@Serializable
data class PhysEdClass(
    @SerialName("disciplines")
    val disciplines: List<String> = emptyList(),

    @SerialName("location_ids")
    val locationIds: List<String> = emptyList()
)

@Serializable
data class PhysEdSubscribedClass(
    @SerialName("id")
    val id: String,

    @SerialName("discipline")
    val discipline: String,

    @SerialName("timeSlot")
    val timeSlot: String,

    @SerialName("locationName")
    val locationName: String = "",

    @SerialName("locationAddress")
    val locationAddress: String = "",

    @SerialName("dayOfWeek")
    val dayOfWeek: String = ""
)
