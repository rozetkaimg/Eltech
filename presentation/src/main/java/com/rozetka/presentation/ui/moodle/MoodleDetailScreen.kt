package com.rozetka.presentation.ui.moodle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rozetka.domain.util.StringObject
import com.rozetka.model.*
import com.rozetka.presentation.ui.pay.LoadingState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodleDetailScreen(
    courseId: String,
    courseTitle: String,
    onBackClick: () -> Unit,
    onModuleClick: (CourseModule) -> Unit = {},
    viewModel: MoodleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Материалы", "Оценки", "Участники")

    LaunchedEffect(courseId) {
        viewModel.loadCourseDetail(courseId)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = courseTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                )
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is MoodleUiState.Loading -> LoadingState()
                is MoodleUiState.DetailSuccess -> {
                    when (selectedTab) {
                        0 -> CourseModulesList(state.sections, onModuleClick)
                        1 -> CourseGradesList(state.grades)
                        2 -> CourseParticipantsList(
                            participants = state.participants,
                            hasMore = state.hasMoreParticipants,
                            isLoadingMore = state.isLoadingMore,
                            onLoadMore = {
                                viewModel.loadNextParticipantsPage(courseId)
                            }
                        )
                    }
                }
                is MoodleUiState.Error -> ErrorState(state.message) {
                    viewModel.loadCourseDetail(courseId)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun CourseModulesList(
    sections: List<CourseSection>,
    onModuleClick: (CourseModule) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(sections) { section ->
            Column {
                if (section.name.isNotEmpty()) {
                    Text(
                        text = section.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    section.modules.forEach { module ->
                        ModuleItemExpressive(module) {
                            onModuleClick(module)
                            if (module.link.isNotEmpty() && module.type != ModuleType.QUIZ) {
                                uriHandler.openUri(module.link)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CourseGradesList(grades: List<GradeItem>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(grades) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(Cookie9Sided.toShape())
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        if (item.range.isNotEmpty()) {
                            Text("Диапазон: ${item.range}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(
                        text = item.grade.ifEmpty { "-" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CourseParticipantsList(
    participants: List<ParticipantItem>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(participants.size) { index ->
            val person = participants[index]

            if (index == participants.lastIndex && hasMore && !isLoadingMore) {
                LaunchedEffect(index) {
                    onLoadMore()
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = person.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(Cookie9Sided.toShape()),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(person.fullName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(person.roles, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(person.lastAccess, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        if (isLoadingMore) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModuleItemExpressive(module: CourseModule, onClick: () -> Unit) {
    val theme = getModuleTheme(module.type)
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(Cookie9Sided.toShape())
                    .background(theme.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(theme.icon, null, tint = theme.color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(module.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(theme.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                if (module.isCompleted) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                null,
                tint = if (module.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

private data class ModuleTheme(val icon: ImageVector, val color: Color, val label: String)

@Composable
private fun getModuleTheme(type: ModuleType) = when (type) {
    ModuleType.RESOURCE -> ModuleTheme(Icons.Default.Description, Color(0xFF2196F3), "Ресурс")
    ModuleType.ASSIGN -> ModuleTheme(Icons.Default.Assignment, Color(0xFFFF9800), "Задание")
    ModuleType.QUIZ -> ModuleTheme(Icons.Default.Quiz, Color(0xFFE91E63), "Тест")
    ModuleType.FORUM -> ModuleTheme(Icons.Default.Forum, Color(0xFF9C27B0), "Форум")
    ModuleType.FOLDER -> ModuleTheme(Icons.Default.Folder, Color(0xFF795548), "Папка")
    ModuleType.PAGE -> ModuleTheme(Icons.Default.Web, Color(0xFF00BCD4), "Страница")
    ModuleType.URL -> ModuleTheme(Icons.Default.Link, Color(0xFF4CAF50), "Ссылка")
    ModuleType.UNKNOWN -> ModuleTheme(Icons.Default.HelpOutline, Color.Gray, "Материал")
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) { Text("Повторить") }
    }
}