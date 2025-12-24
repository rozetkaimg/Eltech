package com.rozetka.model



data class CreateApplicationUiState(
    val phone: String,
    val email: String,
    val comment: String,
    val deliveryMethod: String,
    val mfcBranch: String,
    val selectedDepartmentId: String,
    val subject: String,
    val body: String,
    val majorCode: String,
    val majorName: String,
    val eduForm: String,
    val budgetBasis: String,
    val supportReasonType: String,
    val supportReasonText: String,
    val tradeUnionTicket: String,
    val paymentMethod: String,
    val bankAccount: String,
    val placeOfDemand: String,
    val numCopies: String,
    val attachedFiles: List<String>
)