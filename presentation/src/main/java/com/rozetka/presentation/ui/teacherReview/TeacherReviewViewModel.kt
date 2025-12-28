package com.rozetka.presentation.ui.teacherReview


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject
import com.rozetka.model.campus.ReviewCriteriaValue
import com.rozetka.model.campus.ReviewOptions
import com.rozetka.model.campus.TeacherReviewRequest
import com.rozetka.network.campus.CampusApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherReviewViewModel(val campusApi: CampusApi) : ViewModel() {

    private val _uiState = MutableStateFlow<ReviewFieldsUiState>(ReviewFieldsUiState.Loading)
    val uiState: StateFlow<ReviewFieldsUiState> = _uiState.asStateFlow()

    private val _submissionState = MutableStateFlow<ReviewSubmissionState>(ReviewSubmissionState.Idle)
    val submissionState: StateFlow<ReviewSubmissionState> = _submissionState.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment = _comment.asStateFlow()

    private val _selectedRatings = MutableStateFlow<Map<String, Int>>(emptyMap())
    val selectedRatings = _selectedRatings.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags = _selectedTags.asStateFlow()

    fun loadFields(teacherId: String) {
        viewModelScope.launch {
            _uiState.value = ReviewFieldsUiState.Loading
            try {
                val token = StringObject.campusToken
                val fields = campusApi.getTeacherRatingFields(bearerToken = token, teacherId = teacherId)

                val initialRatings = fields.criteria.associate { it.id to 5 }
                _selectedRatings.value = initialRatings

                _uiState.value = ReviewFieldsUiState.Success(fields)
            } catch (e: Exception) {
                _uiState.value = ReviewFieldsUiState.Error("error: ${e.message}")
            }
        }
    }

    fun updateRating(criteriaId: String, value: Int) {
        _selectedRatings.value = _selectedRatings.value.toMutableMap().apply {
            put(criteriaId, value)
        }
    }

    fun toggleTag(tagId: String) {
        val currentTags = _selectedTags.value.toMutableSet()
        if (currentTags.contains(tagId)) {
            currentTags.remove(tagId)
        } else {
            currentTags.add(tagId)
        }
        _selectedTags.value = currentTags
    }

    fun updateComment(text: String) {
        _comment.value = text
    }

    fun submitReview(teacherId: String) {
        viewModelScope.launch {
            _submissionState.value = ReviewSubmissionState.Loading
            try {
                val token = StringObject.campusToken

                val request = TeacherReviewRequest(
                    content = _comment.value,
                    criteria = _selectedRatings.value.map { (id, value) ->
                        ReviewCriteriaValue(id = id, value = value)
                    },
                    tags = _selectedTags.value.toList()
                )

                campusApi.sendTeacherReview(bearerToken = token, teacherId = teacherId, reviewBody = request)
                _submissionState.value = ReviewSubmissionState.Success
            } catch (e: Exception) {
                _submissionState.value = ReviewSubmissionState.Error("error: ${e.message}")
            }
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = ReviewSubmissionState.Idle
    }
}

sealed interface ReviewFieldsUiState {
    object Loading : ReviewFieldsUiState
    data class Success(val data: ReviewOptions) : ReviewFieldsUiState
    data class Error(val message: String) : ReviewFieldsUiState
}

sealed interface ReviewSubmissionState {
    object Idle : ReviewSubmissionState
    object Loading : ReviewSubmissionState
    object Success : ReviewSubmissionState
    data class Error(val message: String) : ReviewSubmissionState
}