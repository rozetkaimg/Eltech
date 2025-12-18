package com.rozetka.presentation.ui.teacherReview


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rozetka.model.campus.Criteria
import com.rozetka.model.campus.ReviewOptions
import com.rozetka.model.campus.Tag
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.UiSize
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherReviewScreen(
    navController: NavController,
    teacherId: String,
    viewModel: TeacherReviewViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val submissionState by viewModel.submissionState.collectAsState()

    val comment by viewModel.comment.collectAsState()
    val ratings by viewModel.selectedRatings.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()

    LaunchedEffect(teacherId) {
        viewModel.loadFields(teacherId)
    }

    if (submissionState is ReviewSubmissionState.Success) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetSubmissionState()
                navController.popBackStack()
            },
            icon = { Icon(Icons.Default.Check, contentDescription = null) },
            title = { Text("Отзыв отправлен") },
            text = { Text("Ваш комментарий появится в списке после проверки модератором.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSubmissionState()
                        navController.popBackStack()
                    }
                ) {
                    Text("Хорошо")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новый отзыв") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            when (val state = uiState) {
                is ReviewFieldsUiState.Loading -> LoadingState()
                is ReviewFieldsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is ReviewFieldsUiState.Success -> {
                    ReviewFormContent(
                        options = state.data,
                        ratings = ratings,
                        selectedTags = selectedTags,
                        comment = comment,
                        isSubmitting = submissionState is ReviewSubmissionState.Loading,
                        onRatingChanged = viewModel::updateRating,
                        onTagToggled = viewModel::toggleTag,
                        onCommentChanged = viewModel::updateComment,
                        onSubmit = { viewModel.submitReview(teacherId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewFormContent(
    options: ReviewOptions,
    ratings: Map<String, Int>,
    selectedTags: Set<String>,
    comment: String,
    isSubmitting: Boolean,
    onRatingChanged: (String, Int) -> Unit,
    onTagToggled: (String) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Оцените преподавателя",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(options.criteria) { criterion ->
            CriterionInputItem(
                criterion = criterion,
                currentValue = ratings[criterion.id] ?: 5,
                onValueChange = { onRatingChanged(criterion.id, it) }
            )
        }

        item { HorizontalDivider() }

        item {
            Text(
                text = "Что выделите?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.tags.forEach { tag ->
                    FilterChip(
                        selected = selectedTags.contains(tag.id),
                        onClick = { onTagToggled(tag.id) },
                        label = { Text(tag.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        item { HorizontalDivider() }

        item {
            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChanged,
                label = { Text("Ваш комментарий") },
                placeholder = { Text("Расскажите подробнее о преподавателе...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Оставить отзыв")
                }
            }
            Spacer(modifier = Modifier.height(UiSize().getNavBarPaddingSize()-8.dp))
        }
    }
}

@Composable
fun CriterionInputItem(
    criterion: Criteria,
    currentValue: Int,
    onValueChange: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = criterion.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$currentValue/5",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (criterion.description.isNotEmpty()) {
                Text(
                    text = criterion.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Slider(
                value = currentValue.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}