package com.rozetka.presentation.ui.groupJournal


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.Student
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.UiSize
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
                        text = stringResource(R.string.group_journal_title),
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

                is GroupJournalUiState.Error -> ExpressiveErrorState(
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
                            message = stringResource(R.string.group_journal_data_not_available),
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
                text = stringResource(R.string.group_journal_total_students, totalCount),
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

