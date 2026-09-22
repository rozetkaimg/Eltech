package com.rozetka.presentation.ui.projectActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialShapes.Companion.Cookie12Sided
import androidx.compose.material3.MaterialShapes.Companion.Cookie4Sided
import androidx.compose.material3.MaterialShapes.Companion.Cookie7Sided
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.PDModel
import com.rozetka.model.ProjectSheetItem
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.UiSize
import com.rozetka.presentation.util.generateColorFromHash
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectActivityScreen(
    navController: NavController,
    viewModel: ProjectActivityViewModel = koinViewModel()
) {
    val stateTop = rememberTopAppBarState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(stateTop)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.project_activity), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ProjectActivityUiState.Loading -> LoadingState()
                is ProjectActivityUiState.Success -> ProjectActivitySuccessState(
                    navController = navController,
                    data = state.projectData,
                    scheduleItems = state.scheduleItems,
                    isLoadingSchedule = state.isLoadingSchedule,
                    onRefreshSchedule = viewModel::refreshSchedule
                )
                is ProjectActivityUiState.Error -> ExpressiveErrorState(
                    message = state.message,
                    onUpdate = { viewModel.loadProjectData() }
                )
            }
        }
    }
}

@Composable
private fun ProjectActivitySuccessState(
    navController: NavController,
    data: PDModel,
    scheduleItems: List<ProjectSheetItem>,
    isLoadingSchedule: Boolean,
    onRefreshSchedule: () -> Unit
) {
    val studentProject = remember(data.project) { data.project.trim() }

    val myProjectItem = remember(scheduleItems, studentProject) {
        if (studentProject.isEmpty()) null
        else scheduleItems.find { item ->
            item.projectTitle.contains(studentProject, ignoreCase = true) ||
                studentProject.contains(item.projectTitle, ignoreCase = true)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 340.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ProjectMainInfoBlock(data) }

        item {
            MyProjectScheduleCard(
                data = data,
                myProjectItem = myProjectItem,
                isLoading = isLoadingSchedule,
                onRefresh = onRefreshSchedule
            )
        }

        item { CurrentSemesterBlock(data) }

        item(span = { GridItemSpan(maxLineSpan) }) {
            BrowseAllProjectsCard(
                totalCount = scheduleItems.size,
                onClick = { navController.navigate(Screen.AllProjects.route) }
            )
        }

        if (data.lastSemestrBalls != "0") {
            item { LastSemesterBlock(data) }
        }

        if (data.arrearBalls >= "0" && data.arrear != "0") {
            item { ArrearBlock(data) }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(Modifier.height(UiSize().getNavBarPaddingSize()))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MyProjectScheduleCard(
    data: PDModel,
    myProjectItem: ProjectSheetItem?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Шапка карточки ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(Cookie12Sided.toShape())
                            .background(
                                generateColorFromHash(stringResource(R.string.my_project_schedule_title)).copy(0.15f)
                            )) {
                        Icon(
                            painter = painterResource(R.drawable.calendar_outline_24),
                            contentDescription = null,
                            tint = generateColorFromHash(stringResource(R.string.my_project_schedule_title)),
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Text(
                        text = stringResource(R.string.my_project_schedule_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.refresh_outline_28),
                            contentDescription = stringResource(R.string.try_again),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // --- Теги и Название проекта ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Тег "Твой проект"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.your_project_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Тег Направления
                    if (myProjectItem != null && myProjectItem.direction.isNotBlank()) {
                        val dirColor = generateColorFromHash(myProjectItem.direction)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(dirColor.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = myProjectItem.direction,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = dirColor
                            )
                        }
                    }
                }

                Text(
                    text = myProjectItem?.projectTitle ?: data.project,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // --- Информация о преподавателе и контактах ---
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val teacherName = myProjectItem?.teacher?.ifBlank { data.curator } ?: data.curator
                if (teacherName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.user_outline),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = teacherName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (myProjectItem != null && myProjectItem.contactInfo.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.mail_outline),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = myProjectItem.contactInfo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Блок расписания по дням ---
            if (myProjectItem != null && myProjectItem.scheduleByDays.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(16.dp)
                ) {
                    myProjectItem.scheduleByDays.forEach { (_, scheduleText) ->
                        Text(
                            text = scheduleText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else if (!isLoading && myProjectItem == null) {
                Text(
                    text = stringResource(R.string.my_project_schedule_not_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BrowseAllProjectsCard(
    totalCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(Cookie12Sided.toShape())
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.services_outline_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = stringResource(R.string.all_projects_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.view_all_projects_button) + if (totalCount > 0) " ($totalCount)" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProjectMainInfoBlock(data: PDModel) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
                        .background(generateColorFromHash(data.project).copy(0.15f))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lightbulb_star_outline),
                        contentDescription = stringResource(R.string.academic_year_content_description),
                        tint = generateColorFromHash(data.project),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = data.project,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.size(2.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 4.dp,
                bottomEnd = 24.dp,
                bottomStart = 24.dp
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                InfoRow(stringResource(R.string.label_year_pa), data.year)
                InfoRow(stringResource(R.string.label_semester_pa), data.semestr)
                InfoRow(stringResource(R.string.label_project_colon), data.project)
                InfoRow(stringResource(R.string.label_project_theme_colon), data.projectTheme)
                InfoRow(stringResource(R.string.label_curator_colon), data.curator)
                InfoRow(stringResource(R.string.label_subproject_colon), data.subproject)
                InfoRow(stringResource(R.string.label_rating_colon), data.raiting)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CurrentSemesterBlock(data: PDModel) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
                        .clip(Cookie12Sided.toShape())
                        .background(
                            generateColorFromHash(data.currentSemestrResult + data.currentSemestrBalls).copy(0.15f)
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.education_outline_28),
                        contentDescription = stringResource(R.string.academic_year_content_description),
                        tint = generateColorFromHash(data.currentSemestrResult + data.currentSemestrBalls),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(R.string.current_semester),
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(stringResource(R.string.label_points_colon), data.currentSemestrBalls)
                InfoRow(stringResource(R.string.label_result_colon), data.currentSemestrResult)
                InfoRow(stringResource(R.string.label_att_1_colon), data.currentAtt1)
                InfoRow(stringResource(R.string.label_att_2_colon), data.currentAtt2)
                InfoRow(stringResource(R.string.label_att_mid_colon), data.currentAttMid)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LastSemesterBlock(data: PDModel) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
                        .clip(Cookie12Sided.toShape())
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.document_outline_28),
                        contentDescription = stringResource(R.string.academic_year_content_description),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(R.string.last_semester),
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                InfoRow(stringResource(R.string.label_points_colon), data.lastSemestrBalls)
                InfoRow(stringResource(R.string.label_result_colon), data.lastSemestrResult)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ArrearBlock(data: PDModel) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
                        .clip(Cookie12Sided.toShape())
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.warning_triangle_outline_28),
                        contentDescription = stringResource(R.string.academic_year_content_description),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(R.string.arrear_title),
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                InfoRow(stringResource(R.string.label_description_pa), data.arrear)
                InfoRow(stringResource(R.string.label_points_colon), data.arrearBalls)
                InfoRow(stringResource(R.string.label_result_colon), data.arrearResult)
            }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
