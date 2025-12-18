package com.rozetka.presentation.ui.physEdJournal


import android.annotation.SuppressLint
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.FKStudentData
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.groupJournal.GroupJournalScreen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.UiSize
import com.rozetka.presentation.util.getNavigationBarHeightDp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PhysEdJournalScreen(
    navController: NavController,
    viewModel: PhysEdJournalViewModel = koinViewModel()
) {
    val stateTop = rememberTopAppBarState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(stateTop)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            when (val state = uiState) {
                is PhysEdJournalUiState.Error -> PhysEdJournalSmallTopAppBar(
                    scrollBehavior = scrollBehavior,
                    onBackClicked = { navController.navigateUp() }
                )

                PhysEdJournalUiState.Loading -> PhysEdJournalSmallTopAppBar(
                    scrollBehavior = scrollBehavior,
                    onBackClicked = { navController.navigateUp() }
                )

                is PhysEdJournalUiState.Success -> {
                    if (state.studentData.success)
                        PhysEdJournalTopAppBar(
                            scrollBehavior = scrollBehavior,
                            onBackClicked = { navController.navigateUp() },

                        ) else {
                        PhysEdJournalSmallTopAppBar(
                            scrollBehavior = scrollBehavior,
                            onBackClicked = { navController.navigateUp() }
                        )
                    }
                }
            }

        },

        ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                ),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is PhysEdJournalUiState.Loading -> LoadingState()
                is PhysEdJournalUiState.Success ->
                    if (state.studentData.success) {
                        state.studentData.data?.let { PhysEdJournalSuccessState(data = it, toGroup = {navController.navigate(Screen.PhysGroupJournalScreen.route)}) }
                    } else {
                        stateTop.heightOffset = -stateTop.heightOffset
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp, horizontal = 16.dp)
                                .align(Alignment.Center),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),

                            ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(64.dp)
                                        .clip(Cookie9Sided.toShape())
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.education_outline_28),
                                        contentDescription = stringResource(R.string.academic_year_content_description),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                                Spacer(Modifier.size(16.dp))
                                state.studentData.detail?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                }

                            }

                        }

                    }

                is PhysEdJournalUiState.Error -> ErrorState(
                    message = state.message,
                    onUpdate = { viewModel.loadStudentData() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PhysEdJournalSuccessState(data: FKStudentData, toGroup: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StudentInfoCard(
                fullName = data.fullName,
                group = data.groupNumber,
                course = data.course,
                totalPoints = data.totalPoints,
                lms = data.lmsPoints,
                curator = data.curator?.fullName ?: "",
                healthGroup = data.healthGroup,
                specialization = data.specialization,
            )
        }
        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                onClick = toGroup
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(Cookie9Sided.toShape())
                            .background(MaterialTheme.colorScheme.surface)
                    ) {

                        Icon(
                            painter = painterResource(R.drawable.users_outline),
                            contentDescription = "Student Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Журнал группы",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Рейтинг твоей группы",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        if (data.pointsHistory.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.points_history),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(data.pointsHistory) { pointItem ->
                PointHistoryItem(item = pointItem)
            }
        }

        if (data.visitsHistory.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.visits_history),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            items(data.visitsHistory) { visitItem ->
                VisitHistoryItem(item = visitItem)
            }
        }

        if (data.pointsHistory.isEmpty() && data.visitsHistory.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_data_to_display))
                }
            }
            item { Spacer(Modifier.height(UiSize().getNavBarPaddingSize())) }
        }
    }
}


@Composable
private fun ErrorState(message: String, onUpdate: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onUpdate) {
            Text(stringResource(R.string.try_again))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysEdJournalTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onBackClicked: () -> Unit,

    modifier: Modifier = Modifier
) {
    LargeTopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        title = { Text(stringResource(R.string.physical_education), fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysEdJournalSmallTopAppBar(

    scrollBehavior: TopAppBarScrollBehavior? = null,
    onBackClicked: () -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        title = { Text(stringResource(R.string.physical_education), fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}