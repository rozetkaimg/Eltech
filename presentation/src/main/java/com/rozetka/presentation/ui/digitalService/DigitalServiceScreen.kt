package com.rozetka.presentation.ui.digitalService

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.DigitalServiceModelItem
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.UiSize
import com.rozetka.presentation.util.getNavigationBarHeightDp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

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

                is DigitalServiceUiState.Error -> ExpressiveErrorState(
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
                    .padding(end = 16.dp, bottom = UiSize().getNavBarPaddingSize() + 16.dp),
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
            items(requests.reversed(), key = { it.id }) { request ->
                RequestItem(
                    request = request,
                    onClick = { onItemClick(request) }
                )
            }
            item { Spacer(Modifier.height(UiSize().getNavBarPaddingSize())) }
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
                    DetailRow(stringResource(R.string.status), getStatusLabel(request.status))

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

        if (request.filesOutput.isNotEmpty()) {
            item { Spacer(Modifier.height(16.dp)) }
            item { DetailSectionTitle(stringResource(R.string.output_files)) }
            items(request.filesOutput, key = { it.url }) { file ->
                FileLinkItem(name = file.fname, url = file.url)
            }
        }

        if (request.filesInput.isNotEmpty()) {
            item { Spacer(Modifier.height(16.dp)) }
            item { DetailSectionTitle(stringResource(R.string.input_files)) }
            items(request.filesInput, key = { it.url }) { file ->
                FileLinkItem(name = file.name, url = file.url)
            }
        }

    }
}

@Composable
private fun FileLinkItem(name: String, url: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var showFileMenu by remember { mutableStateOf(false) }

    val extension = name.substringAfterLast('.', "").lowercase()
    val fileIcon = when (extension) {
        "mp4", "avi", "mov", "mkv" -> Icons.Default.PlayArrow
        "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx" -> ImageVector.vectorResource(R.drawable.document_text_outline_24)
        else -> ImageVector.vectorResource(R.drawable.document_outline_28)
    }

    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (!isDownloading) {
                                downloadAndOpenFile(
                                    context = context,
                                    fileUrl = url,
                                    fileName = name,
                                    scope = coroutineScope,
                                    onStart = { isDownloading = true },
                                    onFinish = { isDownloading = false },
                                    onError = {
                                        Toast.makeText(context, context.getString(R.string.error_download_failed), Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        onLongPress = {
                            showFileMenu = true
                        }
                    )
                },
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fileIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showFileMenu,
            onDismissRequest = { showFileMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenu(
                expanded = showFileMenu,
                onDismissRequest = { showFileMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.action_share))
                    },
                    onClick = {
                        showFileMenu = false
                        shareFile(context, url, name, coroutineScope)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.action_share)

                        )
                    }
                )
            }

        }
    }
}

private fun shareFile(
    context: Context,
    fileUrl: String,
    fileName: String,
    scope: CoroutineScope
) {
    scope.launch(Dispatchers.IO) {
        try {
            val downloadedFile = getFileForUrl(context, fileUrl, fileName)

            if (!downloadedFile.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.preparing_for_share), Toast.LENGTH_SHORT).show()
                }
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("HTTP ${connection.responseCode}")
                }

                connection.inputStream.use { input ->
                    FileOutputStream(downloadedFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    downloadedFile
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    val extension = downloadedFile.name.substringAfterLast('.', "").lowercase()
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newRawUri(null, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_share)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.error_file_preparation), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun downloadAndOpenFile(
    context: Context,
    fileUrl: String,
    fileName: String,
    scope: CoroutineScope,
    onStart: () -> Unit,
    onFinish: () -> Unit,
    onError: (Exception) -> Unit
) {
    scope.launch(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) { onStart() }

            val downloadedFile = getFileForUrl(context, fileUrl, fileName)

            if (!downloadedFile.exists()) {
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("HTTP ${connection.responseCode}")
                }

                connection.inputStream.use { input ->
                    FileOutputStream(downloadedFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                onFinish()
                openFileWithIntent(context, downloadedFile, fileUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onFinish()
                onError(e)
            }
        }
    }
}

private fun openFileWithIntent(context: Context, file: java.io.File, fallbackUrl: String) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val extension = file.name.substringAfterLast('.', "").lowercase()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_open)))
    } catch (e: Exception) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
            context.startActivity(browserIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, context.getString(R.string.cannot_open_file), Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun getStatusLabel(status: String): String {
    return when (status) {
        "Получено" -> stringResource(R.string.status_received)
        "Готово" -> stringResource(R.string.status_ready)
        "Отклонено" -> stringResource(R.string.status_rejected)
        "В работе" -> stringResource(R.string.status_in_progress)
        else -> status
    }
}

private fun getFileForUrl(context: Context, url: String, fileName: String): java.io.File {
    val hash = url.hashCode().toString(16)
    val dir = java.io.File(context.cacheDir, "digital_service_files/$hash")
    if (!dir.exists()) dir.mkdirs()
    return java.io.File(dir, fileName)
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