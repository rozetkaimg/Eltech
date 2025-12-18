package com.rozetka.presentation.ui.groupJournal



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.Student
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.ui.settings.components.MonetItem
import com.rozetka.presentation.util.UiSize
import com.rozetka.presentation.util.getNavigationBarHeightDp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupJournalScreen(
    navController: NavController,
    viewModel: GroupJournalViewModel = koinViewModel()
) {
    val stateTop = rememberTopAppBarState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(stateTop)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Журнал группы",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())

        ) {
            when (val state = uiState) {
                is GroupJournalUiState.Loading -> LoadingState()

                is GroupJournalUiState.Error -> ErrorState(
                    message = state.message,
                    onUpdate = { viewModel.loadGroupJournal() }
                )

                is GroupJournalUiState.Success -> {
                    if (state.response.success) {
                        StudentList(
                            students = state.response.data.students,
                            totalCount = state.response.data.totalCount
                        )
                    } else {
                        ErrorState(
                            message = "Не удалось получить данные",
                            onUpdate = { viewModel.loadGroupJournal() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentList(
    students: List<Student>,
    totalCount: Int
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Всего студентов: $totalCount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(students, key = { it.studentGuid }) { student ->
            StudentCard(student = student)
        }
        item { Spacer(Modifier.height(UiSize().getNavBarPaddingSize())) }
    }
}

@Composable
private fun StudentCard(student: Student) {

    Column {
        Card(
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = student.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        }
    }

    Spacer(Modifier.size(2.dp))

    Card(
        shape = RoundedCornerShape( 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoColumn(label = "Посещения", value = student.visits.toString())
            InfoColumn(label = "Баллы", value = student.totalPoints.toString())
            InfoColumn(label = "LMS", value = student.lmsPoints.toString())
        }
    }
    Spacer(Modifier.size(2.dp))
        Card(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 22.dp, bottomEnd = 22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, ) {

                Icon(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.education_outline_28),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = translateHealthGroup(student.healthGroup),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
                if (student.hasDebt) {

                        Badge(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text("Долг", modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }


        }


}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


fun translateHealthGroup(group: String): String {
    return when(group) {
        "Basic" -> "Основная"
        "Preparatory" -> "Подготовительная"
        "SpecialA" -> "Спец. А"
        "SpecialB" -> "Спец. Б"
        "None" -> "Не указана"
        else -> group
    }
}

@Composable
private fun ErrorState(message: String, onUpdate: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onUpdate) {
            Text(stringResource(R.string.try_again))
        }
    }
}