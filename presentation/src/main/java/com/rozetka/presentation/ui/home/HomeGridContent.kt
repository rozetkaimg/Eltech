package com.rozetka.presentation.ui.home

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rozetka.model.ExternalNewsItem
import com.rozetka.model.NewsModelItem
import com.rozetka.model.PolytechEvent
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.moodle.DeadlineExpressiveCard
import com.rozetka.presentation.util.getNavigationBarHeightDp

@Composable
fun HomeGridContent(
    state: HomeUiState.Success,
    pageIndex: Int,
    onNewsClick: (NewsModelItem) -> Unit,
    onEventClick: (PolytechEvent) -> Unit,
    navController: NavController,
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
                    ExternalNewsCard(item = item, onClick = {
                        val encodedUrl =
                            Uri.encode(item.link)
                        navController.navigate("Article/$encodedUrl")
                    })
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
            3 -> {
                if (state.deadlines.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Дедлайнов не найдено",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { navController.navigate(Screen.MoodleDeadlines.route) },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Открыть календарь")
                            }
                        }
                    }
                } else {
                    items(state.deadlines) { deadline ->
                        DeadlineExpressiveCard(deadline = deadline)
                    }
                }
                item {
                    Spacer(Modifier.size(getNavigationBarHeightDp() + 80.dp))
                }
            }
        }
    }
}
