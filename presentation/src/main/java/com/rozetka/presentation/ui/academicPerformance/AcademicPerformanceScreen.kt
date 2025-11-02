package com.rozetka.presentation.ui.academicPerformance

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.AcademicPerformanceItem
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ThemeObject.BottomNavBarPaddingValue
import com.rozetka.presentation.util.getNavigationBarHeightDp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AcademicPerformanceScreen(
    navController: NavController,
    viewModel: AcademicPerformanceViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadAcademicPerformance()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.session_results)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when (val state = uiState) {
                is AcademicPerformanceUiState.Loading -> LoadingState()
                is AcademicPerformanceUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val errorMessage = state.message ?: stringResource(R.string.unknown_performance_load_error)
                        Text(
                            text = errorMessage,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is AcademicPerformanceUiState.Success -> {
                    val dataBySemester = state.data.groupBy { it.semestr }
                    val semesters = dataBySemester.keys.sortedBy { it.toIntOrNull() ?: 0 }

                    if (semesters.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.academic_data_missing))
                        }
                        return@Column
                    }

                    val pagerState = rememberPagerState(pageCount = { semesters.size })
                    val coroutineScope = rememberCoroutineScope()

                    SecondaryScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.fillMaxWidth(),
                        edgePadding = 0.dp,
                        divider = { HorizontalDivider() }
                    ) {
                        semesters.forEachIndexed { index, semester ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                },
                                text = { Text(text = stringResource(R.string.semester_prefix, semester)) },
                                unselectedContentColor = Color.Gray
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->
                        val semesterData = dataBySemester[semesters[page]]
                        var selectedItem by remember { mutableStateOf<AcademicPerformanceItem?>(null) }

                        if (semesterData.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.no_results_for_semester, semesters[page]),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                item { Spacer(Modifier.height(16.dp)) }
                                items(
                                    items = semesterData,
                                    key = { item -> item.id }
                                ) { item ->
                                    AcademicPerformanceCard(
                                        item = item,
                                        onClick = { selectedItem = item }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                                item { Spacer(Modifier.height(getNavigationBarHeightDp() + BottomNavBarPaddingValue.dp)) }
                            }
                        }

                        selectedItem?.let { item ->
                            DetailsDialogAcademicPerformance(item = item, onDismiss = { selectedItem = null })
                        }
                    }
                }
            }
        }
    }
}



