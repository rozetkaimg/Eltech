package com.rozetka.presentation.ui.submitanApplication

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rozetka.presentation.util.generateColorFromHash

data class ServiceLink(
    val title: String,
    val route: String,
    val description: String? = null,
    val isExternal: Boolean = false
)

data class ServiceSection(
    val title: String,
    val links: List<ServiceLink>
)

fun getServiceData(): List<ServiceSection> {
    return listOf(
        ServiceSection(
            title = "Многофункциональный центр",
            links = listOf(
                ServiceLink("Справка о выплате стипендии и иные выплаты (из бухгалтерии)", "applications/accounting"),
                ServiceLink("Справка о прослушанных дисциплинах за период обучения (справка об обучении)", "applications/certificate-of-attendance"),
                ServiceLink("Справка о прохождении обучения в университете (о статусе обучающегося) по месту требования", "applications/student-status"),
                ServiceLink("Справка в социальные учреждения (Пенсионный фонд, УСЗН и пр.)", "applications/social-agencies"),
                ServiceLink("Справка-вызов", "applications/paper-call"),
                ServiceLink("Заявление на пересдачу для получения диплома с отличием", "applications/retake-for-diploma"),
                ServiceLink("Запрос на изменение персональных данных", "applications/changing-personal-data"),
                ServiceLink("Запрос на восстановление магнитного пропуска", "applications/restoring-the-magnetic-pass"),
                ServiceLink("Уточнение паспортных данных", "applications/clarification-of-passport-data"),
                ServiceLink("Выдача лицензий и свидетельств о государственной аккредитации", "applications/state-accreditation"),
                ServiceLink("Предоставление каникул в связи с окончанием университета", "applications/holidays-after-training"),
                ServiceLink("Предоставление отпуска", "applications/provision-academic-leave"),
                ServiceLink("Выход из отпуска", "applications/exit-academic-leave"),
                ServiceLink("Отчисление по инициативе обучающегося", "applications/independently-deducted"),
                ServiceLink("Продление промежуточной аттестации или ГИА", "applications/extension-attestation")
            )
        ),
        ServiceSection(
            title = "Управление студенческим городком",
            links = listOf(
                ServiceLink("Предоставление медицинских справок для проживающих в общежитии", "medical-certificate"),
                ServiceLink("Предоставление права проживания (очная форма)", "applications/regular-accommodation"),
                ServiceLink("Предоставление права проживания (очно-заочная форма)", "applications/full-time-part-time-form"),
                ServiceLink("Предоставление права проживания (заочная форма)", "applications/accommodation-correspondence-form"),
                ServiceLink("Предоставление или продление права проживания льготной категории граждан", "applications/preferential-accommodation"),
                ServiceLink("Предоставление права проживания в период академического отпуска", "applications/academic-leave-accommodation"),
                ServiceLink("Предоставление права проживания в семейной комнате", "applications/family-room"),
                ServiceLink("Переселение внутри общежития", "applications/relocation-inside-hostel"),
                ServiceLink("Переселение в другое общежитие", "applications/relocation-to-another-hostel"),
                ServiceLink("Расторжение договора найма", "applications/termination-of-employment-contract"),
                ServiceLink("Предоставление права проживания в период каникул", "applications/accommodation-for-graduates")
            )
        ),
        ServiceSection(
            title = "Профсоюзная организация",
            links = listOf(
                ServiceLink("Вступить в Профсоюз", "https://lk.eseur.ru/signup", isExternal = true),
                ServiceLink("Оформить материальную поддержку остронуждающимся студентам (Дотацию)", "applications/financial-support"),
                ServiceLink("Заявка на материальную помощь", "applications/financial-assistance"),
                ServiceLink("Оформить социальную стипендию", "applications/social-scollarship"),
                ServiceLink("Конкурс на назначение повышенной государственной академической стипендии", "applications/increased-state-academic-scholarship")
            )
        ),
        ServiceSection(
            title = "Управление мобилизационной подготовки",
            links = listOf(
                ServiceLink("Отправить документы воинского учета", "applications/military-registration-documents"),
                ServiceLink("Заполнить личную карточку обучающегося по воинскому учету для получения отсрочки от призыва на военную службу (форма 10)", "applications/military-registration"),
                ServiceLink("Заказать справку об обучении для студентов в военкомат (форма 4)", "applications/military-form-4", "Доступна после заполнения формы № 10"),
                ServiceLink("Заказать справку об обучении для аспирантов в военкомат (форма 5)", "applications/military-form-5", "Доступна после заполнения формы № 10"),
                ServiceLink("Заверенные копии документов по воинскому учету из личного дела", "applications/military-copies")
            )
        ),
        ServiceSection(
            title = "Приемная комиссия",
            links = listOf(
                ServiceLink("Изменение условий обучения (направление подготовки (специальность), форма), в том числе перевод с платного обучения на бесплатное", "https://mospolytech.ru/obuchauschimsya/izmenenie-uslovij-obucheniya-i-vosstanovlenie", isExternal = true)
            )
        ),
        ServiceSection(
            title = "Прочее",
            links = listOf(
                ServiceLink("Предоставление справок о группе здоровья", "applications/medical-certificates-086"),
                ServiceLink("Контактные данные родителей", "applications/family-contacts"),
                ServiceLink("Техническая эксплуатация", "applications/technical-maintenance"),
                ServiceLink("Произвольный запрос", "applications/arbitrary-request")
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitAnApplicationScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val allSections = remember { getServiceData() }

    val filteredSections = remember(searchQuery, allSections) {
        if (searchQuery.isBlank()) {
            allSections
        } else {
            allSections.mapNotNull { section ->
                val filteredLinks = section.links.filter { link ->
                    link.title.contains(searchQuery, ignoreCase = true)
                }
                if (filteredLinks.isNotEmpty()) {
                    section.copy(links = filteredLinks)
                } else {
                    null
                }
            }
        }
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = { }) {
                    Text("Понятно")
                }
            },
            title = { Text("В разработке") },
            text = { Text("Данный функционал находится в процессе разработки и будет доступен позже.") },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        "Новая заявка",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск заявок") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            items(filteredSections) { section ->
                ServiceSectionItem(
                    section = section,
                    onLinkClicked = {  }
                )
            }
            item {
                Spacer(modifier = Modifier.size(80.dp + 16.dp))
            }
        }
    }
}

@Composable
fun ServiceSectionItem(
    section: ServiceSection,
    onLinkClicked: (ServiceLink) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            section.links.forEach { link ->
                ServiceLinkItem(
                    link = link,
                    onClick = { onLinkClicked(link) },
                    selectionName = section.title
                )
            }
        }
    }
}

@Composable
fun ServiceLinkItem(
    link: ServiceLink,
    selectionName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(generateColorFromHash(selectionName).copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (link.isExternal) Icons.Default.Link else Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    tint = generateColorFromHash(selectionName)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (link.description != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = link.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}