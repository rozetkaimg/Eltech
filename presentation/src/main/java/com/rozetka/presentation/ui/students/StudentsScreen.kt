package com.rozetka.presentation.ui.students

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.rozetka.model.StudentR
import com.rozetka.model.Specialty
import com.rozetka.model.GroupInfo
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.generateColorFromHash
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    navController: NavController,
    viewModel: StudentsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val messageUiState by viewModel.messageUiState.collectAsStateWithLifecycle()

    var selectedStudent by remember { mutableStateOf<StudentR?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = {
                    Box(
                        Modifier
                            .height(64.dp)
                            .padding(end = 16.dp)
                            .semantics { isTraversalGroup = true }
                    ) {
                        SearchBar(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .semantics { traversalIndex = 0f },
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = searchQuery,
                                    onQueryChange = viewModel::onSearchQueryChange,
                                    onSearch = { viewModel.searchStudents() },
                                    expanded = false,
                                    onExpandedChange = {},
                                    placeholder = { Text("Поиск студентов") },
                                    leadingIcon = { Icon(Icons.Default.Search, null) }
                                )
                            },
                            expanded = false,
                            onExpandedChange = { },
                        ) {}
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            when (val state = uiState) {
                is StudentsUiState.Loading -> LoadingState()
                is StudentsUiState.Content -> {
                    StudentsList(
                        items = state.items,
                        isLoadingMore = state.isLoadingMore,
                        onLoadMore = viewModel::loadNextPage,
                        onItemClick = { selectedStudent = it }
                    )
                }
                is StudentsUiState.Error -> ExpressiveErrorState(
                    message = state.message,
                    onUpdate = { viewModel.searchStudents() }
                )
                is StudentsUiState.Empty -> EmptyState(state.query)
                is StudentsUiState.Initial -> InitialState()
            }
        }

        if (selectedStudent != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    selectedStudent = null
                    viewModel.resetMessageState()
                },
                sheetState = sheetState,

            ) {
                StudentDetailsBottomSheet(
                    student = selectedStudent!!,
                    messageUiState = messageUiState,
                    viewModel = viewModel,
                    onSendMessage = { message ->
                        viewModel.sendMessageToStudent(selectedStudent!!.id, message)
                    },
                    onNavigateToDialog = { dialogId ->
                        navController.navigate("dialo/${selectedStudent!!.fio}/$dialogId")
                        selectedStudent = null
                        viewModel.resetMessageState()
                    },
                    onScheduleClick = { group ->
                        navController.navigate(Screen.Schedule.route + "/$group")
                    }
                )
            }
        }
    }
}

@Composable
private fun StudentsList(
    items: List<StudentR>,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onItemClick: (StudentR) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            top = 8.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 90.dp
        )
    ) {
        item {
            Text(
                text = "Найдено студентов: ${items.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        itemsIndexed(items) { index, student ->
            if (index >= items.size - 1 && !isLoadingMore) {
                onLoadMore()
            }
            StudentItem(
                student = student,
                onClick = { onItemClick(student) }
            )
        }

        if (isLoadingMore) {
            item {
                Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StudentItem(
    student: StudentR,
    viewModel: StudentsViewModel = koinViewModel(),
    onClick: () -> Unit
) {
    val specialty = remember(student.group) {
        viewModel.getSpecialtyForGroup(student.group)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = student.avatar.ifEmpty { null },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(MaterialShapes.Cookie9Sided.toShape()),
                loading = { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(strokeWidth = 2.dp) } },
                error = {
                    Box(
                        Modifier.fillMaxSize().background(generateColorFromHash(student.fio).copy(0.15f)),
                        Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = generateColorFromHash(student.fio))
                    }
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = student.fio,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Column () {
                    if (student.group.isNotBlank()) {
                        Text(
                            text = specialty.fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = student.group,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )



                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StudentDetailsBottomSheet(
    student: StudentR,
    messageUiState: MessageUiState,
    viewModel: StudentsViewModel,
    onSendMessage: (String) -> Unit,
    onNavigateToDialog: (String) -> Unit,
    onScheduleClick: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val groupInfo = remember(student.group) {
        viewModel.getGroupInfo(student.group)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = student.avatar.ifEmpty { null },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(160.dp).clip(MaterialShapes.Cookie9Sided.toShape()),
            loading = { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() } },
            error = {
                Box(
                    Modifier.fillMaxSize().background(generateColorFromHash(student.fio).copy(0.15f)),
                    Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = generateColorFromHash(student.fio))
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(student.fio, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

        if (student.group.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                Text(student.group, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        if (groupInfo.specialty != Specialty.UNKNOWN) {
            StudentInfoRow(Icons.Default.School, "Направление", groupInfo.specialty.name)
            Spacer(modifier = Modifier.height(16.dp))
            StudentInfoRow(Icons.Default.Assignment, "Профиль", groupInfo.profile)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (student.faculty.isNotBlank()) {
            StudentInfoRow(Icons.Default.AccountBalance, "Подразделение", student.faculty)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (student.group.isNotBlank()) {
            Button(
                onClick = { onScheduleClick(student.group) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(Icons.Default.DateRange, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Расписание группы")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Написать сообщение", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(targetState = messageUiState, label = "message_state") { state ->
            when (state) {
                is MessageUiState.Idle, is MessageUiState.Error -> {
                    Column {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("Введите сообщение...") },
                            minLines = 3,
                            maxLines = 5,
                            isError = state is MessageUiState.Error
                        )
                        if (state is MessageUiState.Error) {
                            Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onSendMessage(messageText) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = messageText.isNotBlank(),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Отправить")
                        }
                    }
                }
                is MessageUiState.Loading -> Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) { CircularProgressIndicator() }
                is MessageUiState.Success -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Сообщение отправлено!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onNavigateToDialog(state.dialogId) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Перейти в диалог")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyState(query: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(120.dp).background(MaterialTheme.colorScheme.primary.copy(0.1f), MaterialShapes.Cookie6Sided.toShape()), Alignment.Center) {
                Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(0.5f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.empty_state_message, query), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InitialState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(120.dp).background(MaterialTheme.colorScheme.primary.copy(0.1f), MaterialShapes.Cookie6Sided.toShape()), Alignment.Center) {
                Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(0.5f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Введите ФИО или группу\nдля поиска студентов", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        }
    }
}