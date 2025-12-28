package com.rozetka.presentation.ui.shedule

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.rozetka.model.Auditory
import com.rozetka.model.Lesson
import com.rozetka.presentation.R
import com.rozetka.presentation.util.formatLessonDates
import com.rozetka.presentation.util.generateColorFromHash
import com.rozetka.presentation.util.getRandomRoundedCornerShape
import com.rozetka.presentation.util.getSubjectIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailsBottomSheet(
    lesson: Lesson,
    onDismissRequest: () -> Unit,
    onLinkClick: (String) -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                val baseColor = generateColorFromHash(lesson.sbj)

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))

                        .background(baseColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getSubjectIcon(lesson.sbj),
                        contentDescription = null,
                        tint = baseColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Text(
                    text = lesson.sbj,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 16.dp),
                    fontWeight = FontWeight.Bold
                )}
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(label = stringResource(R.string.label_type), value = lesson.type)

                if (lesson.teacher.isNotBlank()) {
                    val teachers = lesson.teacher.split(",").map { it.trim() }
                    Column {
                        Text(
                            text = if (teachers.size > 1) stringResource(R.string.label_teachers)
                            else stringResource(R.string.label_teacher_single),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        teachers.forEach { teacher ->
                            Surface(
                                onClick = { onLinkClick(teacher) },
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

                if (lesson.df.isNotBlank() && lesson.dt.isNotBlank()) {
                    InfoItem(
                        label = stringResource(R.string.label_dates),
                        value = formatLessonDates( "${lesson.df} - ${lesson.dt}")
                    )
                }

                if (lesson.auditories.isNotEmpty()) {
                    AuditoryInfo(auditoryList = lesson.auditories)
                }
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
fun AuditoryInfo(
    auditoryList: List<Auditory>,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val regex = "<a\\s+href=\"([^\"]+)\".*?>(.*?)</a>".toRegex()

    val (linkAuditories, textAuditories) = auditoryList.partition {
        regex.containsMatchIn(it.title)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (textAuditories.isNotEmpty()) {
            val allTitles = textAuditories.joinToString(separator = ", ") { it.title }
            InfoItem(label = stringResource(R.string.label_auditorium), value = allTitles)
        }

        linkAuditories.forEach { auditory ->
            regex.find(auditory.title)?.let { matchResult ->
                val (url, text) = matchResult.destructured
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text)
                }
            }
        }
    }
}