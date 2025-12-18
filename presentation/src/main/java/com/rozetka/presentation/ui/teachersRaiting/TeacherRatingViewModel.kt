package com.rozetka.presentation.ui.teachersRaiting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject
import com.rozetka.model.campus.TeacherResponse
import com.rozetka.network.MospolytechMethods
import com.rozetka.network.campus.CampusApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeacherRatingViewModel(val campusApi: CampusApi) : ViewModel() {



    private val _uiState = MutableStateFlow<TeacherRatingUiState>(TeacherRatingUiState.Loading)
    val uiState: StateFlow<TeacherRatingUiState> = _uiState.asStateFlow()

    fun getTeacherRating(id: String) {
        viewModelScope.launch {
            _uiState.value = TeacherRatingUiState.Loading
            try {
                val token = StringObject.campusToken
                val teacherData = campusApi.getTeacher(bearerToken = token, id = id)
                _uiState.value = TeacherRatingUiState.Success(teacherData)
            } catch (e: Exception) {
                _uiState.value = TeacherRatingUiState.Error("error: ${e.message}")
            }
        }
    }
    fun setReaction(reviewId: String, reaction: String, teacherId: String) {
        viewModelScope.launch {
            try {
                val token = StringObject.campusToken
                campusApi.setReaction(bearerToken = token, reaction = reaction, id = reviewId)
                val updatedData = campusApi.getTeacher(bearerToken = token, id = teacherId)
                _uiState.value = TeacherRatingUiState.Success(updatedData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

sealed interface TeacherRatingUiState {
    data class Success(val data: TeacherResponse) : TeacherRatingUiState
    data class Error(val message: String) : TeacherRatingUiState
    object Loading : TeacherRatingUiState
}