package com.rozetka.presentation.ui.sessionSchedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rozetka.model.ScheduleModel
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.ui.shedule.LessonItem
import com.rozetka.presentation.util.getNavigationBarHeightDp
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScheduleScreen(
    navController: NavHostController,
    groupName: String,
    viewModel: SessionScheduleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(groupName) {
        viewModel.getSessionSchedule(groupName)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.session_schedule_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            color = MaterialTheme.colorScheme.surface
        ) {
            when (val state = uiState) {
                is SessionScheduleUiState.Loading -> LoadingState()
                is SessionScheduleUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message)
                    }
                }
                is SessionScheduleUiState.Success -> {
                    SessionContent(state.data, navController)
                }
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SessionContent(schedule: ScheduleModel, navController: NavHostController) {
    val dayNameFormatter = remember { DateTimeFormatter.ofPattern("EEEE", Locale("ru")) }
    val dateNumFormatter = remember { DateTimeFormatter.ofPattern("dd.MM", Locale("ru")) }

    val sortedDays = remember(schedule) {
        schedule.grid.entries
            .mapNotNull { entry ->
                try {
                    LocalDate.parse(entry.key) to entry.value
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.first }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        sortedDays.forEach { (date, timeMap) ->
            item(key = "header_${date}") {
                val dayName = date.format(dayNameFormatter)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
                val formattedDate = date.format(dateNumFormatter)

                Text(
                    text = "$dayName, $formattedDate",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
            }

            val dayLessons = timeMap.flatMap { (timeKey, lessons) ->
                lessons.map { it to timeKey }
            }

            if (dayLessons.isEmpty()) {
                item(key = "empty_${date}") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .clip(Cookie9Sided.toShape())
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.home_outline),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp).align(Alignment.Center)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = stringResource(R.string.weekend_text_line1),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = stringResource(R.string.weekend_text_line2),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(dayLessons) { index, (lesson, timeKey) ->
                    SessionLessonItem(
                        lesson = lesson,
                        lessonNumber = timeKey,
                        index = index,
                        totalLessonsInDay = dayLessons.size,
                        lessonDate = date,
                        onLessonClick = {
                            if (it.teacher.isNotBlank()) {
                                navController.navigate("teacherSchedule/${it.teacher}")
                            }
                        }
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(getNavigationBarHeightDp() + 80.dp + 16.dp))
        }
    }
}