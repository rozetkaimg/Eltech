package com.rozetka.model

data class StudentProfile(
    val fullName: String?,
    val photoUrl: String?,
    val personalFileNumber: String?,
    val status: String?,
    val gender: String?,
    val birthDate: String?,
    val studentCode: String?,
    val faculty: String?,
    val course: String?,
    val group: String?,
    val specialty: String?,
    val specialization: String?,
    val program: String?,
    val standardStudyPeriod: String?,
    val actualStudyPeriod: String?,
    val educationForm: String?,
    val financingType: String?,
    val educationLevel: String?,
    val admissionYear: String?,
    val correctionInfo: String?,
    val orders: List<String>
)