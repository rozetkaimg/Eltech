package com.rozetka.presentation.ui.dialog

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rozetka.domain.util.StringObject
import com.rozetka.model.File
import com.rozetka.model.MessageDialogItem
import com.rozetka.presentation.ui.pay.LoadingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatTime(dateTimeString: String): String {
    return try {
        dateTimeString.substring(11, 16)
    } catch (_: Exception) {
        ""
    }
}

private fun getDisplayDate(datetime: String): String {
    return try {
        val datePart = datetime.substring(0, 10)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val messageDate = LocalDate.parse(datePart, formatter)
        val today = LocalDate.now()

        when (messageDate) {
            today -> "Сегодня"
            today.minusDays(1) -> "Вчера"
            else -> {
                val outFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
                messageDate.format(outFormatter)
            }
        }
    } catch (e: Exception) {
        datetime
    }
}

sealed interface DialogUiItem {
    data class Message(val message: MessageDialogItem) : DialogUiItem
    data class DateHeader(val dateText: String) : DialogUiItem
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogScreen(
    dialogViewModel: DialogViewModel = koinViewModel(),
    navController: NavController,
    userId: String,
    userName: String
) {
    val uiState by dialogViewModel.uiState.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    var selectedFiles by remember { mutableStateOf<List<java.io.File>>(emptyList()) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val files = withContext(Dispatchers.IO) {
                    uris.mapNotNull { uri -> uriToFile(context, uri) }
                }
                // Добавляем новые файлы к уже выбранным
                if (files.isNotEmpty()) {
                    selectedFiles = selectedFiles + files
                }
            }
        }
    }

    LaunchedEffect(userId) {
        dialogViewModel.startPolling(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = userName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        bottomBar = {
            MessageInputExpressive(
                text = messageText,
                selectedFiles = selectedFiles,
                onTextChanged = { messageText = it },
                onSendMessage = {
                    // Логика отправки
                    if (selectedFiles.isNotEmpty()) {
                        // Если есть файлы, проверяем наличие текста (если нужно)
                        if (messageText.isBlank()) {
                            Toast.makeText(context, "Добавьте описание к файлу", Toast.LENGTH_SHORT).show()
                        } else {
                            dialogViewModel.sendFiles(selectedFiles, userId, messageText)
                            // Очистка после отправки
                            messageText = ""
                            selectedFiles = emptyList()
                        }
                    } else if (messageText.isNotBlank()) {
                        // Только текст
                        dialogViewModel.sendMessage(messageText, userId)
                        messageText = ""
                    }
                },
                onAttachFile = {
                    filePickerLauncher.launch("*/*")
                },
                onRemoveFile = { fileToRemove ->
                    selectedFiles = selectedFiles.filter { it != fileToRemove }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DialogUiState.Loading -> LoadingState()
                is DialogUiState.Success -> DialogSuccessState(
                    messages = state.data
                )
                is DialogUiState.Error -> ErrorState(message = state.message) {
                    dialogViewModel.loadMessages(userId, isSilent = false)
                }
            }
        }
    }
}

private fun uriToFile(context: Context, uri: Uri): java.io.File? {
    return try {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        val name = cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) it.getString(index) else "temp_file"
            } else "temp_file"
        } ?: "temp_file"

        val tempFile = java.io.File(context.cacheDir, name)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
private fun DialogSuccessState(messages: List<MessageDialogItem>) {
    val listState = rememberLazyListState()
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val groupedItems = remember(messages) {
        val uiItems = mutableListOf<DialogUiItem>()
        messages.forEachIndexed { index, message ->
            uiItems.add(DialogUiItem.Message(message))

            val currentDate = message.datetime.take(10)
            val nextMessage = messages.getOrNull(index + 1)
            val nextDate = nextMessage?.datetime?.take(10)

            if (currentDate != nextDate) {
                uiItems.add(DialogUiItem.DateHeader(getDisplayDate(message.datetime)))
            }
        }
        uiItems
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            items(groupedItems) { item ->
                when (item) {
                    is DialogUiItem.DateHeader -> {
                        DateHeaderItem(dateText = item.dateText)
                    }
                    is DialogUiItem.Message -> {
                        MessageBubble(
                            message = item.message,
                            isMyMessage = item.message.authorId == StringObject.userId.toString(),
                            onImageClick = { imageUrl ->
                                selectedImageUrl = imageUrl
                            }
                        )
                    }
                }
            }
        }
        if (selectedImageUrl != null) {
            FullscreenImageDialog(
                imageUrl = selectedImageUrl!!,
                onDismiss = { selectedImageUrl = null }
            )
        }
    }
}

@Composable
fun DateHeaderItem(dateText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FullscreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Полноэкранное изображение",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

fun File.isImage(): Boolean {
    val extension = this.name.substringAfterLast('.', "").lowercase(Locale.getDefault())
    return extension in listOf("jpg", "jpeg", "png", "gif", "webp")
}

fun java.io.File.isImageFile(): Boolean {
    val extension = this.name.substringAfterLast('.', "").lowercase(Locale.getDefault())
    return extension in listOf("jpg", "jpeg", "png", "gif", "webp")
}


@Composable
fun MessageInputExpressive(
    text: String,
    selectedFiles: List<java.io.File>,
    onTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachFile: () -> Unit,
    onRemoveFile: (java.io.File) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)

            .imePadding()
    ) {
        Surface(
            tonalElevation = 3.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Column {

                AnimatedVisibility(
                    visible = selectedFiles.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedFiles) { file ->
                            SelectedFilePreviewItem(
                                file = file,
                                onRemove = { onRemoveFile(file) }
                            )
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp) .navigationBarsPadding(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    TextField(
                        value = text,
                        onValueChange = onTextChanged,
                        modifier = Modifier.weight(1f),

                        placeholder = { Text("Сообщение...") },
                        shape = CircleShape,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                        maxLines = 5
                    )

                    Spacer(Modifier.width(8.dp))

                    FilledTonalIconButton(onClick = onAttachFile) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Прикрепить файл"
                        )
                    }

                    val canSend = text.isNotBlank() || selectedFiles.isNotEmpty()

                    AnimatedVisibility(
                        visible = canSend,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        FilledIconButton(onClick = onSendMessage) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Отправить сообщение"
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SelectedFilePreviewItem(
    file: java.io.File,
    onRemove: () -> Unit
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.size(60.dp, 60.dp)
        ) {
            if (file.isImageFile()) {
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }


        Box(
            modifier = Modifier
                .padding(2.dp)
                .size(20.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Удалить",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}