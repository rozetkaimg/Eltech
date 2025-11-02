package com.rozetka.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ScheduleModel(
    @SerialName("status")
    val status: String,
    @SerialName("grid")
    val grid: Map<String, Map<String, List<Lesson>>>,
    @SerialName("group")
    val group: Group,
    @SerialName("isSession")
    val isSession: Boolean
)


@Serializable
data class Lesson(
    @SerialName("sbj")
    val sbj: String,
    @SerialName("teacher")
    val teacher: String,
    @SerialName("dts")
    val dts: String,
    @SerialName("df")
    val df: String,
    @SerialName("dt")
    val dt: String,
    @SerialName("auditories")
    val auditories: List<Auditory>,
    @SerialName("type")
    val type: String,
    @SerialName("e_link")
    val eLink: String? = null
)


@Serializable
data class Auditory(
    @SerialName("title")
    val title: String,
    @SerialName("color")
    val color: String
)

