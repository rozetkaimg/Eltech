package com.rozetka.presentation.ui.createApplication

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.CreateApplicationUiState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateApplicationScreen(
    applicationId: String,
    navController: NavController,
    viewModel: CreateApplicationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.addFile(it.path?.substringAfterLast("/") ?: "file") }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        getApplicationTitle(applicationId),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface

                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.submit(applicationId, "") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Отправить заявку", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ExpressiveSectionCard {
                    when (applicationId) {
                        "arbitrary-request" -> ArbitraryFields(uiState, viewModel)
                        "student-status", "certificate-of-attendance", "accounting" -> CertificateFields(
                            uiState,
                            viewModel
                        )

                        "financial-support" -> FinancialFields(uiState, viewModel)
                    }
                }
            }

            if (isAttachmentAllowed(applicationId)) {
                item {
                    AttachmentBlockExpressive(
                        files = uiState.attachedFiles,
                        onAttachClick = { filePickerLauncher.launch("*/*") },
                        onRemoveFile = { viewModel.removeFile(it) }
                    )
                }
            }

            item {
                ExpressiveSectionCard(title = "Дополнительно") {
                    OutlinedTextField(
                        value = uiState.comment,
                        onValueChange = { viewModel.updateComment(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Комментарий") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ExpressiveSectionCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun AttachmentBlockExpressive(
    files: List<String>,
    onAttachClick: () -> Unit,
    onRemoveFile: (String) -> Unit
) {
    ExpressiveSectionCard(title = "Прикрепленные файлы") {
        FilledTonalButton(
            onClick = onAttachClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AttachFile, null)
            Spacer(Modifier.width(8.dp))
            Text("Выбрать файл")
        }

        files.forEach { fileName ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    maxLines = 1
                )
                IconButton(onClick = { onRemoveFile(fileName) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ArbitraryFields(s: CreateApplicationUiState, v: CreateApplicationViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Параметры запроса",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = s.subject,
            onValueChange = { v.updateSubject(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Тематика") },
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = s.body,
            onValueChange = { v.updateBody(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Суть") },
            minLines = 4,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun CertificateFields(s: CreateApplicationUiState, v: CreateApplicationViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Способ получения",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        listOf("в электронном виде", "лично", "на почтовый адрес").forEach { method ->
            Surface(
                onClick = { v.updateDelivery(method) },
                shape = RoundedCornerShape(12.dp),
                color = if (s.deliveryMethod == method) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(s.deliveryMethod == method, null)
                    Text(method, Modifier.padding(start = 12.dp))
                }
            }
        }
        OutlinedTextField(
            value = s.placeOfDemand,
            onValueChange = { v.updatePlace(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Место предъявления") },
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = s.numCopies,
            onValueChange = { v.updateCopies(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Количество копий") },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun FinancialFields(s: CreateApplicationUiState, v: CreateApplicationViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Банковские реквизиты",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = s.bankAccount,
            onValueChange = { v.updateBankAccount(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Номер счета") },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

fun isAttachmentAllowed(id: String): Boolean = when(id) {
    "arbitrary-request", "financial-support", "changing-personal-data" -> true
    else -> false
}

fun getApplicationTitle(id: String): String = when (id) {
    "accounting" -> "Бухгалтерия"
    "certificate-of-attendance" -> "Справка об обучении"
    "student-status" -> "О прохождении обучения"
    "arbitrary-request" -> "Произвольный запрос"
    "financial-support" -> "Дотация"
    else -> "Новая заявка"
}