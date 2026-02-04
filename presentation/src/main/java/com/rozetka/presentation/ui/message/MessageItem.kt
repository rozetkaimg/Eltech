package com.rozetka.presentation.ui.message

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.SubcomposeAsyncImage
import com.rozetka.model.MessageModelItem
import com.rozetka.presentation.util.generateColorFromHash
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MessageItem(
    message: MessageModelItem,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val opponentName = message.opponent.name?.takeIf { it.isNotBlank() } ?: "Личный чат"
    val avatarUrl = message.opponent.avatar?.takeIf { it.isNotBlank() }

    val prefix = if (message.lastmessage.from == "you") "Вы: " else ""
    val rawMessageText = message.lastmessage.text?.takeIf { it.isNotBlank() } ?: "Вложение"
    val messageDisplayText = "$prefix$rawMessageText"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        val names = opponentName.split(" ").filter { it.isNotEmpty() }
        val initials = if (names.size >= 2) {
            "${names[0].first()}${names[1].first()}"
        } else {
            names.firstOrNull()?.take(1) ?: "?"
        }

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(Cookie9Sided.toShape()),
                error = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(generateColorFromHash(opponentName).copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = initials.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = generateColorFromHash(opponentName)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (!message.subject.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = message.subject,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = opponentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = formatFullDateAndTime(context, message.lastmessage.datetime ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        softWrap = false,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val annotatedText = remember(messageDisplayText) {
                        val cleanHtml = messageDisplayText
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
                    Text(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (message.lastmessage.from != "you" && !message.lastmessage.readed)
                            FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (message.lastmessage.from == "you") {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp).size(16.dp),
                            tint = if (message.lastmessage.readedOpponent)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    } else if (!message.lastmessage.readed) {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(8.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}
fun formatFullDateAndTime(context: Context, dateTimeString: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(dateTimeString, inputFormatter)
        val day = dateTime.dayOfMonth
        val month = dateTime.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        "$day $month $time"
    } catch (e: Exception) { "" }
}