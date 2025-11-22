package com.rozetka.presentation.ui.employees

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.rozetka.model.campus.TeacherSmall
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.pay.LoadingState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    navController: NavController,
    viewModel: EmployeesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val teachersUiState by viewModel.teachersUiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.searchEmployees()
    }
    var selectedEmployee by remember { mutableStateOf<ItemX?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    Scaffold(
        topBar = {
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
                                    query = searchQuery,
                                    onQueryChange = viewModel::onSearchQueryChange,
                                    onSearch = { viewModel.searchEmployees() },
                                    expanded = false,
                                    onExpandedChange = {},
                                    placeholder = { Text("Поиск сотрудников") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null
                                        )
                                    }
                                )
                            },
                            expanded = false,
                            onExpandedChange = { },
                        ) {}
                    }
                },
                navigationIcon = {
                    Box(Modifier.height(64.dp)) {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back)
                            )
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal])
                    )
                }
            ) {
                EmployeeTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        text = { Text(text = tab.title) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            val displayedItems = remember(uiState, selectedTab, teachersUiState) {
                if (uiState is EmployeesUiState.Content) {
                    val allItems = (uiState as EmployeesUiState.Content).items
                    when (selectedTab) {
                        EmployeeTab.ALL -> allItems
                        EmployeeTab.TEACHERS -> allItems.filter { employee ->
                            viewModel.findTeacherByFio(employee.fio) != null
                        }
                    }
                } else {
                    emptyList()
                }
            }

            when (val state = uiState) {
                is EmployeesUiState.Loading -> LoadingState()
                is EmployeesUiState.Content -> {
                    if (selectedTab == EmployeeTab.TEACHERS && displayedItems.isEmpty() && state.items.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Преподаватели не найдены",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        EmployeesList(
                            items = displayedItems,
                            isLoadingMore = state.isLoadingMore,
                            onLoadMore = viewModel::loadNextPage,
                            onItemClick = { selectedEmployee = it },
                            viewModel = viewModel,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            navController = navController
                        )
                    }
                }
                is EmployeesUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = { viewModel.searchEmployees() }
                )
                is EmployeesUiState.Empty -> EmptyState(state.query)
                is EmployeesUiState.Initial -> InitialState()
            }
        }

        if (selectedEmployee != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedEmployee = null },
                sheetState = sheetState
            ) {
                EmployeeDetailsBottomSheet(
                    employee = selectedEmployee!!,
                    onScheduleClick = {
                        navController.navigate("teacherSchedule/${selectedEmployee!!.fio}")
                    }
                )
            }
        }
    }
}

@Composable
private fun EmployeesList(
    items: List<ItemX>,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onItemClick: (ItemX) -> Unit,
    viewModel: EmployeesViewModel,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        itemsIndexed(items) { index, employee ->
            if (index >= items.size - 1 && !isLoadingMore) {
                onLoadMore()
            }
           var teacherSmall =  viewModel.findTeacherByFio(employee.fio)
            EmployeeItem(
                employee = employee,
                onClick = {
                    if (teacherSmall  != null){
                        val route = Screen.TeacherRating.route + "/${teacherSmall.id}?fio=${employee.fio}&avatar=${employee.avatar}&division=${employee.division}&email=${employee.email}&id=${employee.id}&post=${employee.post}"
                        navController.navigate(route)

                    } else {

                    onItemClick(employee)} },
                teacherSmall = teacherSmall
            )
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmployeeItem(
    employee: ItemX,
    onClick: () -> Unit,
    teacherSmall: TeacherSmall?
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                SubcomposeAsyncImage(
                    model = employee.avatar,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(Cookie9Sided.toShape()),
                    loading = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LoadingIndicator()
                        }
                    },
                    error = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_profile),
                                contentDescription = stringResource(R.string.cd_profile_photo_placeholder),
                                modifier = Modifier.size(96.dp).padding(16.dp)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (teacherSmall != null) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = String.format("%.2f", teacherSmall.rating.value),
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.fio,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (employee.post.isNotBlank()) {
                    Text(
                        text = employee.post,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (employee.division.isNotBlank()) {
                    Text(
                        text = employee.division,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (teacherSmall != null) {
                    Text(
                        text = "${teacherSmall.rating.count} отзывов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmployeeDetailsBottomSheet(
    employee: ItemX,
    onScheduleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = employee.avatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(180.dp)
                .clip(Cookie9Sided.toShape()),
            loading = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            },
            error = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_profile),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = employee.fio,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (employee.post.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = employee.post,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (employee.division.isNotBlank()) {
                EmployeeInfoRow(
                    icon = Icons.Default.Info,
                    label = "Подразделение",
                    value = employee.division
                )
            }

            if (employee.email.isNotBlank()) {
                EmployeeInfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = employee.email
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onScheduleClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Расписание сотрудника")
        }
    }
}

@Composable
private fun EmployeeInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry_search_button))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyState(query: String) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialShapes.Cookie6Sided.toShape()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.empty_state_message, query),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

        }
    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InitialState() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialShapes.Cookie6Sided.toShape()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Введите ФИО или подразделение\nдля поиска сотрудников",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

        }
    }
}