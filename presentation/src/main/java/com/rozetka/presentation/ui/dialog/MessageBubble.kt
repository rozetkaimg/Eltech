package com.rozetka.presentation.ui.dialog

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.text.util.Linkify
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.core.text.util.LinkifyCompat
import coil.compose.AsyncImage
import com.rozetka.model.MessageDialogItem
import com.rozetka.presentation.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MessageBubble(
    message: MessageDialogItem,
    isMyMessage: Boolean,
    onImageClick: (String) -> Unit,
    onLongClick: (MessageDialogItem, Rect) -> Unit,
    isOverlay: Boolean = false
) {
    val bubbleColor =
        if (isMyMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow

    val bubbleShape = if (!isMyMessage) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 5.dp, bottomEnd = 20.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 5.dp)
    }

    val uriHandler = LocalUriHandler.current
    var bounds by remember { mutableStateOf(Rect.Zero) }

    val cardContent = @Composable {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .onGloballyPositioned { coordinates ->
                    bounds = coordinates.boundsInWindow()
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongClick(message, bounds) }
                    )
                },
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 8.dp
                )
            ) {
                if (!isMyMessage) {
                    Text(
                        text = message.authorName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }

                val imageFiles = remember(message.files) {
                    message.files.filter { it.isImage() || it.isVideo() }
                }
                val otherFiles = remember(message.files) {
                    message.files.filterNot { it.isImage() || it.isVideo() }
                }

                if (imageFiles.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        imageFiles.forEach { file ->
                            AsyncImage(
                                model = file.url,
                                contentDescription = file.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onImageClick(file.url) },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                val linkColor = MaterialTheme.colorScheme.primary
                val annotatedText = remember(message.html) {
                    val preProcessed = message.html
                        .replace("\\r\\n\\t", "")
                        .replace("\\\"", "\"")
                        .replace("\\/", "/")
                        .replace(Regex("<p>\\s*</p>"), "")
                        .replace("<p>", "")
                        .replace("</p>", "<br>")
                        .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
                        .replace(Regex("\\*(.*?)\\*"), "<i>$1</i>")
                        .replace(Regex("~~(.*?)~~"), "<s>$1</s>")
                        .replace(Regex("`(.*?)`"), "<tt>$1</tt>")
                        .trim()

                    val spanned = HtmlCompat.fromHtml(preProcessed, HtmlCompat.FROM_HTML_MODE_COMPACT)
                    val spannable = SpannableString(spanned)
                    LinkifyCompat.addLinks(spannable, Linkify.WEB_URLS)

                    buildAnnotatedString {
                        append(spannable.toString())
                        val characterStyles = spannable.getSpans(0, spannable.length, CharacterStyle::class.java)

                        for (span in characterStyles) {
                            val start = spannable.getSpanStart(span)
                            val end = spannable.getSpanEnd(span)
                            when (span) {
                                is URLSpan -> {
                                    addStringAnnotation("URL", span.url, start, end)
                                    addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), start, end)
                                }
                                is StyleSpan -> {
                                    if (span.style == Typeface.BOLD || span.style == Typeface.BOLD_ITALIC) {
                                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                                    }
                                    if (span.style == Typeface.ITALIC || span.style == Typeface.BOLD_ITALIC) {
                                        addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                                    }
                                }
                                is StrikethroughSpan -> {
                                    addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                                }
                                is UnderlineSpan -> {
                                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                                }
                                is TypefaceSpan -> {
                                    if (span.family == "monospace") {
                                        addStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.Gray.copy(alpha = 0.2f)), start, end)
                                    }
                                }
                            }
                        }
                    }
                }

                if (annotatedText.isNotBlank()) {
                    if (imageFiles.isNotEmpty()) Spacer(Modifier.height(4.dp))

                    val textColor = if (isMyMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

                    ClickableText(
                        text = annotatedText,
                        style = TextStyle(
                            color = textColor,
                            fontSize = 16.sp
                        ),
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    try {
                                        uriHandler.openUri(annotation.item)
                                    } catch (e: Exception) {
                                        Log.w("MessageBubble", "Не удалось открыть: ${annotation.item}", e)
                                    }
                                }
                        }
                    )
                }

                if (otherFiles.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        otherFiles.forEach { file ->
                            FileAttachmentItem(file = file)
                        }
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = try { message.datetime.substring(11, 16) } catch (_: Exception) { "" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isMyMessage) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (isOverlay) {
        cardContent()
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            cardContent()
        }
    }
}

@Composable
fun PendingMessageBubble(
    pendingMessage: PendingMessage
) {
    val bubbleColor = MaterialTheme.colorScheme.primaryContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(0.7f),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 5.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            ) {
                val imageFiles = pendingMessage.files.filter { it.isImageFile() }
                val otherFiles = pendingMessage.files.filterNot { it.isImageFile() }

                if (imageFiles.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        imageFiles.forEach { file ->
                            AsyncImage(
                                model = file,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                if (pendingMessage.text.isNotBlank()) {
                    if (imageFiles.isNotEmpty()) Spacer(Modifier.height(4.dp))

                    val annotatedText = remember(pendingMessage.text) {
                        val preProcessed = pendingMessage.text
                            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
                            .replace(Regex("\\*(.*?)\\*"), "<i>$1</i>")
                            .replace(Regex("~~(.*?)~~"), "<s>$1</s>")
                            .replace(Regex("`(.*?)`"), "<tt>$1</tt>")

                        val spanned = HtmlCompat.fromHtml(preProcessed, HtmlCompat.FROM_HTML_MODE_COMPACT)
                        val spannable = SpannableString(spanned)

                        buildAnnotatedString {
                            append(spannable.toString())
                            val characterStyles = spannable.getSpans(0, spannable.length, CharacterStyle::class.java)

                            for (span in characterStyles) {
                                val start = spannable.getSpanStart(span)
                                val end = spannable.getSpanEnd(span)
                                when (span) {
                                    is StyleSpan -> {
                                        if (span.style == Typeface.BOLD) {
                                            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                                        }
                                        if (span.style == Typeface.ITALIC) {
                                            addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                                        }
                                    }
                                    is StrikethroughSpan -> {
                                        addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
                                    }
                                    is TypefaceSpan -> {
                                        if (span.family == "monospace") {
                                            addStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.Gray.copy(alpha = 0.2f)), start, end)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = annotatedText,
                        style = TextStyle(color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp)
                    )
                }

                if (otherFiles.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        otherFiles.forEach { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.InsertDriveFile, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = try { pendingMessage.timestamp.substring(11, 16) } catch (_: Exception) { "" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


@Composable
fun FileAttachmentItem(file: com.rozetka.model.File) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var showFileMenu by remember { mutableStateOf(false) }

    val extension = file.name.substringAfterLast('.', "").lowercase()
    val fileIcon = when (extension) {
        "mp4", "avi", "mov", "mkv" -> Icons.Default.PlayArrow
        "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx" -> ImageVector.vectorResource(R.drawable.document_text_outline_24)
        else -> ImageVector.vectorResource(R.drawable.document_outline_28)
    }

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (!isDownloading) {
                                downloadAndOpenFile(
                                    context = context,
                                    fileUrl = file.url,
                                    fileName = file.name,
                                    scope = coroutineScope,
                                    onStart = { isDownloading = true },
                                    onFinish = { isDownloading = false },
                                    onError = {
                                        Toast.makeText(context, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        onLongPress = {
                            showFileMenu = true
                        }
                    )
                },
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fileIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showFileMenu,
            onDismissRequest = { showFileMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Поделиться файлом", color = MaterialTheme.colorScheme.onSurface) },
                onClick = {
                    showFileMenu = false
                    shareFile(context, file.url, file.name, coroutineScope)
                },
                leadingIcon = {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
            )
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
            val cacheDir = java.io.File(context.cacheDir, "chat_files")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val downloadedFile = java.io.File(cacheDir, fileName)

            if (!downloadedFile.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Подготовка к отправке...", Toast.LENGTH_SHORT).show()
                }
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

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
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Поделиться файлом"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ошибка при подготовке файла", Toast.LENGTH_SHORT).show()
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

            val cacheDir = java.io.File(context.cacheDir, "chat_files")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val downloadedFile = java.io.File(cacheDir, fileName)

            if (!downloadedFile.exists()) {
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

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
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(intent, "Открыть"))
    } catch (e: Exception) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
            context.startActivity(browserIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, "Не удалось открыть файл", Toast.LENGTH_SHORT).show()
        }
    }
}