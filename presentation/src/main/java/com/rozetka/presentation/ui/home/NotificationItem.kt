package com.rozetka.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rozetka.model.NotificationModelItem
import com.rozetka.presentation.R
import com.rozetka.presentation.util.generateColorFromHash
import com.rozetka.presentation.util.getRandomRoundedCornerShape

@Composable
fun NotificationItem(
    item: NotificationModelItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .padding(16.dp)
            ) {
                val iconRes = when (item.type) {
                    "message" -> R.drawable.mail_outline
                    "doc-for-review" -> R.drawable.document_outline_28
                    else -> R.drawable.notifications_28
                }
                
                Box(
                    Modifier
                        .size(128.dp)
                        .clip(getRandomRoundedCornerShape())
                        .background(generateColorFromHash(item.title).copy(alpha = 0.15f))
                        .align(Alignment.Center)
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = generateColorFromHash(item.title),
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 17.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    color = generateColorFromHash(item.title)
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = item.text,
                    fontSize = 14.sp,
                    maxLines = 3,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
