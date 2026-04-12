package com.rozetka.presentation.ui.searchPeople

import android.net.Uri
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
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.rozetka.model.ItemX
import com.rozetka.model.StudentR
import com.rozetka.model.campus.TeacherSmall
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.employees.EmployeeTab
import com.rozetka.presentation.ui.employees.EmployeesUiState
import com.rozetka.presentation.ui.employees.EmployeesViewModel
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.ui.students.MessageUiState
import com.rozetka.presentation.ui.students.StudentsUiState
import com.rozetka.presentation.ui.students.StudentsViewModel
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.UiSize
import com.rozetka.presentation.util.generateColorFromHash
import org.koin.androidx.compose.koinViewModel

enum class SearchTab {
    STUDENTS,
    EMPLOYEES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedSearchScreen(
    navController: NavController,
    studentsViewModel: StudentsViewModel = koinViewModel(),
    employeesViewModel: EmployeesViewModel = koinViewModel()
) {
    var selectedTab by remember { mutableStateOf(SearchTab.STUDENTS) }
    val studentSearchQuery by studentsViewModel.searchQuery.collectAsStateWithLifecycle()
    val employeeSearchQuery by employeesViewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Box(
                            Modifier
                                .height(64.dp)
                                .padding(end = 32.dp)
                                .semantics { isTraversalGroup = true }
                        ) {
                            SearchBar(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .semantics { traversalIndex = 0f },
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = if (selectedTab == SearchTab.STUDENTS) studentSearchQuery else employeeSearchQuery,
                                        onQueryChange = {
                                            if (selectedTab == SearchTab.STUDENTS) studentsViewModel.onSearchQueryChange(it)
                                            else employeesViewModel.onSearchQueryChange(it)
                                        },
                                        onSearch = {
                                            if (selectedTab == SearchTab.STUDENTS) studentsViewModel.searchStudents()
                                            else employeesViewModel.searchEmployees()
                                        },
                                        expanded = false,
                                        onExpandedChange = {},
                                        placeholder = {
                                            Text(if (selectedTab == SearchTab.STUDENTS) stringResource(R.string.search_students_placeholder) else stringResource(R.string.search_employees_placeholder))
                                        },
                                        leadingIcon = {
                                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                                        }
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
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    SearchTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(text = when(tab) {
                                SearchTab.STUDENTS -> stringResource(R.string.tab_students)
                                SearchTab.EMPLOYEES -> stringResource(R.string.tab_employees)
                            }) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (selectedTab == SearchTab.STUDENTS) {
                StudentsSearchContent(studentsViewModel, navController)
            } else {
                EmployeesSearchContent(employeesViewModel, navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentsSearchContent(viewModel: StudentsViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageUiState by viewModel.messageUiState.collectAsStateWithLifecycle()
    var selectedStudent by remember { mutableStateOf<StudentR?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is StudentsUiState.Loading -> LoadingState()
            is StudentsUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    itemsIndexed(state.items) { index, student ->
                        if (index >= state.items.size - 1 && !state.isLoadingMore) {
                            viewModel.loadNextPage()
                        }
                        StudentItem(student = student, onClick = { selectedStudent = student })
                    }
                    if (state.isLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    item { Spacer(Modifier.height(UiSize().getNavBarPaddingSize())) }
                }
            }
            is StudentsUiState.Error -> ExpressiveErrorState(state.message, viewModel::searchStudents)
            is StudentsUiState.Empty -> EmptyState(state.query)
            is StudentsUiState.Initial -> InitialState(stringResource(R.string.initial_search_students))
        }
    }

    if (selectedStudent != null) {
        ModalBottomSheet(
            onDismissRequest = {
                selectedStudent = null
                viewModel.resetMessageState()
            }
        ) {
            StudentDetailsBottomSheet(
                student = selectedStudent!!,
                messageUiState = messageUiState,
                onSendMessage = { viewModel.sendMessageToStudent(selectedStudent!!.id, it) },
                onNavigateToDialog = { dialogId ->
                    val encodedAvatar = Uri.encode(selectedStudent!!.avatar)
                    navController.navigate("dialo/${selectedStudent!!.fio}/$dialogId?avatarUrl=$encodedAvatar&isSubject=false")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeesSearchContent(viewModel: EmployeesViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val teachersUiState by viewModel.teachersUiState.collectAsStateWithLifecycle()
    var selectedEmployee by remember { mutableStateOf<ItemX?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is EmployeesUiState.Loading -> LoadingState()
            is EmployeesUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    itemsIndexed(state.items) { index, employee ->
                        if (index >= state.items.size - 1 && !state.isLoadingMore) {
                            viewModel.loadNextPage()
                        }
                        val teacherSmall = viewModel.findTeacherByFio(employee.fio)
                        EmployeeItem(
                            employee = employee,
                            teacherSmall = teacherSmall,
                            onClick = {
                                if (teacherSmall != null) {
                                    val route = Screen.TeacherRating.route + "/${teacherSmall.id}?fio=${employee.fio}&avatar=${employee.avatar}&division=${employee.division}&email=${employee.email}&id=${employee.id}&post=${employee.post}"
                                    navController.navigate(route)
                                } else {
                                    selectedEmployee = employee
                                }
                            }
                        )
                    }
                    if (state.isLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    item { Spacer(Modifier.height(UiSize().getNavBarPaddingSize())) }
                }
            }
            is EmployeesUiState.Error -> ExpressiveErrorState(state.message, viewModel::searchEmployees)
            is EmployeesUiState.Empty -> EmptyState(state.query)
            is EmployeesUiState.Initial -> InitialState(stringResource(R.string.initial_search_employees))
        }
    }

    if (selectedEmployee != null) {
        ModalBottomSheet(onDismissRequest = { selectedEmployee = null }) {
            EmployeeDetailsBottomSheet(
                employee = selectedEmployee!!,
                onScheduleClick = {
                    navController.navigate("teacherSchedule/${selectedEmployee!!.fio}")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StudentItem(student: StudentR, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            SubcomposeAsyncImage(
                model = student.avatar.ifEmpty { null },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(Cookie9Sided.toShape()),
                error = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().background(generateColorFromHash(student.fio).copy(0.15f))
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = generateColorFromHash(student.fio))
                    }
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = student.fio, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (student.group.isNotBlank()) {
                    Text(text = student.group, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmployeeItem(employee: ItemX, teacherSmall: TeacherSmall?, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SubcomposeAsyncImage(
                    model = employee.avatar.ifEmpty { null },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp).clip(Cookie9Sided.toShape()),
                    error = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().background(generateColorFromHash(employee.fio).copy(0.15f))
                        ) {
                            Icon(painterResource(R.drawable.ic_profile), contentDescription = null, tint = generateColorFromHash(employee.fio), modifier = Modifier.size(32.dp))
                        }
                    }
                )
                if (teacherSmall != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text(text = String.format("%.2f", teacherSmall.rating.value), modifier = Modifier.padding(4.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = employee.fio, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (employee.post.isNotBlank()) {
                    Text(text = employee.post, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (employee.division.isNotBlank()) {
                    Text(text = employee.division, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    onSendMessage: (String) -> Unit,
    onNavigateToDialog: (String) -> Unit,
    onScheduleClick: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = student.avatar.ifEmpty { null },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(160.dp).clip(Cookie9Sided.toShape()),
            error = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(generateColorFromHash(student.fio).copy(0.15f))) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = generateColorFromHash(student.fio))
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = student.fio, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (student.group.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                Text(text = student.group, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))
        if (student.group.isNotBlank()) {
            Button(
                onClick = { onScheduleClick(student.group) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.group_schedule))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = stringResource(R.string.write_message), style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        when (val state = messageUiState) {
            is MessageUiState.Idle, is MessageUiState.Error -> {
                Column {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text(stringResource(R.string.enter_message_placeholder)) },
                        minLines = 3,
                        maxLines = 5,
                        isError = state is MessageUiState.Error
                    )
                    if (state is MessageUiState.Error) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onSendMessage(messageText) }, modifier = Modifier.fillMaxWidth(), enabled = messageText.isNotBlank(), contentPadding = PaddingValues(vertical = 16.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.send_button))
                    }
                }
            }
            is MessageUiState.Loading -> Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is MessageUiState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.message_sent), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onNavigateToDialog(state.dialogId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.go_to_dialog))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmployeeDetailsBottomSheet(employee: ItemX, onScheduleClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = employee.avatar.ifEmpty { null },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(180.dp).clip(Cookie9Sided.toShape()),
            error = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(generateColorFromHash(employee.fio).copy(0.15f))) {
                    Icon(painterResource(R.drawable.ic_profile), contentDescription = null, modifier = Modifier.size(64.dp), tint = generateColorFromHash(employee.fio))
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = employee.fio, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (employee.post.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = employee.post, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))
        if (employee.division.isNotBlank()) {
            EmployeeInfoRow(icon = Icons.Default.Info, label = stringResource(R.string.label_division), value = employee.division)
        }
        if (employee.email.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            EmployeeInfoRow(icon = Icons.Default.Email, label = stringResource(R.string.email_label), value = employee.email)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onScheduleClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 16.dp)) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.employee_schedule))
        }
    }
}

@Composable
private fun EmployeeInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyState(query: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier.size(120.dp).background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = MaterialShapes.Cookie6Sided.toShape()),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(R.string.empty_state_message, query), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InitialState(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier.size(120.dp).background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = MaterialShapes.Cookie6Sided.toShape()),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = text, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        }
    }
}
