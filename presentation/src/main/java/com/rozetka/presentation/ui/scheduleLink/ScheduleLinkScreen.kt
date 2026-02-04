package com.rozetka.presentation.ui.scheduleLink

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.ui.shedule.DaySchedule
import com.rozetka.presentation.ui.shedule.MissingScheduleView
import com.rozetka.presentation.ui.shedule.ScheduleScreenData
import com.rozetka.presentation.ui.shedule.ScheduleUiState
import com.rozetka.presentation.ui.shedule.WeekScheduleContent
import com.rozetka.presentation.util.ExpressiveErrorState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun ScheduleLinkScreen(
    navController: NavHostController,
    groupName: String,
    viewModel: ScheduleLinkViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.getSchedule(groupName)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, title = {
                Text(
                    text = stringResource(R.string.schedule_for_group, groupName),
                    fontWeight = FontWeight.Bold
                )
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
            )
        }, containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            when (val state = uiState) {
                is ScheduleUiState.Loading -> {
                    LoadingState()
                }

                is ScheduleUiState.Error -> MissingScheduleView(groupName, {
                    navController.navigate("SessionSchedule/${groupName}")
                })

                is ScheduleUiState.Success -> {
                    if (viewModel.getScheduleState()) {
                        WeekSchedule(state.data, paddingValues, navController)
                    } else {
                        DaySchedule(state.data, paddingValues) {
                            it
                            navController.navigate("teacherSchedule/${it}")
                        }

                    }
                }

                is ScheduleUiState.Initial -> {}
            }
        }
    }
}

@Composable
fun WeekSchedule(
    state: ScheduleScreenData, paddingValues: PaddingValues, navController: NavController
) {
    val screenData = state
    val pagerState = rememberPagerState(
        initialPage = screenData.initialWeekIndex, pageCount = { screenData.weeks.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())
    ) {
        SecondaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface

        ) {
            screenData.weeks.forEachIndexed { index, weekInfo ->
                Tab(selected = pagerState.currentPage == index, onClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                }, text = { Text(text = weekInfo.label) })
            }
        }

        HorizontalPager(
            state = pagerState, modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val selectedWeek = screenData.weeks[pageIndex]
            WeekScheduleContent(
                schedule = screenData.fullSchedule,
                week = selectedWeek,
                navController = navController
            )
        }
    }

}
