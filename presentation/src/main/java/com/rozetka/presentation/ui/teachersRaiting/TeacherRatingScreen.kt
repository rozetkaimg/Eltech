package com.rozetka.presentation.ui.teachersRaiting


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.rozetka.model.campus.TeacherResponse
import com.rozetka.presentation.R
import com.rozetka.presentation.util.CollapsingToolbarScaffold
import com.rozetka.presentation.util.ScrollStrategy
import com.rozetka.presentation.util.getNavigationBarHeightDp
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
            division = division
        )

        is TeacherRatingUiState.Error -> ErrorState(
            message = state.message,
            onUpdate = { viewModel.getTeacherRating(teacherId) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(Modifier.size(48.dp))
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
            scrollStrategy = ScrollStrategy.EnterAlwaysCollapsed,
            toolbar = {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
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
                                            modifier = Modifier.size(96.dp).padding(16.dp)
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
                TeacherDataLayout(data = state.data, {
                    navController.navigate("teacherSchedule/${state.data.teacher.name}")
                },division)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeacherDataLayout(data: TeacherResponse, onScheduleClick: () -> Unit, division: String) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Button(
                onClick = onScheduleClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Расписание сотрудника")
            }
        }

        item {

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 28.dp,
                    bottomStart = 8.dp,
                    bottomEnd = 8.dp
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
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomEnd = 28.dp,
                    bottomStart = 28.dp
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

                    data.teacher.rating.criteria.forEach {
                        InfoRowSmall(it.title, String.format("%.2f", it.value))
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

        }
        item {
            Text(
                text = "Отзывы ${ if(data.teacher.rating.count > 0 ){
                    data.teacher.rating.count.toString()
                } else {
                    
                }
                }" ,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        item {
            Button(
                onClick = onScheduleClick,
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
                ReviewItem(review)
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

            item { Spacer(Modifier.size(getNavigationBarHeightDp() + 80.dp)) }

    }

}

@Composable
fun ReviewItem(review: com.rozetka.model.campus.Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape( 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = review.author ?: "Аноним",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = if (review.value >= 4.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary) {
                        Text("Оценка: ${review.value}", modifier = Modifier.padding(4.dp))
                    }
                }

            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.createdAt.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
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