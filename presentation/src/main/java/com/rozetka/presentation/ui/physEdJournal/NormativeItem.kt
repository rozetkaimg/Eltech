package com.rozetka.presentation.ui.physEdJournal



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rozetka.model.PointsHistory
import com.rozetka.presentation.util.generateColorFromHash

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NormativeItem(item: PointsHistory) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .background(
                        color = generateColorFromHash(item.type).copy(0.15f),
                        shape = Cookie9Sided.toShape()
                    )
                    .size(65.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNormativeIcon(item.type),
                    contentDescription = null,
                    tint = generateColorFromHash(item.type),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getNormativeName(item.type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Результат: ${item.points} б.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = item.teacherFullName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Text(
                    text = "Дата: ${item.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }


            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "+${item.points}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun getNormativeIcon(type: String): ImageVector {
    return when (type) {
        "Tilts" -> Icons.Default.AccessibilityNew // Наклоны
        "Jumps" -> Icons.Default.DirectionsRun    // Прыжки
        "PullUps" -> Icons.Default.FitnessCenter  // Подтягивания (сила)
        "JumpingRopeJumps" -> Icons.Default.Timer // Скакалка (интенсивность)
        "TorsoLifts" -> Icons.Default.Accessibility // Пресс
        else -> Icons.Default.EmojiEvents         // Кубок по умолчанию
    }
}


private fun getNormativeName(type: String): String {
    return when (type) {
        "Tilts" -> "Наклоны"
        "Jumps" -> "Прыжки в длину"
        "PullUps" -> "Подтягивания"
        "JumpingRopeJumps" -> "Прыжки на скакалке"
        "TorsoLifts" -> "Подъем туловища"
        else -> type
    }
}