package com.rozetka.presentation.ui.moodle

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import com.rozetka.model.AttachmentItem
import com.rozetka.model.ModuleContentNative
import com.rozetka.presentation.ui.pay.LoadingState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleNativeScreen(
    url: String,
    onBackClick: () -> Unit,
    viewModel: ModuleNativeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(url) {
        viewModel.loadModuleContent(url)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = (uiState as? ModuleNativeUiState.Success)?.data?.title ?: "Материал"
                    Text(titleText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ModuleNativeUiState.Loading -> LoadingState()
                is ModuleNativeUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadModuleContent(url) }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Повторить")
                        }
                    }
                }
                is ModuleNativeUiState.Success -> {
                    val data = state.data
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Текстовое описание
                        if (data.textHtml.isNotBlank()) {
                            item {
                                HtmlText(html = data.textHtml, modifier = Modifier.fillMaxWidth())
                            }
                        }

                        // Изображения (нативно через Coil)
                        items(data.images) { imageUrl ->
                            Card(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Файлы для скачивания
                        if (data.files.isNotEmpty()) {
                            item { Text("Файлы:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                            items(data.files) { file ->
                                AttachmentCard(
                                    item = file,
                                    icon = Icons.Default.Download,
                                    onClick = { startDownload(context, file.url, file.name, state.moodleSession) }
                                )
                            }
                        }

                        // Внешние ссылки и видео
                        if (data.links.isNotEmpty()) {
                            item { Text("Ссылки:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                            items(data.links) { link ->
                                AttachmentCard(
                                    item = link,
                                    icon = Icons.Default.OpenInNew,
                                    onClick = { uriHandler.openUri(link.url) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextColor(textColor)
                textSize = 16f
            }
        },
        update = { textView ->
            textView.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AttachmentCard(
    item: AttachmentItem,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Запуск скачивания файла через системный DownloadManager
private fun startDownload(context: Context, url: String, fileName: String, moodleSession: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            addRequestHeader("Cookie", "MoodleSession=$moodleSession")
            setTitle(fileName)
            setDescription("Загрузка документа Moodle...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(context, "Началась загрузка: $fileName", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Ошибка при скачивании файла", Toast.LENGTH_SHORT).show()
    }
}
