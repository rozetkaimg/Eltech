package com.rozetka.presentation.ui.teacherSchedule

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rozetka.model.LessonS
import com.rozetka.presentation.R
import com.rozetka.presentation.util.getRandomRoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLessonDetailsDialog(
    lesson: LessonS,
    onDismissRequest: () -> Unit,
    onTeacherClick: (String) -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.education_outline_28),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                lesson.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(start = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                lesson.groups?.let {
                    InfoItem(label = stringResource(R.string.group_name_label), value = it)
                }
                if (lesson.teachers.isNotEmpty()) {
                    Column {
                        Text(
                            text = if (lesson.teachers.size > 1) stringResource(R.string.label_teachers)
                            else stringResource(R.string.label_teacher_single),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        lesson.teachers.forEach { teacher ->
                            Surface(
                                onClick = { onTeacherClick(teacher) },
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                            ) {
                                Text(
                                    text = teacher,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                lesson.dateInterval?.let {
                    InfoItem(label = stringResource(R.string.label_dates), value = it)
                }
                AuditoryInfoS(lesson = lesson)
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun AuditoryInfoS(
    lesson: LessonS,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val auditoryText = (lesson.place ?: "") +
            if (lesson.rooms.isNotEmpty()) " (${lesson.rooms.joinToString()})" else ""

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (auditoryText.isNotBlank()) {
            InfoItem(label = stringResource(R.string.label_auditorium), value = auditoryText)
        }

        lesson.link?.let { url ->
            if (url.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        try {
                            uriHandler.openUri(url.trim())
                        } catch (_: Exception) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_open_link_no_app),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.error_open_link_no_app))
                }
            }
        }
    }
}