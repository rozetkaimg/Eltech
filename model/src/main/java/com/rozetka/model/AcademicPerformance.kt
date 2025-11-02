package com.rozetka.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicPerformance(
    @SerialName("academicPerformance")
    val academicPerformance: List<AcademicPerformanceItem>
)