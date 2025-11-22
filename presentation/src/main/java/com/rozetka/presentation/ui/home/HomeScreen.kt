package com.rozetka.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rozetka.domain.UserDataHolder
import com.rozetka.model.NewsModelItem
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedNewsItem by remember { mutableStateOf<NewsModelItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (uiState) {
                        is HomeUiState.Success -> Text(
                            stringResource(R.string.greeting_user, UserDataHolder().getUserName()),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 24.sp
                        )

                        else -> Text(stringResource(R.string.home_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> LoadingState()
                is HomeUiState.Success -> HomeSuccessState(
                    news = state.news,
                    onNewsClick = { newsItem ->
                        selectedNewsItem = newsItem
                        scope.launch { sheetState.show() }
                    }
                )

                is HomeUiState.Error -> ErrorState(
                    message = state.message,
                    onUpdate = { viewModel.loadHomeData() }
                )
            }
        }
    }

    if (sheetState.isVisible && selectedNewsItem != null) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    selectedNewsItem = null
                }
            },
            sheetState = sheetState
        ) {
            NewsDetailSheet(newsItem = selectedNewsItem!!)
        }
    }
}

private fun extractImageUrl(htmlContent: String): String {
    val pattern = Pattern.compile("src=\"([^\"]+)\"")
    val matcher = pattern.matcher(htmlContent)
    return if (matcher.find()) {
        val src = matcher.group(1)
        if (src.startsWith("http")) src else "https://e.mospolytech.ru/old/$src"
    } else {
        ""
    }
}

private fun htmlToString(html: String): String {
    return android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
}

@Composable
private fun HomeSuccessState(news: List<NewsModelItem>, onNewsClick: (NewsModelItem) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = stringResource(R.string.latest_news),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                fontSize = 24.sp
            )
        }
        items(news) { newsItem ->
            val imageUrl = extractImageUrl(newsItem.content)
            NewsItem(
                image = imageUrl,
                categoryName = newsItem.date,
                title = newsItem.title,
                tag = newsItem.time,
                onClick = { onNewsClick(newsItem) }
            )
            Spacer(Modifier.height(8.dp))
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
            Text(stringResource(R.string.try_again_button))
        }
    }
}

@Composable
private fun NewsDetailSheet(newsItem: NewsModelItem) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = newsItem.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "${newsItem.date} в ${newsItem.time}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val imageUrl = extractImageUrl(newsItem.content)
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.cd_news_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = htmlToString(newsItem.content),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}