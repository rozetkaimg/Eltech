package com.rozetka.presentation.ui.academicPerformance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes.Companion.Clover8Leaf
import androidx.compose.material3.MaterialShapes.Companion.Cookie7Sided
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialShapes.Companion.Gem
import androidx.compose.material3.MaterialShapes.Companion.VerySunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rozetka.model.AcademicPerformanceItem
import com.rozetka.presentation.R
import com.rozetka.presentation.util.AutoSizingText
import com.rozetka.presentation.util.formatLocalizedDate

private object GradeStrings {
    const val EXCELLENT = "отлично"
    const val PASSED = "зачтено"
    const val GOOD = "хорошо"
    const val SATISFACTORY = "удовлетворительно"
    const val UNSATISFACTORY = "неудовлетворительно"
    const val FAILED = "не зачтено"
    const val NO_SHOW = "Не явился"
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private data class GradeDisplayInfo(
    val text: String,
    val color: Color,
    val shape: Shape,
    val icon: ImageVector? = null // Добавили поле для иконки
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun getGradeDisplayInfo(grade: String): GradeDisplayInfo {
    val unknownGradeText = stringResource(R.string.lesson_type_unknown)
    val lockedColor = Color(0xFFBDBDBD)
    val defaultColor = MaterialTheme.colorScheme.primary
    if (grade.isBlank()) {
        return GradeDisplayInfo(
            text = "",
            color = lockedColor,
            shape = Gem.toShape(),
            icon = Icons.Rounded.Lock
        )
    }

    return when (grade.lowercase()) {
        GradeStrings.EXCELLENT -> GradeDisplayInfo(
            text = "5",
            color = Color(0xFF4CAF50),
            shape = Cookie9Sided.toShape()
        )
        GradeStrings.PASSED -> GradeDisplayInfo(
            text = "✓",
            color = Color(0xFF4CAF50),
            shape = Clover8Leaf.toShape()
        )
        GradeStrings.GOOD -> GradeDisplayInfo(
            text = "4",
            color = Color(0xFF8BC34A),
            shape = Cookie7Sided.toShape()
        )
        GradeStrings.SATISFACTORY -> GradeDisplayInfo(
            text = "3",
            color = Color(0xFFFFC107),
            shape = VerySunny.toShape()
        )
        GradeStrings.UNSATISFACTORY,
        GradeStrings.FAILED,
        GradeStrings.NO_SHOW -> GradeDisplayInfo(
            text = "✖",
            color = Color(0xFFF44336),
            shape = Gem.toShape()
        )
        else -> GradeDisplayInfo(
            text = unknownGradeText,
            color = defaultColor,
            shape = Gem.toShape()
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AcademicPerformanceCard(item: AcademicPerformanceItem, onClick: () -> Unit) {
    val gradeInfo = getGradeDisplayInfo(grade = item.grade)
    val isEnabled = item.grade.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        enabled = isEnabled,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                )
                Spacer(Modifier.width(8.dp))

                // Блок с оценкой или замком
                Box(
                    modifier = Modifier
                        .background(gradeInfo.color, gradeInfo.shape)
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (gradeInfo.icon != null) {
                        // Рисуем иконку замка
                        Icon(
                            imageVector = gradeInfo.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        // Рисуем текст оценки
                        AutoSizingText(
                            text = gradeInfo.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.size(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.label_exam_type),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        item.examType.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.label_date),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = formatLocalizedDate(
                            LocalContext.current, item.examDate
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.label_teacher_card),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Text(
                text = if (item.teacher.isNotBlank()) item.teacher else "—",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}