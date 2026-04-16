package com.rozetka.presentation.ui.dialog

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.rozetka.domain.util.StringObject
import com.rozetka.model.File
import com.rozetka.model.MessageDialogItem
import com.rozetka.presentation.ui.gallery.GalleryScreen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.ui.viewer.ImageViewer
import com.rozetka.presentation.util.IDownloadUtils
import com.rozetka.presentation.util.generateColorFromHash
import com.rozetka.presentation.util.getNavigationBarHeightDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


private fun getDisplayDate(datetime: String): String {
    return try {
        val datePart = datetime.take(10)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val messageDate = LocalDate.parse(datePart, formatter)
        val today = LocalDate.now()

        when (messageDate) {
            today -> "Сегодня"
            today.minusDays(1) -> "Вчера"
            else -> {
                val outFormatter = DateTimeFormatter.ofPattern("d MMMM")
                messageDate.format(outFormatter)
            }
        }
    } catch (_: Exception) {
        datetime
    }
}

sealed interface DialogUiItem {
    data class Message(val message: MessageDialogItem) : DialogUiItem
    data class Pending(val pendingMessage: PendingMessage) : DialogUiItem
    data class DateHeader(val dateText: String) : DialogUiItem
}

fun applyMarkdownFormat(
    currentValue: TextFieldValue,
    prefix: String,
    suffix: String = prefix
): TextFieldValue {
    val text = currentValue.text
    val min = currentValue.selection.min.coerceIn(0, text.length)
    val max = currentValue.selection.max.coerceIn(0, text.length)

    return if (min != max) {
        val before = text.substring(0, min)
        val selected = text.substring(min, max)
        val after = text.substring(max)
        val newText = "$before$prefix$selected$suffix$after"
        val newCursorPos = max + prefix.length + suffix.length
        TextFieldValue(text = newText, selection = TextRange(min, newCursorPos))
    } else {
        val before = text.substring(0, min)
        val after = text.substring(min)
        val newText = "$before$prefix$suffix$after"
        val newCursorPos = min + prefix.length
        TextFieldValue(text = newText, selection = TextRange(newCursorPos))
    }
}

fun applyListFormat(currentValue: TextFieldValue, prefix: String): TextFieldValue {
    val text = currentValue.text
    val min = currentValue.selection.min.coerceIn(0, text.length)
    val max = currentValue.selection.max.coerceIn(0, text.length)

    val before = text.substring(0, min)
    val after = text.substring(max)

    val insertText = if (before.isEmpty() || before.endsWith("\n")) prefix else "\n$prefix"
    val newText = "$before$insertText$after"
    return TextFieldValue(text = newText, selection = TextRange(min + insertText.length))
}

class MarkdownVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val builder = AnnotatedString.Builder()
        val originalToTransformed = mutableListOf<Int>()
        val transformedToOriginal = mutableListOf<Int>()

        var i = 0
        var isBold = false
        var isItalic = false
        var isStrike = false
        var isCode = false
        var transformedIndex = 0

        var boldStart = -1
        var italicStart = -1
        var strikeStart = -1
        var codeStart = -1

        while (i < original.length) {
            var skip = 0
            var toggleBold = false
            var toggleItalic = false
            var toggleStrike = false
            var toggleCode = false

            if (i + 1 < original.length && original.substring(i, i + 2) == "**") {
                toggleBold = true
                skip = 2
            } else if (i + 1 < original.length && original.substring(i, i + 2) == "~~") {
                toggleStrike = true
                skip = 2
            } else if (original[i] == '*') {
                toggleItalic = true
                skip = 1
            } else if (original[i] == '`') {
                toggleCode = true
                skip = 1
            }

            if (skip > 0) {
                for (j in 0 until skip) {
                    originalToTransformed.add(transformedIndex)
                }
                i += skip

                if (toggleBold) {
                    if (isBold && boldStart != -1) {
                        builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), boldStart, transformedIndex)
                    } else {
                        boldStart = transformedIndex
                    }
                    isBold = !isBold
                }
                if (toggleItalic) {
                    if (isItalic && italicStart != -1) {
                        builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), italicStart, transformedIndex)
                    } else {
                        italicStart = transformedIndex
                    }
                    isItalic = !isItalic
                }
                if (toggleStrike) {
                    if (isStrike && strikeStart != -1) {
                        builder.addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), strikeStart, transformedIndex)
                    } else {
                        strikeStart = transformedIndex
                    }
                    isStrike = !isStrike
                }
                if (toggleCode) {
                    if (isCode && codeStart != -1) {
                        builder.addStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x33888888)), codeStart, transformedIndex)
                    } else {
                        codeStart = transformedIndex
                    }
                    isCode = !isCode
                }
            } else {
                originalToTransformed.add(transformedIndex)
                transformedToOriginal.add(i)
                builder.append(original[i])
                transformedIndex++
                i++
            }
        }
        originalToTransformed.add(transformedIndex)
        transformedToOriginal.add(original.length)

        if (isBold && boldStart != -1) builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), boldStart, transformedIndex)
        if (isItalic && italicStart != -1) builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), italicStart, transformedIndex)
        if (isStrike && strikeStart != -1) builder.addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), strikeStart, transformedIndex)
        if (isCode && codeStart != -1) builder.addStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x33888888)), codeStart, transformedIndex)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset < 0) return 0
                if (offset >= originalToTransformed.size) return transformedIndex
                return originalToTransformed[offset]
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset < 0) return 0
                if (offset >= transformedToOriginal.size) return original.length
                return transformedToOriginal[offset]
            }
        }

        return TransformedText(builder.toAnnotatedString(), offsetMapping)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DialogScreen(
    dialogViewModel: DialogViewModel = koinViewModel(),
    navController: NavController,
    userId: String,
    userName: String,
    opponentData: String = "",
    isTablet: Boolean = false,
    avatarURL: String = "",
    isSubject: Boolean = false
) {
    val uiState by dialogViewModel.uiState.collectAsStateWithLifecycle()
    val pendingMessages by dialogViewModel.pendingMessages.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedFiles by remember { mutableStateOf<List<java.io.File>>(emptyList()) }
    var selectedMessageForAction by remember { mutableStateOf<Pair<MessageDialogItem, Rect>?>(null) }
    var showGallery by remember { mutableStateOf(false) }

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    var viewerStartIndex by remember { mutableIntStateOf(-1) }
    var viewerMediaUrls by remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    var hasMediaPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasMediaPermission = permissions.values.all { it }
        if (hasMediaPermission) {
            showGallery = true
        } else {
            Toast.makeText(context, "Разрешение отклонено", Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler(enabled = viewerStartIndex >= 0 || showGallery || selectedMessageForAction != null || isSearchActive) {
        when {
            viewerStartIndex >= 0 -> viewerStartIndex = -1
            showGallery -> showGallery = false
            selectedMessageForAction != null -> selectedMessageForAction = null
            isSearchActive -> {
                isSearchActive = false
                searchQuery = ""
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val files = withContext(Dispatchers.IO) { uris.mapNotNull { uri -> uriToFile(context, uri) } }
                selectedFiles = selectedFiles + files
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { uri ->
                scope.launch {
                    val file = withContext(Dispatchers.IO) { uriToFile(context, uri) }
                    if (file != null) selectedFiles = selectedFiles + file
                }
            }
        }
    }

    LaunchedEffect(userId) {
        dialogViewModel.startPolling(userId)
    }

    val names = userName.split(" ").filter { it.isNotEmpty() }
    val initials = if (names.size >= 2) {
        "${names[0].first()}${names[1].first()}"
    } else {
        names.firstOrNull()?.take(1) ?: "?"
    }

    val dummyDownloadUtils = remember {
        object : IDownloadUtils {
            override fun saveFileToDownloads(url: String) {}
            override fun copyBitmapToClipboard(bitmap: android.graphics.Bitmap) {}
            override fun saveBitmapToGallery(bitmap: android.graphics.Bitmap) {}
        }
    }

    var containerPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { 
        containerPosition = it.positionInWindow() 
        containerSize = it.size
    }) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = isSearchActive,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                        slideInHorizontally(initialOffsetX = { it })) togetherWith
                                        fadeOut(animationSpec = tween(90))
                            }, label = "search_animation"
                        ) { searchActive ->
                            if (searchActive) {
                                LaunchedEffect(Unit) {
                                    focusRequester.requestFocus()
                                }
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Поиск...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                    singleLine = true
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SubcomposeAsyncImage(
                                        model = avatarURL.takeIf { it.isNotBlank() },
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(Cookie9Sided.toShape()),
                                        error = {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(generateColorFromHash(userName).copy(alpha = 0.15f))
                                            ) {
                                                Text(
                                                    text = initials.uppercase(),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = generateColorFromHash(userName)
                                                )
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = userName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (opponentData.isNotBlank()) {
                                            Text(
                                                text = opponentData,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        if (isSearchActive) {
                            IconButton(onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            }
                        } else if (!isTablet) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        }
                    },
                    actions = {
                        if (!isSearchActive) {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                        } else {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (!isSubject) {
                    MessageInputExpressive(
                        textValue = messageText,
                        selectedFiles = selectedFiles,
                        onTextChanged = { messageText = it },
                        onSendMessage = {
                            if (selectedFiles.isNotEmpty()) {
                                if (messageText.text.isBlank() && selectedFiles.any { !it.isImageFile() }) {
                                    Toast.makeText(context, "Добавьте описание к файлу", Toast.LENGTH_SHORT).show()
                                } else {
                                    dialogViewModel.sendFiles(selectedFiles, userId, messageText.text)
                                    messageText = TextFieldValue("")
                                    selectedFiles = emptyList()
                                }
                            } else if (messageText.text.isNotBlank()) {
                                dialogViewModel.sendMessage(messageText.text, userId)
                                messageText = TextFieldValue("")
                            }
                        },
                        onAttachFile = {
                            if (hasMediaPermission) {
                                showGallery = true
                            } else {
                                val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO)
                                } else {
                                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                                permissionLauncher.launch(perms)
                            }
                        },
                        onRemoveFile = { fileToRemove ->
                            selectedFiles = selectedFiles.filter { it != fileToRemove }
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top= paddingValues.calculateTopPadding(), bottom = if(isSubject) 0.dp else paddingValues.calculateBottomPadding())
            ) {
                when (val state = uiState) {
                    is DialogUiState.Loading -> LoadingState()
                    is DialogUiState.Success -> DialogSuccessState(
                        messages = state.data,
                        pendingMessages = pendingMessages,
                        searchQuery = searchQuery,
                        onLongClick = { msg, bounds -> selectedMessageForAction = Pair(msg, bounds) },
                        isSubject = isSubject,
                        onMediaClick = { index, urls ->
                            viewerMediaUrls = urls
                            viewerStartIndex = index
                        }
                    )
                    is DialogUiState.Error -> ErrorState(message = state.message) {
                        dialogViewModel.loadMessages(userId, isSilent = false)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showGallery,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            GalleryScreen(
                onMediaSelected = { uris ->
                    scope.launch {
                        val files = withContext(Dispatchers.IO) { uris.mapNotNull { uriToFile(context, it) } }
                        selectedFiles = selectedFiles + files
                        showGallery = false
                    }
                },
                onDismiss = { showGallery = false },
                onCameraClick = {
                    val cacheDir = java.io.File(context.cacheDir, "chat_files")
                    if (!cacheDir.exists()) cacheDir.mkdirs()
                    val photoFile = java.io.File.createTempFile(
                        "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}_",
                        ".jpg",
                        cacheDir
                    )
                    cameraUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
                    cameraLauncher.launch(cameraUri!!)
                },
                hasMediaAccess = hasMediaPermission,
                isPartialAccess = false,
                onPickFromOtherSources = {
                    filePickerLauncher.launch("*/*")
                    showGallery = false
                },
                onRequestMediaAccess = {
                    val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO)
                    } else {
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    permissionLauncher.launch(perms)
                }
            )
        }

        AnimatedVisibility(
            visible = selectedMessageForAction != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { selectedMessageForAction = null }
            ) {
                selectedMessageForAction?.let { (message, bounds) ->
                    val density = LocalDensity.current
                    val containerWidthDp = with(density) { containerSize.width.toDp() }

                    val dpX = with(density) { (bounds.left - containerPosition.x).toDp() }
                    val dpY = with(density) { (bounds.top - containerPosition.y).toDp() }
                    val isMyMessage = message.authorId == StringObject.userId.toString()

                    Box(modifier = Modifier.offset(x = dpX, y = dpY)) {
                        MessageBubble(
                            message = message,
                            isMyMessage = isMyMessage,
                            onImageClick = {},
                            onLongClick = { _, _ -> },
                            isOverlay = true
                        )
                    }

                    val menuY = if (with(density) { (bounds.bottom - containerPosition.y).toDp() } > 550.dp) {
                        dpY - 180.dp
                    } else {
                        dpY + with(density) { bounds.height.toDp() } + 16.dp
                    }

                    val menuX = if (isMyMessage) {
                        (dpX + with(density) { bounds.width.toDp() } - 220.dp).coerceIn(16.dp, (containerWidthDp - 236.dp).coerceAtLeast(16.dp))
                    } else {
                        dpX.coerceIn(16.dp, (containerWidthDp - 236.dp).coerceAtLeast(16.dp))
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = menuX, y = menuY)
                            .padding(bottom = 16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCE4D9)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(modifier = Modifier.width(220.dp).padding(12.dp)) {
                                ExpressiveMenuItem(
                                    icon = Icons.Default.ContentCopy,
                                    text = "Копировать",
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(message.html))
                                        Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                                        selectedMessageForAction = null
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ExpressiveMenuItem(
                                    icon = Icons.Default.Share,
                                    text = "Отправить",
                                    onClick = {
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, message.html)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Отправить сообщение"))
                                        selectedMessageForAction = null
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ExpressiveMenuItem(
                                    icon = Icons.Default.Close,
                                    text = "Закрыть",
                                    onClick = { selectedMessageForAction = null }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (viewerStartIndex >= 0) {
            ImageViewer(
                images = viewerMediaUrls,
                startIndex = viewerStartIndex,
                onDismiss = { viewerStartIndex = -1 },
                downloadUtils = dummyDownloadUtils
            )
        }
    }
}

@Composable
fun ExpressiveMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF191C19),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF191C19),
            fontWeight = FontWeight.Medium
        )
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
private fun DialogSuccessState(
    messages: List<MessageDialogItem>,
    pendingMessages: List<PendingMessage>,
    searchQuery: String,
    onLongClick: (MessageDialogItem, Rect) -> Unit,
    isSubject: Boolean,
    onMediaClick: (Int, List<String>) -> Unit
) {
    val listState = rememberLazyListState()

    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) {
            messages
        } else {
            messages.filter { message ->
                val plainText = HtmlCompat.fromHtml(message.html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
                plainText.contains(searchQuery, ignoreCase = true) ||
                        message.authorName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val allMediaUrls = remember(filteredMessages, pendingMessages) {
        filteredMessages.flatMap { it.files }
            .filter { it.isImage() || it.isVideo() }
            .map { it.url }
    }

    val groupedItems = remember(filteredMessages, pendingMessages, searchQuery) {
        val uiItems = mutableListOf<DialogUiItem>()
        if (searchQuery.isBlank()) {
            pendingMessages.forEach { pm -> uiItems.add(DialogUiItem.Pending(pm)) }
        }
        filteredMessages.forEachIndexed { index, message ->
            uiItems.add(DialogUiItem.Message(message))
            val currentDate = message.datetime.take(10)
            val nextMessage = filteredMessages.getOrNull(index + 1)
            val nextDate = nextMessage?.datetime?.take(10)
            if (currentDate != nextDate) {
                uiItems.add(DialogUiItem.DateHeader(getDisplayDate(message.datetime)))
            }
        }
        uiItems
    }

    LaunchedEffect(filteredMessages.size, pendingMessages.size) {
        if (groupedItems.isNotEmpty() && searchQuery.isBlank()) {
            listState.animateScrollToItem(0)
        }
    }

    var containerPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { 
        containerPosition = it.positionInWindow() 
        containerSize = it.size
    }) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            items(
                items = groupedItems,
                key = { item ->
                    when (item) {
                        is DialogUiItem.DateHeader -> "date_${item.dateText}"
                        is DialogUiItem.Message -> "msg_${item.message.hashCode()}"
                        is DialogUiItem.Pending -> "pending_${item.pendingMessage.id}"
                    }
                }
            ) { item ->
                Box(modifier = Modifier.animateItem()) {
                    when (item) {
                        is DialogUiItem.DateHeader -> {
                            DateHeaderItem(dateText = item.dateText)
                        }
                        is DialogUiItem.Message -> {
                            MessageBubble(
                                message = item.message,
                                isMyMessage = item.message.authorId == StringObject.userId.toString(),
                                onImageClick = { url ->
                                    val index = allMediaUrls.indexOf(url).takeIf { it >= 0 } ?: 0
                                    onMediaClick(index, allMediaUrls)
                                },
                                onLongClick = onLongClick
                            )
                        }
                        is DialogUiItem.Pending -> {
                            PendingMessageBubble(pendingMessage = item.pendingMessage)
                        }
                    }
                }
            }
            if (isSubject) {
                item { Spacer(Modifier.height(getNavigationBarHeightDp())) }
            }
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

fun File.isImage(): Boolean {
    val extension = this.name.substringAfterLast('.', "").lowercase(Locale.getDefault())
    return extension in listOf("jpg", "jpeg", "png", "gif", "webp")
}

fun File.isVideo(): Boolean {
    val extension = this.name.substringAfterLast('.', "").lowercase(Locale.getDefault())
    return extension in listOf("mp4", "avi", "mov", "mkv", "webm", "3gp")
}

fun java.io.File.isImageFile(): Boolean {
    val extension = this.name.substringAfterLast('.', "").lowercase(Locale.getDefault())
    return extension in listOf("jpg", "jpeg", "png", "gif", "webp")
}

@Composable
fun MessageInputExpressive(
    textValue: TextFieldValue,
    selectedFiles: List<java.io.File>,
    onTextChanged: (TextFieldValue) -> Unit,
    onSendMessage: () -> Unit,
    onAttachFile: () -> Unit,
    onRemoveFile: (java.io.File) -> Unit
) {
    var showFormatMenu by remember { mutableStateOf(false) }
    val visualTransformation = remember { MarkdownVisualTransformation() }

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
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onAttachFile,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                                .padding(4.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextField(
                        value = textValue,
                        onValueChange = onTextChanged,
                        visualTransformation = visualTransformation,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Сообщение...") },
                        shape = RoundedCornerShape(50),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                        ),
                        maxLines = 5,
                        trailingIcon = {
                            Box {
                                IconButton(onClick = { showFormatMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = showFormatMenu,
                                    onDismissRequest = { showFormatMenu = false },
                                    properties = PopupProperties(focusable = false)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Жирный") },
                                        onClick = { onTextChanged(applyMarkdownFormat(textValue, "**")); showFormatMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Курсив") },
                                        onClick = { onTextChanged(applyMarkdownFormat(textValue, "*")); showFormatMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Зачёркнутый") },
                                        onClick = { onTextChanged(applyMarkdownFormat(textValue, "~~")); showFormatMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Моно") },
                                        onClick = { onTextChanged(applyMarkdownFormat(textValue, "`")); showFormatMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Список") },
                                        onClick = { onTextChanged(applyListFormat(textValue, "- ")); showFormatMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Нумерация") },
                                        onClick = { onTextChanged(applyListFormat(textValue, "1. ")); showFormatMenu = false }
                                    )
                                }
                            }
                        }
                    )

                    val canSend = textValue.text.isNotBlank() || selectedFiles.isNotEmpty()

                    AnimatedVisibility(
                        visible = canSend,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        IconButton(
                            onClick = onSendMessage,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
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
                contentDescription = null,
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