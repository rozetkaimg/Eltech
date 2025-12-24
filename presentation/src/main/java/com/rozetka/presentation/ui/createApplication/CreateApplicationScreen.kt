package com.rozetka.presentation.ui.createApplication


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateApplicationScreen(
    applicationId: String,
    navController: NavController,
    viewModel: CreateApplicationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.addFile(it.path?.substringAfterLast("/") ?: "file") }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(getApplicationTitle(applicationId)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(p),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                when (applicationId) {
                    "arbitrary-request" -> ArbitraryFields(uiState, viewModel)
                    "student-status", "certificate-of-attendance", "accounting" -> CertificateFields(uiState, viewModel)
                    "financial-support" -> FinancialFields(uiState, viewModel)
                }
            }

            if (isAttachmentAllowed(applicationId)) {
                item {
                    AttachmentBlock(
                        files = uiState.attachedFiles,
                        onAttachClick = { filePickerLauncher.launch("*/*") },
                        onRemoveFile = { viewModel.removeFile(it) }
                    )
                }
            }

            item {
                SectionHeader("Дополнительно")
                OutlinedTextField(
                    value = uiState.comment,
                    onValueChange = { viewModel.updateComment(it) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text("Комментарий") },
                    minLines = 3
                )
            }

            item {
                Button(
                    onClick = { viewModel.submit(applicationId, "") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Отправить заявку")
                }
            }
        }
    }
}

@Composable
fun AttachmentBlock(
    files: List<String>,
    onAttachClick: () -> Unit,
    onRemoveFile: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader("Прикрепленные файлы")
        Button(
            onClick = onAttachClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.filledTonalButtonColors(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.AttachFile, null)
            Spacer(Modifier.width(8.dp))
            Text("Выбрать файл")
        }
        files.forEach { fileName ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(fileName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.clickable { onRemoveFile(fileName) }
                )
            }
        }
    }
}

@Composable
fun ArbitraryFields(s: CreateApplicationUiState, v: CreateApplicationViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Параметры запроса")
        OutlinedTextField(
            value = s.subject,
            onValueChange = { v.updateSubject(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Тематика") }
        )
        OutlinedTextField(
            value = s.body,
            onValueChange = { v.updateBody(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Суть") },
            minLines = 4
        )
    }
}

@Composable
fun CertificateFields(s: CreateApplicationUiState, v: CreateApplicationViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Способ получения")
        listOf("в электронном виде", "лично", "на почтовый адрес").forEach { method ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(s.deliveryMethod == method, { v.updateDelivery(method) })
                Text(method, Modifier.padding(start = 8.dp))
            }
        }
        OutlinedTextField(
            value = s.placeOfDemand,
            onValueChange = { v.updatePlace(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Место предъявления") }
        )
        OutlinedTextField(
            value = s.numCopies,
            onValueChange = { v.updateCopies(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Копии") }
        )
    }
}

@Composable
fun FinancialFields(s: CreateApplicationUiState, v: CreateApplicationViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Реквизиты")
        OutlinedTextField(
            value = s.bankAccount,
            onValueChange = { v.updateBankAccount(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Номер счета") }
        )
    }
}

@Composable
fun SectionHeader(t: String) {
    Text(t, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

fun isAttachmentAllowed(id: String): Boolean = when(id) {
    "arbitrary-request", "financial-support", "changing-personal-data" -> true
    else -> false
}

fun getApplicationTitle(id: String): String = when (id) {
    "accounting" -> "Бухгалтерия"
    "certificate-of-attendance" -> "Справка об обучении"
    "student-status" -> "Справка о статусе"
    "arbitrary-request" -> "Произвольный запрос"
    "financial-support" -> "Дотация"
    else -> "Заявка"
}