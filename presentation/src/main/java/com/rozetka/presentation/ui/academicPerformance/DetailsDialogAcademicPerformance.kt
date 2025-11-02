package com.rozetka.presentation.ui.academicPerformance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rozetka.model.AcademicPerformanceItem
import com.rozetka.presentation.R

@Composable
fun DetailsDialogAcademicPerformance(item: AcademicPerformanceItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider()
                DetailItem(label = stringResource(R.string.label_grade), value = item.grade.replaceFirstChar { it.uppercase() })
                DetailItem(label = stringResource(R.string.label_exam_type), value = item.examType.replaceFirstChar { it.uppercase() })
                DetailItem(label = stringResource(R.string.label_date), value = item.examDate)
                DetailItem(label = stringResource(R.string.label_teacher), value = item.teacher)
                HorizontalDivider()
                DetailItem(label = stringResource(R.string.label_statement_number), value = item.billNum)
                DetailItem(label = stringResource(R.string.label_department), value = item.chair)
                DetailItem(label = stringResource(R.string.label_year), value = item.year)
                DetailItem(label = stringResource(R.string.label_course), value = item.course)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        }
    )
}
@Composable
private fun DetailItem(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End
            )
        }
    }
}