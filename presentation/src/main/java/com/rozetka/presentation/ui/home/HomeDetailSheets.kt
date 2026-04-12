package com.rozetka.presentation.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rozetka.model.ExternalNewsItem
import com.rozetka.model.NewsModelItem
import com.rozetka.model.NotificationModelItem
import com.rozetka.model.PolytechEvent
import com.rozetka.presentation.R
import com.rozetka.presentation.util.generateColorFromHash

@Composable
fun NotificationsListSheet(
    notifications: List<NotificationModelItem>,
    onNotificationClick: (NotificationModelItem) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Уведомления",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
        )

        if (notifications.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center) {
                Text("У вас пока нет уведомлений", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                notifications.forEach { notification ->
                    NotificationItem(
                        item = notification,
                        onClick = { onNotificationClick(notification) }
                    )
                }
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun NotificationDetailSheet(notification: NotificationModelItem) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val iconRes = when (notification.type) {
            "message" -> R.drawable.mail_outline
            "doc-for-review" -> R.drawable.document_outline_28
            else -> R.drawable.notifications_28
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(generateColorFromHash(notification.title).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = generateColorFromHash(notification.title),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                notification.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = generateColorFromHash(notification.title)
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = notification.text,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 26.sp
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Перейти", fontSize = 16.sp) }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun NewsDetailSheet(newsItem: NewsModelItem) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(newsItem.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("${newsItem.date} в ${newsItem.time}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
        val imageUrl = extractImageUrl(newsItem.content)
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .aspectRatio(16f / 9f),
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
fun EventDetailSheet(event: PolytechEvent) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(event.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(event.date, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        AsyncImage(
            model = event.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .aspectRatio(16f / 9f),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Зарегистрироваться", fontSize = 16.sp) }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun ExternalNewsDetailSheet(item: ExternalNewsItem) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(item.date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
        AsyncImage(
            model = item.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .aspectRatio(16f / 9f),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(20.dp))
        Text(item.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Читать полностью", fontSize = 16.sp) }
        Spacer(Modifier.height(48.dp))
    }
}
