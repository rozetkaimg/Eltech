package com.rozetka.presentation.ui.academicPerformance

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes.Companion.Clover8Leaf
import androidx.compose.material3.MaterialShapes.Companion.Cookie7Sided
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialShapes.Companion.Gem
import androidx.compose.material3.MaterialShapes.Companion.VerySunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.AcademicPerformanceItem
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.UiSize
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AcademicPerformanceScreen(
    navController: NavController,
    viewModel: AcademicPerformanceViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadAcademicPerformance()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.session_results), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when (val state = uiState) {
                is AcademicPerformanceUiState.Loading -> LoadingState()
                is AcademicPerformanceUiState.Error ->
                    ExpressiveErrorState(
                        state.message.toString(),
                        viewModel::loadAcademicPerformance
                    )

                is AcademicPerformanceUiState.Success -> {
                    val dataBySemester = state.data.groupBy { it.semestr }
                    val semesters = dataBySemester.keys.sortedBy { it.toIntOrNull() ?: 0 }

                    if (semesters.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.academic_data_missing))
                        }
                        return@Column
                    }

                    val pagerState = rememberPagerState(
                        initialPage = if (semesters.isNotEmpty()) semesters.lastIndex else 0,
                        pageCount = { semesters.size }
                    )
                    val coroutineScope = rememberCoroutineScope()

                    SecondaryScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.fillMaxWidth(),
                        edgePadding = 0.dp,
                        divider = { HorizontalDivider() }
                    ) {
                        semesters.forEachIndexed { index, semester ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                },
                                text = { Text(text = stringResource(R.string.semester_prefix, semester)) },
                                unselectedContentColor = Color.Gray
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->
                        val semesterData = dataBySemester[semesters[page]]
                        var selectedItem by remember { mutableStateOf<AcademicPerformanceItem?>(null) }

                        if (semesterData.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    stringResource(R.string.no_results_for_semester, semesters[page]),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            val groupedData = remember(semesterData) {
                                semesterData.groupBy { it.examType }
                                    .toSortedMap { a, b ->
                                        val priorityA = getControlPriority(a)
                                        val priorityB = getControlPriority(b)
                                        priorityA.compareTo(priorityB)
                                    }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 340.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Spacer(Modifier.height(16.dp))
                                }

                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    SemesterStatsCard(items = semesterData)
                                }

                                groupedData.forEach { (controlType, items) ->
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp),
                                            color = MaterialTheme.colorScheme.background
                                        ) {
                                            Text(
                                                text = controlType,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }

                                    items(
                                        items = items,
                                        key = { item -> item.id }
                                    ) { item ->
                                        AcademicPerformanceCard(
                                            item = item,
                                            onClick = { selectedItem = item }
                                        )
                                    }
                                }

                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Spacer(Modifier.height(UiSize().getNavBarPaddingSize()))
                                }
                            }
                        }

                        selectedItem?.let { item ->
                            DetailsBottomSheetAcademicPerformance(item = item, onDismiss = { selectedItem = null })
                        }
                    }
                }
            }
        }
    }
}

private data class StatsTheme(
    val title: String,
    val subtitle: String,
    val topIcon: ImageVector,
    val topShape: Shape,
    val topShapeColor: Color,
    val bgColor: Color,
    val contentColor: Color
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SemesterStatsCard(items: List<AcademicPerformanceItem>) {
    var count5 = 0; var count4 = 0; var count3 = 0; var count2 = 0
    items.forEach {
        when (it.grade.lowercase()) {
            "отлично" -> count5++
            "хорошо" -> count4++
            "удовлетворительно" -> count3++
            "неудовлетворительно", "не зачтено", "не явился" -> count2++
        }
    }

    val total = count5 + count4 + count3 + count2
    if (total == 0) return

    val p5 = count5.toFloat() / total
    val p4 = count4.toFloat() / total
    val p3 = count3.toFloat() / total
    val p2 = count2.toFloat() / total

    val theme = when {
        count2 > 0 -> StatsTheme(
            title = "Нужно подтянуть хвосты", subtitle = "Есть задолженности",
            topIcon = Icons.Rounded.Close, topShape = Gem.toShape(), topShapeColor = MaterialTheme.colorScheme.error,
            bgColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        count3 > 0 -> StatsTheme(
            title = "Есть над чем поработать", subtitle = "Не сдавайся!",
            topIcon = Icons.Rounded.Warning, topShape = VerySunny.toShape(), topShapeColor = MaterialTheme.colorScheme.tertiary,
            bgColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        count4 > 0 -> StatsTheme(
            title = "Хороший результат!", subtitle = "Так держать!",
            topIcon = Icons.Rounded.Check, topShape = Cookie7Sided.toShape(), topShapeColor = MaterialTheme.colorScheme.secondary,
            bgColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        count5 > 0 -> StatsTheme(
            title = "В этом семестре ты отличник!", subtitle = "Наши поздравления!",
            topIcon = Icons.Rounded.EmojiEvents, topShape = Cookie9Sided.toShape(), topShapeColor = MaterialTheme.colorScheme.primary,
            bgColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        else -> StatsTheme(
            title = "Семестр пройден!", subtitle = "Молодец!",
            topIcon = Icons.Rounded.Check, topShape = Clover8Leaf.toShape(), topShapeColor = MaterialTheme.colorScheme.primary,
            bgColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.bgColor,
            contentColor = theme.contentColor
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = theme.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = theme.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = theme.contentColor.copy(alpha = 0.9f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(theme.topShape)
                        .background(theme.topShapeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = theme.topIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (count5 > 0) Box(modifier = Modifier.weight(p5).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary))
                if (count4 > 0) Box(modifier = Modifier.weight(p4).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.secondary))
                if (count3 > 0) Box(modifier = Modifier.weight(p3).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.tertiary))
                if (count2 > 0) Box(modifier = Modifier.weight(p2).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.error))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (count5 > 0) LegendItem(color = MaterialTheme.colorScheme.primary, label = "5", percentage = (p5 * 100).roundToInt(), contentColor = theme.contentColor)
                if (count4 > 0) LegendItem(color = MaterialTheme.colorScheme.secondary, label = "4", percentage = (p4 * 100).roundToInt(), contentColor = theme.contentColor)
                if (count3 > 0) LegendItem(color = MaterialTheme.colorScheme.tertiary, label = "3", percentage = (p3 * 100).roundToInt(), contentColor = theme.contentColor)
                if (count2 > 0) LegendItem(color = MaterialTheme.colorScheme.error, label = "2", percentage = (p2 * 100).roundToInt(), contentColor = theme.contentColor)
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    percentage: Int,
    contentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label - $percentage%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

private fun getControlPriority(controlType: String?): Int {
    if (controlType == null) return 99
    return when {
        controlType.contains("Экзамен", ignoreCase = true) -> 1
        controlType.contains("Дифференцированный", ignoreCase = true) -> 2
        controlType.contains("Зачет", ignoreCase = true) -> 3
        else -> 4
    }
}