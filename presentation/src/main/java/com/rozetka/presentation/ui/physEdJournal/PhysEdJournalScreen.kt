package com.rozetka.presentation.ui.physEdJournal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.FKStudentData
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.generateColorFromHash
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysEdJournalScreen(
    navController: NavController,
    viewModel: PhysEdJournalViewModel = koinViewModel()
) {
    val stateTop = rememberTopAppBarState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(stateTop)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            when (val state = uiState) {
                is PhysEdJournalUiState.Error, PhysEdJournalUiState.Loading -> {
                    PhysEdJournalSmallTopAppBar(
                        scrollBehavior = scrollBehavior,
                        onBackClicked = { navController.navigateUp() }
                    )
                }
                is PhysEdJournalUiState.Success -> {
                    if (state.studentData.success) {
                        PhysEdJournalTopAppBar(
                            scrollBehavior = scrollBehavior,
                            onBackClicked = { navController.navigateUp() },
                        )
                    } else {
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is PhysEdJournalUiState.Loading -> LoadingState()
                is PhysEdJournalUiState.Success -> {
                    if (state.studentData.success) {
                        state.studentData.data?.let {
                            PhysEdJournalSuccessState(
                                data = it,
                                toGroup = { navController.navigate(Screen.PhysGroupJournalScreen.route) },
                                scrollBehavior = scrollBehavior
                            )
                        }
                    } else {
                        EmptyStudentCard(state.studentData.detail)
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

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
private fun PhysEdJournalSuccessState(
    data: FKStudentData,
    toGroup: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val scope = rememberCoroutineScope()
    val tabs = listOf(
        Triple("Посещения", Icons.Default.CheckCircle, 0),
        Triple("Нормативы", Icons.Default.List, 1),
        Triple("Доп. Баллы", Icons.Default.Star, 2),
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    onClick = toGroup
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(72.dp)
                                .clip(Cookie9Sided.toShape())
                                .background(generateColorFromHash("Журнал группы").copy(0.15f))
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.users_outline),
                                contentDescription = null,
                                tint = generateColorFromHash("Журнал группы"),
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.Center)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Журнал группы",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Рейтинг твоей группы",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {

            TabsRow(tabs = tabs, pagerState = pagerState, scope = scope)
        }


        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillParentMaxHeight(),
                beyondViewportPageCount = 1,
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (pageIndex) {
                        0 -> historyItems(data.visitsHistory) { VisitHistoryItem(it) }
                        1 -> historyItems(data.standardsHistory) { NormativeItem(it) }
                        2 -> historyItems(data.pointsHistory) { PointHistoryItem(it) }
                    }
                }
            }
        }
    }
}

private fun <T> androidx.compose.foundation.lazy.LazyListScope.historyItems(
    items: List<T>,
    itemContent: @Composable (T) -> Unit
) {
    if (items.isEmpty()) {
        item {
            Box(
                modifier = Modifier.fillParentMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_data_to_display),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        items(items) { item ->
            itemContent(item)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabsRow(
    tabs: List<Triple<String, androidx.compose.ui.graphics.vector.ImageVector, Int>>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(tabs.size) { index ->
                val (title, icon, page) = tabs[index]
                val isSelected = pagerState.currentPage == page
                val themeColor = generateColorFromHash(title)

                Surface(
                    onClick = { scope.launch { pagerState.animateScrollToPage(page) } },
                    shape = RoundedCornerShape(24.dp),
                    color = if (isSelected) themeColor.copy(alpha = 0.15f) else Color.Transparent,
                    modifier = Modifier.height(44.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = themeColor
                            )
                        }
                        if (isSelected) Spacer(Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyStudentCard(detail: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    painter = painterResource(R.drawable.education_outline_28),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(Modifier.size(16.dp))
            detail?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onUpdate: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onUpdate) { Text(stringResource(R.string.try_again)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysEdJournalTopAppBar(scrollBehavior: TopAppBarScrollBehavior, onBackClicked: () -> Unit) {
    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        title = { Text(stringResource(R.string.physical_education), fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysEdJournalSmallTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior?,
    onBackClicked: () -> Unit
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = { Text(stringResource(R.string.physical_education), fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        }
    )
}