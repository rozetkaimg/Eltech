package com.rozetka.presentation.ui.digitalService

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialShapes.Companion.Clover8Leaf
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.DigitalServiceModelItem
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.getNavigationBarHeightDp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalServiceScreen(
    navController: NavController,
    viewModel: DigitalServiceViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var selectedRequest by remember { mutableStateOf<DigitalServiceModelItem?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        stringResource(R.string.digital_services),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface

                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = getNavigationBarHeightDp() + 80.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is DigitalServiceUiState.Loading -> LoadingState()
                is DigitalServiceUiState.Success -> {
                    DigitalServiceSuccessState(
                        requests = state.requests,
                        onItemClick = { request ->
                            selectedRequest = request
                        }
                    )
                }

                is DigitalServiceUiState.Error -> ErrorState(
                    message = state.message,
                    onUpdate = { viewModel.loadAppRequests() }
                )
            }
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.SubmitAnApplication.route) },
                modifier = Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .padding(16.dp),
                icon = {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.create_new_request_fab)
                    )
                },
                text = { Text(text = stringResource(R.string.apply_now)) },
            )

            if (selectedRequest != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedRequest = null },
                    sheetState = sheetState
                ) {
                    RequestDetailsSheetContent(
                        request = selectedRequest!!,
                        onDismiss = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    selectedRequest = null
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DigitalServiceSuccessState(
    requests: List<DigitalServiceModelItem>,
    onItemClick: (DigitalServiceModelItem) -> Unit
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_requests_yet))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests.reversed()) { request ->
                RequestItem(
                    request = request,
                    onClick = { onItemClick(request) }
                )
            }
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


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RequestDetailsSheetContent(
    request: DigitalServiceModelItem,
    onDismiss: () -> Unit
) {


    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()

            .padding(bottom = getNavigationBarHeightDp()),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            val gradeShape = when (request.status) {
                "Получено", "Готово" -> Cookie9Sided
                else -> Clover8Leaf
            }
            val gradeColor = when (request.status) {
                "Получено", "Готово" -> Color(0xFF4CAF50)
                "Отклонено" -> Color.Red
                else -> Color(0xFFFFC107)
            }
            val gradeText = when (request.status) {

                "Получено", "Готово" -> Icons.Default.Done
                "Отклонено" -> Icons.Default.Close
                else -> Icons.Default.Edit
            }
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .background(gradeColor, gradeShape.toShape())
                        .size(65.dp)
                        .padding(16.dp)
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = gradeText,
                        "",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.details_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontWeight = FontWeight.Bold

                )
                Spacer(Modifier.weight(0.5f))
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }

            }

        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp,
                    bottomEnd = 8.dp,
                    bottomStart = 8.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(stringResource(R.string.status), request.status)

                    DetailRow(stringResource(R.string.created_at), request.created)
                }
            }
        }

        item { Spacer(Modifier.height(2.dp)) }


        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                val annotatedText = remember(request.description) {
                    val cleanHtml = request.description
                        .replace("\\r\\n\\t", "")
                        .replace("\\\"", "\"")
                        .replace("\\/", "/")
                        .replace(Regex("<p>\\s*</p>"), "")
                        .replace("<p>", "")
                        .replace("</p>", "<br>")
                        .trim()

                    val spanned = HtmlCompat.fromHtml(cleanHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
                    AnnotatedString.Builder().apply {
                        append(spanned)
                    }.toAnnotatedString()
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailSectionTitle(stringResource(R.string.description))
                    Text(
                        text = annotatedText.text.ifEmpty { stringResource(R.string.no_description) },
                        style = MaterialTheme.typography.bodyMedium,

                        )
                }
            }
        }

        item { Spacer(Modifier.height(2.dp)) }
        item { }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    bottomEnd = 28.dp,
                    bottomStart = 28.dp,
                    topEnd = 8.dp, topStart = 8.dp

                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val annotatedText = remember(request.comment) {
                        val cleanHtml = request.comment
                            .replace("\\r\\n\\t", "")
                            .replace("\\\"", "\"")
                            .replace("\\/", "/")
                            .replace(Regex("<p>\\s*</p>"), "")
                            .replace("<p>", "")
                            .replace("</p>", "<br>")
                            .trim()

                        val spanned =
                            HtmlCompat.fromHtml(cleanHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
                        AnnotatedString.Builder().apply {
                            append(spanned)
                        }.toAnnotatedString()
                    }

                    DetailSectionTitle(stringResource(R.string.comment))
                    Text(
                        text = annotatedText.text.ifEmpty { stringResource(R.string.no_comment) },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (request.filesInput.isNotEmpty()) {
            item { Spacer(Modifier.height(16.dp)) }
            item { DetailSectionTitle(stringResource(R.string.input_files)) }
            items(request.filesInput) { file ->
                FileLinkItem(name = file.name, url = file.url)
            }
        }

        if (request.filesOutput.isNotEmpty()) {
            item { Spacer(Modifier.height(16.dp)) }
            item { DetailSectionTitle(stringResource(R.string.output_files)) }
            items(request.filesOutput) { file ->
                FileLinkItem(name = file.fname, url = file.url)
            }
        }

    }
}

@Composable
private fun FileLinkItem(name: String, url: String) {

    val openUrlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    TextButton(
        onClick = {
            try {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                openUrlLauncher.launch(intent)
            } catch (e: Exception) {
                Log.e("FileLinkItem", "Could not open browser for $url", e)
            }
        },
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = name,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Spacer(Modifier.height(2.dp))
}

@Composable
private fun DetailSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(Modifier.height(2.dp))
}