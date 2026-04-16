package com.rozetka.presentation.ui.groupJournal

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rozetka.model.Student
import com.rozetka.presentation.R
import com.rozetka.presentation.util.generateColorFromHash

@Composable
fun StudentCard(
    student: Student,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )

    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = generateColorFromHash(student.fullName).copy(0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = student.fullName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                color = generateColorFromHash(student.fullName)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (student.hasDebt) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.group_journal_debt_badge),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    InfoColumn(label = stringResource(R.string.group_journal_visits), value = student.visits.toString())
                    VerticalDivider(modifier = Modifier.height(32.dp).align(Alignment.CenterVertically))
                    InfoColumn(label = stringResource(R.string.group_journal_points), value = student.totalPoints.toString())
                    VerticalDivider(modifier = Modifier.height(32.dp).align(Alignment.CenterVertically))
                    InfoColumn(label = stringResource(R.string.group_journal_lms), value = student.lmsPoints.toString())
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Нижняя панель с группой здоровья
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.education_outline_28),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = translateHealthGroup(student.healthGroup),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}




@Composable
fun VerticalDivider(modifier: Modifier) {
    androidx.compose.material3.VerticalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun translateHealthGroup(group: String): String {
    return when (group) {
        "Basic" -> stringResource(R.string.group_journal_health_group_basic)
        "Preparatory" -> stringResource(R.string.group_journal_health_group_preparatory)
        "SpecialA" -> stringResource(R.string.group_journal_health_group_special_a)
        "SpecialB" -> stringResource(R.string.group_journal_health_group_special_b)
        "None" -> stringResource(R.string.group_journal_health_group_none)
        else -> group
    }
}

@Composable
fun ErrorState(message: String, onUpdate: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onUpdate) {
            Text(stringResource(R.string.try_again))
        }
    }
}