package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PDModel(
    @SerialName("arrear")
    val arrear: String,
    @SerialName("arrear_balls")
    val arrearBalls: String,
    @SerialName("arrear_result")
    val arrearResult: String,
    @SerialName("curator")
    val curator: String,
    @SerialName("current_att1")
    val currentAtt1: String,
    @SerialName("current_att2")
    val currentAtt2: String,
    @SerialName("current_att_mid")
    val currentAttMid: String,
    @SerialName("current_semestr_balls")
    val currentSemestrBalls: String,
    @SerialName("current_semestr_result")
    val currentSemestrResult: String,
    @SerialName("last_semestr_balls")
    val lastSemestrBalls: String,
    @SerialName("last_semestr_result")
    val lastSemestrResult: String,
    @SerialName("project")
    val project: String,
    @SerialName("project_info")
    val projectInfo: String,
    @SerialName("project_theme")
    val projectTheme: String,
    @SerialName("raiting")
    val raiting: String,
    @SerialName("semestr")
    val semestr: String,
    @SerialName("subproject")
    val subproject: String,
    @SerialName("year")
    val year: String
)