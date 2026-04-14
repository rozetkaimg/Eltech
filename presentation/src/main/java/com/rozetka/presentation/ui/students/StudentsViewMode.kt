package com.rozetka.presentation.ui.students

import com.rozetka.model.GroupInfo
import com.rozetka.model.Specialty
import com.rozetka.model.StudentProfile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.domain.util.StringObject.ApiToken
import com.rozetka.model.Student
import com.rozetka.model.StudentR
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupRepository {

    private data class MaskRule(
        val pattern: Regex,
        val specialty: Specialty,
        val profile: String = ""
    )

    private val rules = listOf(
        // --- ИСКЛЮЧЕНИЯ (Отрабатывают первыми) ---
        MaskRule(Regex("^(201|321)-33[1-9]$"), Specialty.IBAS, "Обеспечение информационной безопасности распределенных информационных систем"),
        MaskRule(Regex("^101-262$"), Specialty.UTS, "Электронные системы управления"),

        // --- ОБЩИЕ ПРАВИЛА (По хвосту группы) ---
        MaskRule(Regex(".*-11\\d"), Specialty.NTTS, "Спортивные / Перспективные транспортные средства"),
        MaskRule(Regex(".*-12\\d"), Specialty.PTMK, "Проектирование технологических машин"),
        MaskRule(Regex(".*-13\\d"), Specialty.EM, "Энергоустановки для транспорта и малой энергетики"),
        MaskRule(Regex(".*-14\\d"), Specialty.PM, "Программирование и цифровые технологии в динамике и прочности"),
        MaskRule(Regex(".*-16\\d"), Specialty.ETTMK, "Инжиниринг и эксплуатация транспортных систем"),
        MaskRule(Regex(".*-17\\d"), Specialty.PMI, "Математическое и компьютерное моделирование"),
        MaskRule(Regex(".*-21\\d"), Specialty.UK, "Управление качеством на производстве"),
        MaskRule(Regex(".*-22\\d"), Specialty.MASH, "Машины и технологии обработки материалов"),
        MaskRule(Regex(".*-24\\d"), Specialty.THOM, "Разработка и дизайн изделий промышленного дизайна"),
        MaskRule(Regex(".*-25\\d"), Specialty.MR, "Мехатронные системы в автоматизированном производстве"),
        MaskRule(Regex(".*-26\\d"), Specialty.SM, "Метрологическое обеспечение производств"),
        MaskRule(Regex(".*-27\\d"), Specialty.INN, "Управление инновационной деятельностью"),
        MaskRule(Regex(".*-28\\d"), Specialty.MTM, "Перспективные материалы и технологии"),
        MaskRule(Regex(".*-29\\d"), Specialty.ATPP, "Роботы и робототехнические устройства"),

        // IT-направления
        MaskRule(Regex(".*-32\\d"), Specialty.IVT, "Инженерия программного обеспечения / Веб-технологии"),
        MaskRule(Regex(".*-(33|72)\\d"), Specialty.ISIT, "Информационные системы и технологии / Цифровая трансформация"),
        MaskRule(Regex(".*-35\\d"), Specialty.IB, "Безопасность компьютерных систем"),
        MaskRule(Regex(".*-36\\d"), Specialty.PI, "Корпоративные информационные системы / Большие и открытые данные"),

        // НОВЫЕ ГРУППЫ ИБАС: 371 и 372
        // Маска [1-2] означает, что на конце может быть 1 или 2
        MaskRule(Regex(".*-37[1-2]"), Specialty.IBAS, "Обеспечение информационной безопасности распределенных информационных систем"),

        // Энергетика, строительство, безопасность
        MaskRule(Regex(".*-41\\d"), Specialty.EE, "Электрооборудование и промышленная электроника"),
        MaskRule(Regex(".*-43\\d"), Specialty.TT, "Теплоэнергетические установки, системы и комплексы"),
        MaskRule(Regex(".*-44\\d"), Specialty.STR, "Промышленное и гражданское строительство"),
        MaskRule(Regex(".*-45\\d"), Specialty.SUZS, "Строительство высотных и большепролетных зданий и сооружений"),
        MaskRule(Regex(".*-47\\d"), Specialty.RAD, "Интеллектуальная радиоэлектроника и промышленный Интернет вещей"),
        MaskRule(Regex(".*-51\\d"), Specialty.TB, "Экологическая безопасность и охрана труда"),
        MaskRule(Regex(".*-52\\d"), Specialty.BIO, "Промышленная биотехнология и биоинженерия"),
        MaskRule(Regex(".*-53\\d"), Specialty.HTENMI, "Автоматизированное производство химических предприятий"),
        MaskRule(Regex(".*-54\\d"), Specialty.HKT, "Холодильная техника и технологии"),

        // Экономика, гуманитарные и творческие направления
        MaskRule(Regex(".*-61\\d"), Specialty.EK, "Экономика предприятий и организаций"),
        MaskRule(Regex(".*-62\\d"), Specialty.MEN, "Управление бизнес-процессами"),
        MaskRule(Regex(".*-63\\d"), Specialty.RSO, "Реклама и связи с общественностью в цифровых медиа"),
        MaskRule(Regex(".*-64\\d"), Specialty.UP, "Стратегическое управление человеческими ресурсами"),
        MaskRule(Regex(".*-75\\d"), Specialty.TPUP, "Дизайн и проектирование визуального контента"),
        MaskRule(Regex(".*-81\\d"), Specialty.GRAF, "Художник анимации и компьютерной графики / Художник-график"),
        MaskRule(Regex(".*-82\\d"), Specialty.DIZ, "Графический дизайн мультимедиа / Транспортный дизайн"),
        MaskRule(Regex(".*-01\\d"), Specialty.IZD, "Книгоиздательское дело"),
        MaskRule(Regex(".*-02\\d"), Specialty.ZHO, "Периодические издания и мультимедийная журналистика")
    )

    fun getInfoByGroup(groupNumber: String): GroupInfo {
        val matchedRule = rules.find { it.pattern.matches(groupNumber) }
        return if (matchedRule != null) {
            GroupInfo(groupNumber, matchedRule.specialty, matchedRule.profile)
        } else {
            GroupInfo(groupNumber, Specialty.UNKNOWN, "Профиль не найден")
        }
    }
}





sealed interface MessageUiState {
    object Idle : MessageUiState
    object Loading : MessageUiState
    data class Success(val dialogId: String) : MessageUiState
    data class Error(val message: String) : MessageUiState
}

sealed interface StudentsUiState {
    object Initial : StudentsUiState
    object Loading : StudentsUiState
    data class Empty(val query: String) : StudentsUiState
    data class Error(val message: String) : StudentsUiState
    data class Content(
        val items: List<StudentR>,
        val isLoadingMore: Boolean
    ) : StudentsUiState
}

class StudentsViewModel(
    private val repository: MospolytechMethods
) : ViewModel() {

    private val groupRepository = GroupRepository()

    fun getGroupInfo(groupNumber: String): GroupInfo {
        return groupRepository.getInfoByGroup(groupNumber)
    }
    private val _uiState = MutableStateFlow<StudentsUiState>(StudentsUiState.Initial)
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _messageUiState = MutableStateFlow<MessageUiState>(MessageUiState.Idle)
    val messageUiState: StateFlow<MessageUiState> = _messageUiState.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1
    private val perPage = 50
    private val currentItems = mutableListOf<StudentR>()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun searchStudents() {
        val query = _searchQuery.value.trim()
        if (query.isBlank()) return

        currentPage = 1
        currentItems.clear()
        _uiState.value = StudentsUiState.Loading
        _messageUiState.value = MessageUiState.Idle

        loadData(query)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state is StudentsUiState.Content && !state.isLoadingMore && currentPage < totalPages) {
            currentPage++
            loadData(_searchQuery.value, isNextPage = true)
        }
    }

    fun resetMessageState() {
        _messageUiState.value = MessageUiState.Idle
    }

    fun sendMessageToStudent(studentId: String, message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _messageUiState.value = MessageUiState.Loading
            try {
                val response = repository.sendMessageNoFilesByID(
                    iD = studentId,
                    token = ApiToken,
                    newMessage = message
                )

                if (response.id.isNotBlank()) {
                    _messageUiState.value = MessageUiState.Success(dialogId = response.id)
                } else {
                    _messageUiState.value = MessageUiState.Error("Не удалось получить ID диалога")
                }

            } catch (e: Exception) {
                _messageUiState.value = MessageUiState.Error(e.message ?: "Ошибка отправки")
            }
        }
    }

    fun getSpecialtyForGroup(groupNumber: String): Specialty {
        return groupRepository.getInfoByGroup(groupNumber).specialty
    }

    private fun loadData(query: String, isNextPage: Boolean = false) {
        viewModelScope.launch {
            if (isNextPage) {
                _uiState.value = (_uiState.value as? StudentsUiState.Content)?.copy(isLoadingMore = true)
                    ?: StudentsUiState.Loading
            }

            try {
                val isGroupSearch = query.any { it.isDigit() } || query.contains("-")

                val result = repository.getStudents(
                    token = ApiToken,
                    search = if (isGroupSearch) "" else query,
                    group = if (isGroupSearch) query else "",
                    page = currentPage,
                    perPage = perPage
                )

                totalPages = result.pages.toIntOrNull() ?: 1
                val newItems = result.items

                if (newItems.isEmpty() && !isNextPage) {
                    _uiState.value = StudentsUiState.Empty(query)
                } else {
                    currentItems.addAll(newItems)
                    _uiState.value = StudentsUiState.Content(
                        items = currentItems.toList(),
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                if (!isNextPage) {
                    _uiState.value = StudentsUiState.Error(e.message ?: "Ошибка поиска студентов")
                } else {
                    _uiState.value = StudentsUiState.Content(currentItems.toList(), isLoadingMore = false)
                }
            }
        }
    }
}
