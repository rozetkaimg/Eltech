package com.rozetka.presentation.ui.home

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.domain.UserDataHolder
import com.rozetka.model.ExternalNewsItem
import com.rozetka.model.NewsModelItem
import com.rozetka.model.NotificationModelItem
import com.rozetka.model.PolytechEvent
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.generateColorFromHash
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })

    var selectedNewsItem by remember { mutableStateOf<NewsModelItem?>(null) }
    var selectedEventItem by remember { mutableStateOf<PolytechEvent?>(null) }
    var selectedExternalItem by remember { mutableStateOf<ExternalNewsItem?>(null) }
    var selectedNotification by remember { mutableStateOf<NotificationModelItem?>(null) }
    var showNotificationsSheet by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    Scaffold(
        topBar = {
            Surface(tonalElevation = 1.dp) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopAppBar(
                        modifier = Modifier.widthIn(max = 1200.dp),
                        title = {
                            Text(
                                text = if (uiState is HomeUiState.Success)
                                    stringResource(R.string.greeting_user, UserDataHolder().getUserName())
                                else stringResource(R.string.home_title),
                                fontWeight = FontWeight.Black,
                                fontSize = if (isTablet) 26.sp else 22.sp
                            )
                        },
                        actions = {
                            IconButton(onClick = {
                                if (uiState is HomeUiState.Success) {
                                    showNotificationsSheet = true
                                }
                            }) {
                                Icon(
                                    painterResource(R.drawable.notifications_28),
                                    contentDescription = null,
                                )
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Card(
                            modifier = Modifier,
                            shape = RoundedCornerShape(30.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            val tabs = listOf(
                                Triple("Сайт", Icons.Default.Public, 0),
                                Triple("События", ImageVector.vectorResource(R.drawable.calendar_outline_24), 1),
                                Triple("Новости", ImageVector.vectorResource(R.drawable.newsfeed), 2),
                                Triple("Дедлайны", Icons.Rounded.Schedule, 3),
                            )

                            LazyRow(
                                modifier = Modifier.padding(4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(tabs.size) { index ->
                                    val (title, icon, page) = tabs[index]
                                    val isSelected = pagerState.currentPage == page

                                    Surface(
                                        onClick = { scope.launch { pagerState.animateScrollToPage(page) } },
                                        shape = RoundedCornerShape(24.dp),
                                        color = if (isSelected) generateColorFromHash(title).copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent,
                                        modifier = Modifier.height(44.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = if (isTablet) 24.dp else 16.dp),
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
                                                    tint = generateColorFromHash(title)
                                                )
                                            }
                                            if (isSelected) Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = title,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = if (isTablet) 16.sp else 14.sp,
                                                color = if (isSelected) generateColorFromHash(title) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> LoadingState()
                is HomeUiState.Error -> ExpressiveErrorState(state.message, viewModel::loadHomeData)
                is HomeUiState.Success -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 1400.dp),
                        verticalAlignment = Alignment.Top,
                        pageSpacing = 16.dp
                    ) { pageIndex ->
                        HomeGridContent(
                            state = state,
                            pageIndex = pageIndex,
                            onNewsClick = { selectedNewsItem = it },
                            onEventClick = { selectedEventItem = it },
                            onExternalClick = { selectedExternalItem = it },
                            navController = navController,
                            onLoadMore = {
                                when (pageIndex) {
                                    0 -> viewModel.loadMoreExternalNews()
                                    2 -> viewModel.loadMoreEvents()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showNotificationsSheet || selectedNewsItem != null || selectedEventItem != null || selectedExternalItem != null || selectedNotification != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showNotificationsSheet = false
                selectedNewsItem = null
                selectedEventItem = null
                selectedExternalItem = null
                selectedNotification = null
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.widthIn(max = 800.dp)) {
                    if (showNotificationsSheet && uiState is HomeUiState.Success) {
                        NotificationsListSheet(
                            notifications = (uiState as HomeUiState.Success).notifications,
                            onNotificationClick = {
                                selectedNotification = it
                                showNotificationsSheet = false
                            }
                        )
                    }
                    selectedExternalItem?.let { ExternalNewsDetailSheet(it) }
                    selectedNewsItem?.let { NewsDetailSheet(it) }
                    selectedEventItem?.let { EventDetailSheet(it) }
                    selectedNotification?.let { NotificationDetailSheet(it) }
                }
            }
        }
    }
}
