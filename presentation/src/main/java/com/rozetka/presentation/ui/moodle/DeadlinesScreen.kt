package com.rozetka.presentation.ui.moodle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rozetka.model.MoodleDeadline
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.generateColorFromHash
import com.rozetka.presentation.util.getNavigationBarHeightDp
import com.rozetka.presentation.util.getSubjectIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlinesScreen(
    onBackClick: () -> Unit,
    viewModel: DeadlinesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Дедлайны", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadCurrentMonth() }) {
                        Icon(Icons.Default.Today, contentDescription = "Текущий месяц")
                    }
                    IconButton(onClick = { viewModel.loadPreviousMonth() }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Прошлый месяц")
                    }
                    IconButton(onClick = { viewModel.loadNextMonth() }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Следующий месяц")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is DeadlinesUiState.Loading -> LoadingState()
                is DeadlinesUiState.Success -> DeadlinesList(state.deadlines)
                is DeadlinesUiState.Error -> EmptyState(state.message) { viewModel.loadDeadlines() }
            }
        }
    }
}

@Composable
private fun DeadlinesList(deadlines: List<MoodleDeadline>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(deadlines, key = { it.id }) { deadline ->
            DeadlineExpressiveCard(deadline = deadline)
        }
        item {
            Spacer(Modifier.size(getNavigationBarHeightDp() + 80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeadlineExpressiveCard(deadline: MoodleDeadline) {
    val uriHandler = LocalUriHandler.current

    val isUrgent = deadline.formattedDate.contains("сегодня", ignoreCase = true) ||
            deadline.formattedDate.contains("завтра", ignoreCase = true)

    val subjectColor = generateColorFromHash(deadline.name)
    val subjectIcon = getSubjectIcon(deadline.name)

    val containerColor = if (isUrgent) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.surfaceContainerHigh

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = { uriHandler.openUri(deadline.url) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(Cookie9Sided.toShape())
                    .background(subjectColor.copy(alpha = 0.15f))
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subjectIcon,
                    contentDescription = null,
                    tint = subjectColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deadline.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = deadline.courseName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BadgeInfo(
                        icon = Icons.Rounded.Schedule,
                        label = deadline.formattedDate,
                        containerColor = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isUrgent) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeInfo(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String,
    containerColor: Color,
    contentColor: Color = contentColorFor(containerColor)
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Event,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Попробовать снова")
        }
    }
}