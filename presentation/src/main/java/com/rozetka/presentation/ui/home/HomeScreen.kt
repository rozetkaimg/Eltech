package com.rozetka.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rozetka.domain.UserDataHolder
import com.rozetka.model.ExternalNewsItem
import com.rozetka.model.NewsModelItem
import com.rozetka.model.PolytechEvent
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.UiSize
import com.rozetka.presentation.util.generateColorFromHash
import com.rozetka.presentation.util.getNavigationBarHeightDp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    var selectedNewsItem by remember { mutableStateOf<NewsModelItem?>(null) }
    var selectedEventItem by remember { mutableStateOf<PolytechEvent?>(null) }
    var selectedExternalItem by remember { mutableStateOf<ExternalNewsItem?>(null) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
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
                            IconButton(onClick = {}) {
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
                is HomeUiState.Error -> ErrorState(state.message) { viewModel.loadHomeData() }
                is HomeUiState.Success -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().widthIn(max = 1400.dp),
                        verticalAlignment = Alignment.Top,
                        pageSpacing = 16.dp
                    ) { pageIndex ->
                        HomeGridContent(
                            state = state,
                            pageIndex = pageIndex,
                            onNewsClick = { selectedNewsItem = it; scope.launch { sheetState.show() } },
                            onEventClick = { selectedEventItem = it; scope.launch { sheetState.show() } },
                            onExternalClick = { selectedExternalItem = it; scope.launch { sheetState.show() } },
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

    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    selectedNewsItem = null
                    selectedEventItem = null
                    selectedExternalItem = null
                }
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },

        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.widthIn(max = 800.dp)) {
                    selectedExternalItem?.let { ExternalNewsDetailSheet(it) }
                    selectedNewsItem?.let { NewsDetailSheet(it) }
                    selectedEventItem?.let { EventDetailSheet(it) }
                }
            }
        }
    }
}

@Composable
private fun HomeGridContent(
    state: HomeUiState.Success,
    pageIndex: Int,
    onNewsClick: (NewsModelItem) -> Unit,
    onEventClick: (PolytechEvent) -> Unit,
    onExternalClick: (ExternalNewsItem) -> Unit,
    onLoadMore: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisibleItem.index >= gridState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) onLoadMore()
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 340.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (pageIndex) {
            0 -> {
                items(state.externalNews) { item ->
                    ExternalNewsCard(item = item, onClick = { onExternalClick(item) })
                }
                item {
                    Spacer(Modifier.size(getNavigationBarHeightDp() + 80.dp))
                }
            }
            2 -> {
                items(state.news) { item ->
                    NewsItem(
                        image = extractImageUrl(item.content),
                        categoryName = item.date,
                        title = item.title,
                        tag = item.time,
                        onClick = { onNewsClick(item) }
                    )
                }
                item {
                    Spacer(Modifier.size(getNavigationBarHeightDp() + 80.dp))
                }
            }
            1 -> {
                items(state.events) { event ->
                    EventItem(event = event, onClick = { onEventClick(event) })
                }
                item {
                    Spacer(Modifier.size(getNavigationBarHeightDp() + 80.dp))
                }

            }
        }
    }
}

@Composable
private fun NewsDetailSheet(newsItem: NewsModelItem) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).verticalScroll(rememberScrollState())) {
        Text(newsItem.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("${newsItem.date} в ${newsItem.time}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
        val imageUrl = extractImageUrl(newsItem.content)
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = android.text.Html.fromHtml(newsItem.content, android.text.Html.FROM_HTML_MODE_LEGACY).toString(),
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun EventDetailSheet(event: PolytechEvent) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).verticalScroll(rememberScrollState())) {
        Text(event.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(event.date, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        AsyncImage(
            model = event.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).aspectRatio(16f / 9f),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Зарегистрироваться", fontSize = 16.sp) }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun ExternalNewsDetailSheet(item: ExternalNewsItem) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).verticalScroll(rememberScrollState())) {
        Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(item.date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
        AsyncImage(
            model = item.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).aspectRatio(16f / 9f),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(20.dp))
        Text(item.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Читать полностью", fontSize = 16.sp) }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun ErrorState(message: String, onUpdate: () -> Unit) {
    Column(Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Text(text = message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onUpdate) { Text(stringResource(R.string.try_again_button)) }
    }
}

private fun extractImageUrl(htmlContent: String): String {
    val pattern = Pattern.compile("src=\"([^\"]+)\"")
    val matcher = pattern.matcher(htmlContent)
    return if (matcher.find()) {
        val src = matcher.group(1) ?: ""
        if (src.startsWith("http")) src else "https://e.mospolytech.ru/old/$src"
    } else ""
}