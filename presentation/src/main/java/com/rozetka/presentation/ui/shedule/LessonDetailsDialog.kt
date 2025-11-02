package com.rozetka.presentation.ui.shedule


import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rozetka.model.Auditory
import com.rozetka.model.Lesson
import com.rozetka.presentation.R
import com.rozetka.presentation.util.getRandomRoundedCornerShape

@Composable
fun LessonDetailsDialog(
    lesson: Lesson,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(shape = RoundedCornerShape(28.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(getRandomRoundedCornerShape())
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.education_outline_28),
                        contentDescription = stringResource(R.string.academic_year_content_description),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.size(16.dp))

                Text(
                    text = lesson.sbj,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))


                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(R.string.label_type))
                            }
                            append(lesson.type)
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (lesson.teacher.isNotBlank()) {
                        val teachers = lesson.teacher.split(",").map { it.trim() }
                        Text(
                            text = if (teachers.size > 1) stringResource(R.string.label_teachers) else stringResource(R.string.label_teacher_single),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        teachers.forEach { teacher ->
                            Text(
                                text = teacher,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }



                    if (lesson.df.isNotBlank() && lesson.dt.isNotBlank()) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.label_dates))
                                }
                                append("${lesson.df} - ${lesson.dt}")
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (lesson.auditories.isNotEmpty()) {
                        AuditoryInfo(auditoryList = lesson.auditories)
                    }
                }



                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.close_button))
                }
            }
        }
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
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.label_auditorium))
                    }
                    append(allTitles)
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.size(12.dp))
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