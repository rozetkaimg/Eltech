package com.rozetka.presentation.ui.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject
import com.rozetka.domain.util.StringObject.ApiToken
import com.rozetka.model.ItemX
import com.rozetka.model.campus.TeacherSmall
import com.rozetka.model.campus.UniversityData
import com.rozetka.network.MospolytechMethods
import com.rozetka.network.campus.CampusApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class EmployeeTab(val title: String) {
    ALL("Все"),
    TEACHERS("Преподаватели")
}

class EmployeesViewModel(
    private val repository: MospolytechMethods,
    private val campusApi: CampusApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmployeesUiState>(EmployeesUiState.Initial)
    val uiState: StateFlow<EmployeesUiState> = _uiState.asStateFlow()

    private val _teachersUiState = MutableStateFlow<TeachersCampusUiState>(TeachersCampusUiState.Loading)
    val teachersUiState: StateFlow<TeachersCampusUiState> = _teachersUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(EmployeeTab.ALL)
    val selectedTab: StateFlow<EmployeeTab> = _selectedTab.asStateFlow()

    init {
        loadTeachersData()
    }

    private var currentPage = 1
    private var totalPages = 1
    private val perPage = 50
    private val currentItems = mutableListOf<ItemX>()

    fun onTabSelected(tab: EmployeeTab) {
        _selectedTab.value = tab
    }

    fun loadTeachersData() {
        if (_teachersUiState.value is TeachersCampusUiState.Success) {
            return
        }

        viewModelScope.launch {
            _teachersUiState.value = TeachersCampusUiState.Loading
            try {
                val token = StringObject.campusToken
                val universityData = campusApi.getTeachers(bearerToken = token)
                _teachersUiState.value = TeachersCampusUiState.Success(universityData)
            } catch (e: Exception) {
                _teachersUiState.value = TeachersCampusUiState.Error("error: ${e.message}")
            }
        }
    }

    fun findTeacherByFio(fio: String): TeacherSmall? {
        val currentState = _teachersUiState.value
        return if (currentState is TeachersCampusUiState.Success) {
            currentState.data.teachers.find {
                it.name.contains(fio, ignoreCase = true)
            }
        } else {
            null
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
init {

}
    fun searchEmployees() {
        val query = _searchQuery.value.trim()

        currentPage = 1
        currentItems.clear()
        _uiState.value = EmployeesUiState.Loading

        loadData(query)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state is EmployeesUiState.Content && !state.isLoadingMore && currentPage < totalPages) {
            currentPage++
            loadData(_searchQuery.value, isNextPage = true)
        }
    }

    private fun loadData(query: String, isNextPage: Boolean = false) {
        viewModelScope.launch {
            if (isNextPage) {
                _uiState.value = (_uiState.value as? EmployeesUiState.Content)?.copy(isLoadingMore = true)
                    ?: EmployeesUiState.Loading
            }

            try {
                val result = repository.getStaff(
                    token = ApiToken,
                    division = query,
                    page = currentPage,
                    perpage = perPage
                )

                totalPages = result.pages.toIntOrNull() ?: 1
                val newItems = result.items

                if (newItems.isEmpty() && !isNextPage) {
                    _uiState.value = EmployeesUiState.Empty(query)
                } else {
                    currentItems.addAll(newItems)
                    _uiState.value = EmployeesUiState.Content(
                        items = currentItems.toList(),
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                if (!isNextPage) {
                    _uiState.value = EmployeesUiState.Error(e.message ?: "Ошибка поиска сотрудников")
                } else {
                    _uiState.value = EmployeesUiState.Content(currentItems.toList(), isLoadingMore = false)
                }
            }
        }
    }
}

sealed interface EmployeesUiState {
    object Initial : EmployeesUiState
    object Loading : EmployeesUiState
    data class Empty(val query: String) : EmployeesUiState
    data class Error(val message: String) : EmployeesUiState

    data class Content(
        val items: List<ItemX>,
        val isLoadingMore: Boolean
    ) : EmployeesUiState
}

sealed interface TeachersCampusUiState {
    data class Success(val data: UniversityData) : TeachersCampusUiState
    data class Error(val message: String) : TeachersCampusUiState
    object Loading : TeachersCampusUiState
}