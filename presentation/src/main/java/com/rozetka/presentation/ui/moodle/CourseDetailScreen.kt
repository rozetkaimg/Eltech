package com.rozetka.presentation.ui.moodle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rozetka.model.CourseModule
import com.rozetka.model.CourseSection
import com.rozetka.model.ModuleType
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.getNavigationBarHeightDp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseTitle: String,
    onBackClick: () -> Unit,
    viewModel: CourseDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(courseTitle, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is CourseDetailUiState.Loading -> LoadingState()
                is CourseDetailUiState.Success -> CourseContentList(state.sections)
                is CourseDetailUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun CourseContentList(sections: List<CourseSection>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(sections) { section ->
            Column {
                Text(
                    text = section.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                section.modules.forEach { module ->
                    ModuleItem(module)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        item { Spacer(Modifier.height(getNavigationBarHeightDp() + 80.dp)) }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModuleItem(module: CourseModule) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable { },
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(Cookie9Sided.toShape())
                    .background(getModuleColor(module.type).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getModuleIcon(module.type),
                    contentDescription = null,
                    tint = getModuleColor(module.type),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = if (module.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (module.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun getModuleIcon(type: ModuleType): ImageVector = when (type) {
    ModuleType.RESOURCE -> Icons.Default.Description
    ModuleType.ASSIGN -> Icons.Default.Assignment
    ModuleType.QUIZ -> Icons.Default.Quiz
    ModuleType.FORUM -> Icons.Default.Forum
    ModuleType.FOLDER -> Icons.Default.Folder
    ModuleType.PAGE -> Icons.Default.Article
    ModuleType.URL -> Icons.Default.Link
    ModuleType.UNKNOWN -> Icons.Default.HelpOutline
}

@Composable
private fun getModuleColor(type: ModuleType) = when (type) {
    ModuleType.QUIZ -> MaterialTheme.colorScheme.error
    ModuleType.ASSIGN -> MaterialTheme.colorScheme.tertiary
    ModuleType.RESOURCE, ModuleType.PAGE -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.secondary
}