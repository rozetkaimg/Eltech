package com.rozetka.presentation.ui.teachersRaiting

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.rozetka.model.campus.Criterion
import com.rozetka.model.campus.Review
import com.rozetka.model.campus.TeacherResponse
import com.rozetka.model.campus.TeacherTag
import com.rozetka.presentation.R
import com.rozetka.presentation.new.CalendarOutline28
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.CollapsingToolbarScaffold
import com.rozetka.presentation.util.ScrollStrategy
import com.rozetka.presentation.util.UiSize
import com.rozetka.presentation.util.rememberCollapsingToolbarScaffoldState
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeacherRatingScreen(
    navController: NavController,
    teacherId: String,
    avatar: String,
    division: String,
    email: String,
    fio: String,
    id: String,
    post: String,
    viewModel: TeacherRatingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(teacherId) {
        viewModel.getTeacherRating(teacherId)
    }

    when (val state = uiState) {
        is TeacherRatingUiState.Loading -> LoadingState()
        is TeacherRatingUiState.Success -> TeacherRatingSuccessState(
            state = state,
            navController = navController,
            avatar = avatar,
            division = division,
            onReactionClick = { reviewId, reaction ->
                viewModel.setReaction(reviewId, reaction, teacherId)
            }
        )

        is TeacherRatingUiState.Error -> ErrorState(
            message = state.message,
            onUpdate = { viewModel.getTeacherRating(teacherId) }
        )
    }
}

@Composable
private fun ErrorState(message: String, onUpdate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onUpdate) {
            Text(stringResource(R.string.retry_button))
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TeacherRatingSuccessState(
    navController: NavController,
    state: TeacherRatingUiState.Success,
    avatar: String,
    division: String,
    onReactionClick: (String, String) -> Unit
) {
    val states = rememberCollapsingToolbarScaffoldState()
    val currentRadius = 30.dp * states.toolbarState.progress
    val collapsedColor = MaterialTheme.colorScheme.surface
    val expandedColor = MaterialTheme.colorScheme.primaryContainer
    val dynamicContainerColor = lerp(
        start = collapsedColor,
        stop = expandedColor,
        fraction = states.toolbarState.progress
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.data.teacher.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.alpha(1f - states.toolbarState.progress)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = dynamicContainerColor,
                    scrolledContainerColor = dynamicContainerColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )
        }
    ) {
        CollapsingToolbarScaffold(
            modifier = Modifier
                .background(dynamicContainerColor)
                .padding(top = it.calculateTopPadding()),
            state = states,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            toolbar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                        .pin()
                        .background(dynamicContainerColor)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .road(Alignment.Center, Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .alpha(states.toolbarState.progress),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            SubcomposeAsyncImage(
                                model = avatar,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(Cookie9Sided.toShape()),
                                loading = {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        LoadingIndicator()
                                    }
                                },
                                error = {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.onPrimary)
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.ic_profile),
                                            contentDescription = stringResource(R.string.cd_profile_photo_placeholder),
                                            modifier = Modifier.size(150.dp).padding(16.dp)
                                        )
                                    }
                                }
                            )
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.align(Alignment.BottomEnd)) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(
                                        text = String.format("%.2f", state.data.teacher.rating.value),
                                        modifier = Modifier.padding(4.dp),
                                        fontSize = 32.sp,
                                        style = MaterialTheme.typography.labelLargeEmphasized
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = state.data.teacher.name,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 25.sp,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                    }
                }
            }) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = currentRadius, topEnd = currentRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                TeacherDataLayout(
                    data = state.data,
                    onScheduleClick = {
                        navController.navigate("teacherSchedule/${state.data.teacher.name}")
                    },
                    onAddReviewClick = {

                        navController.navigate("teacherReview/${state.data.teacher.id}")
                    },
                    division = division,
                    onReactionClick = onReactionClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeacherDataLayout(
    data: TeacherResponse,
    onScheduleClick: () -> Unit,
    onAddReviewClick: () -> Unit,
    division: String,
    onReactionClick: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Button(
                onClick = onScheduleClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(CalendarOutline28, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Расписание сотрудника")
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 4.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(Cookie9Sided.toShape())
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(R.string.academic_year_content_description),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "О сотруднике",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.size(2.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 4.dp,
                    bottomEnd = 24.dp,
                    bottomStart = 24.dp
                ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = division,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    data.teacher.rating.criteria.forEach { criterion ->
                        CriterionItem(criterion)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
        item {
            QualitiesSection(tags = data.teacher.rating.tags)
        }
        item {
            if (data.reviews.isNotEmpty()) {
                Text(
                    text = "Отзывы ${
                        if (data.teacher.rating.count > 0) {
                            data.teacher.rating.count.toString()
                        } else {
                            ""
                        }
                    }",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        item {
            Button(
                onClick = onAddReviewClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить отзыв")
            }
        }
        if (data.reviews.isNotEmpty()) {
            items(data.reviews) { review ->
                ReviewItem(
                    review = review,
                    onReactionClick = onReactionClick
                )
            }
        } else {
            item {
                Text(
                    text = "Отзывов пока нет",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        item { Spacer(Modifier.height(UiSize().getNavBarPaddingSize())) }
    }
}


private const val MAX_RATING_VALUE = 5.0
@Composable
fun CriterionItem(criterion: Criterion) {
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = criterion.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.5f),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            LinearProgressIndicator(
                progress = { (criterion.value / MAX_RATING_VALUE).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .width(80.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (criterion.value >= 4.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = String.format("%.1f", criterion.value),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewItem(
    review: Review,
    onReactionClick: (String, String) -> Unit
) {
    var tagsExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = 8.dp,
            bottomEnd = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.author ?: "Аноним",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Badge(
                containerColor = if (review.value >= 4.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = String.format("%.1f", review.value),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (review.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .animateContentSize(),
                        maxLines = if (tagsExpanded) Int.MAX_VALUE else 1,
                        overflow = FlowRowOverflow.Clip
                    ) {
                        review.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(tag) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                border = null,
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { tagsExpanded = !tagsExpanded },
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (tagsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle tags",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp,
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ReactionCounter(
                    icon = Icons.Outlined.ThumbUp,
                    count = review.reactions.likes,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { onReactionClick(review.id, "like") }
                )
                Spacer(modifier = Modifier.width(16.dp))
                ReactionCounter(
                    icon = Icons.Outlined.ThumbDown,
                    count = review.reactions.dislikes,
                    color = MaterialTheme.colorScheme.error,
                    onClick = { onReactionClick(review.id, "dislike") }
                )
            }

            Text(
                text = review.createdAt.take(10),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun ReactionCounter(
    icon: ImageVector,
    count: Int,
    color: Color,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
@Composable
private fun InfoRowSmall(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label + ":",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.2f),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QualitiesSection(tags: List<TeacherTag>) {
    if (tags.isEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Text(
            text = "Качества и особенности",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = FlowRowOverflow.Clip
        ) {
            tags.forEach { tag ->
                QualityChip(tag = tag)
            }
        }
        if (tags.size > 6) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Text(text = if (isExpanded) "Свернуть" else "Показать все качества")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun QualityChip(tag: TeacherTag) {
    val containerColor = if (tag.percentage > 50) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val contentColor = if (tag.percentage > 50) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = tag.title,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.width(6.dp))


            Text(
                text = "${tag.percentage}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}