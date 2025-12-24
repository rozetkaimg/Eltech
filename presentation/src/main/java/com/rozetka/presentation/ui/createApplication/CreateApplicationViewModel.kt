package com.rozetka.presentation.ui.createApplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.model.CreateApplicationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.rozetka.network.MospolytechMethods

class CreateApplicationViewModel(
    private val repository: MospolytechMethods
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreateApplicationUiState(
            phone = "",
            email = "",
            comment = "",
            deliveryMethod = "в электронном виде",
            mfcBranch = "Отделение «На Прянишникова»",
            selectedDepartmentId = "",
            subject = "",
            body = "",
            majorCode = "",
            majorName = "",
            eduForm = "",
            budgetBasis = "",
            supportReasonType = "",
            supportReasonText = "",
            tradeUnionTicket = "",
            paymentMethod = "на карту",
            bankAccount = "",
            placeOfDemand = "По месту требования",
            numCopies = "1",
            attachedFiles = emptyList()
        )
    )
    val uiState = _uiState.asStateFlow()

    fun updateComment(v: String) = _uiState.update { it.copy(comment = v) }
    fun updateDepartment(v: String) = _uiState.update { it.copy(selectedDepartmentId = v) }
    fun updateSubject(v: String) = _uiState.update { it.copy(subject = v) }
    fun updateBody(v: String) = _uiState.update { it.copy(body = v) }
    fun updateDelivery(v: String) = _uiState.update { it.copy(deliveryMethod = v) }
    fun updatePlace(v: String) = _uiState.update { it.copy(placeOfDemand = v) }
    fun updateCopies(v: String) = _uiState.update { it.copy(numCopies = v) }
    fun updateBankAccount(v: String) = _uiState.update { it.copy(bankAccount = v) }

    fun addFile(name: String) = _uiState.update { it.copy(attachedFiles = it.attachedFiles + name) }
    fun removeFile(name: String) = _uiState.update { it.copy(attachedFiles = it.attachedFiles - name) }

    fun submit(routeId: String, token: String) {
        val state = _uiState.value
        val technicalId = when (routeId) {
            "arbitrary-request" -> "free_request"
            "student-status" -> "status_regular"
            "certificate-of-attendance" -> "obuch"
            "financial-support" -> "pr_donate"
            else -> routeId
        }

        val params = mutableMapOf(
            "phone" to state.phone,
            "email" to state.email,
            "comment" to state.comment
        )

        when (technicalId) {
            "free_request" -> {
                params["structural-subdivision"] = state.selectedDepartmentId
                params["subject_appeal"] = state.subject
                params["essence"] = state.body
            }
            "status_regular", "obuch" -> {
                params["method_obtaining"] = state.deliveryMethod
                params["place_reference"] = state.placeOfDemand
                params["number_copies"] = state.numCopies
            }
            "pr_donate" -> {
                params["debit_card"] = state.bankAccount
                params["payment_method"] = state.paymentMethod
            }
        }

        viewModelScope.launch {
            try {
                repository.sendApplicationData(technicalId, token, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}