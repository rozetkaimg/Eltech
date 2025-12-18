package com.rozetka.presentation.ui.academicPerformance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rozetka.model.AcademicPerformanceItem
import com.rozetka.presentation.R
import com.rozetka.presentation.util.getRandomRoundedCornerShape

@Composable
fun DetailsDialogAcademicPerformance(item: AcademicPerformanceItem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(getRandomRoundedCornerShape()) // Или RoundedCornerShape(20.dp), если утилита недоступна
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        // Используем ту же иконку или замените на более подходящую для оценок, например R.drawable.ic_grade
                        painter = painterResource(R.drawable.education_outline_28),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.size(16.dp))

                // --- Заголовок предмета ---
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Список деталей ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Оценка
                    PerformanceInfoItem(
                        label = stringResource(R.string.label_grade),
                        value = item.grade.replaceFirstChar { it.uppercase() }
                    )

                    PerformanceInfoItem(
                        label = stringResource(R.string.label_exam_type),
                        value = item.examType.replaceFirstChar { it.uppercase() }
                    )
                    PerformanceInfoItem(
                        label = stringResource(R.string.label_date),
                        value = item.examDate
                    )
                    PerformanceInfoItem(
                        label = stringResource(R.string.label_teacher),
                        value = item.teacher
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PerformanceInfoItem(
                        label = stringResource(R.string.label_statement_number),
                        value = item.billNum
                    )

                    PerformanceInfoItem(
                        label = stringResource(R.string.label_department),
                        value = item.chair
                    )
                    PerformanceInfoItem(
                        label = stringResource(R.string.label_year),
                        value = item.year
                    )
                    PerformanceInfoItem(
                        label = stringResource(R.string.label_course),
                        value = item.course
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.common_close))
                }
            }
        }
    }
}

@Composable
private fun PerformanceInfoItem(label: String, value: String) {
    if (value.isNotBlank()) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$label: ")
                }
                append(value)
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start
        )
    }
}