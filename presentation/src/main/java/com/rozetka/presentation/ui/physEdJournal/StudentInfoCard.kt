package com.rozetka.presentation.ui.physEdJournal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rozetka.presentation.R
import com.rozetka.presentation.util.generateColorFromHash

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StudentInfoCard(
    fullName: String,
    group: String,
    course: Int,
    totalPoints: Int,
    lms: Int,
    curator: String,
    healthGroup: String,
    specialization: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(Cookie9Sided.toShape())
                        .background(generateColorFromHash(fullName).copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.user_outline),
                        contentDescription = null,
                        tint = generateColorFromHash(fullName),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = group,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            InfoGrid(
                course = course,
                specialization = specialization,
                curator = curator,
                healthGroup = healthGroup
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PointBadge(
                    label = "ЛМС",
                    points = lms.toString(),
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                PointBadge(
                    label = "ВСЕГО",
                    points = totalPoints.toString(),
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    isHighlight = true
                )
            }
        }
    }
}

@Composable
private fun InfoGrid(
    course: Int,
    specialization: String,
    curator: String,
    healthGroup: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth()) {
            InfoItem(label = "Курс", value = "$course", modifier = Modifier.weight(1f))
            if (specialization != "None") {
                InfoItem(label = "Спец.", value = specialization, modifier = Modifier.weight(1.5f))
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        InfoItem(label = "Куратор", value = curator)

        val healthGroupText = when (healthGroup) {
            "Basic" -> "Основная"
            "SpecialA" -> "Специальная А"
            "SpecialB" -> "Специальная Б"
            "Preparatory" -> "Подготовительная"
            "HealthLimitations" -> "ОВЗ"
            "Disabled" -> "Инвалид"
            else -> null
        }

        healthGroupText?.let {
            InfoItem(label = "Группа здоровья", value = it)
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PointBadge(
    label: String,
    points: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    isHighlight: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.8f)
            )
            Text(
                text = points,
                style = if (isHighlight) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
        }
    }
}