package com.rozetka.presentation.ui.academicPerformance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rozetka.model.AcademicPerformanceItem
import com.rozetka.presentation.R
import com.rozetka.presentation.util.formatToRussianDate // Импортируй созданную функцию
import com.rozetka.presentation.util.generateColorFromHash
import com.rozetka.presentation.util.getSubjectIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsBottomSheetAcademicPerformance(
    item: AcademicPerformanceItem,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(generateColorFromHash(item.name).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getSubjectIcon(item.name),

                        contentDescription = null,
                        tint = generateColorFromHash(item.name),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 16.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Список всех данных из оригинала ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Оценка
                InfoItem(
                    label = stringResource(R.string.label_grade),
                    value = item.grade.replaceFirstChar { it.uppercase() }
                )

                // 2. Тип контроля
                InfoItem(
                    label = stringResource(R.string.label_exam_type),
                    value = item.examType.replaceFirstChar { it.uppercase() }
                )

                // 3. Дата (с форматированием)
                InfoItem(
                    label = stringResource(R.string.label_date),
                    value = formatToRussianDate(item.examDate)
                )

                // 4. Преподаватель
                if (item.teacher.isNotBlank()) {
                    InfoItem(
                        label = stringResource(R.string.label_teacher),
                        value = item.teacher
                    )
                }

                // 5. Номер ведомости
                InfoItem(
                    label = stringResource(R.string.label_statement_number),
                    value = item.billNum
                )

                InfoItem(
                    label = stringResource(R.string.label_department),
                    value = item.chair
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        InfoItem(label = stringResource(R.string.label_year), value = item.year)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        InfoItem(label = stringResource(R.string.label_course), value = item.course)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

           
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_close))
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    if (value.isNotBlank()) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}