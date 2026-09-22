package com.rozetka.presentation.ui.physEdSchedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.PhysEdClass
import com.rozetka.model.PhysEdLocation
import com.rozetka.model.PhysEdMetadata
import com.rozetka.model.PhysEdScheduleResponse
import com.rozetka.model.PhysEdTimeSlot
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.UiSize
import com.rozetka.presentation.util.generateColorFromHash
import org.koin.androidx.compose.koinViewModel

import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import com.rozetka.model.PhysEdSubscribedClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysEdScheduleScreen(
    navController: NavController,
    viewModel: PhysEdScheduleViewModel = koinViewModel()
) {
    val stateTop = rememberTopAppBarState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
    val selectedDiscipline by viewModel.selectedDiscipline.collectAsStateWithLifecycle()
    val selectedLocationId by viewModel.selectedLocationId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(stateTop)

    var isSearchVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(R.string.physed_schedule_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchVisible = !isSearchVisible
                        if (!isSearchVisible && searchQuery.isNotEmpty()) {
                            viewModel.onSearchQueryChange("")
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearchVisible) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is PhysEdScheduleUiState.Loading -> LoadingState()
                is PhysEdScheduleUiState.Success -> PhysEdScheduleSuccessState(
                    response = state.data,
                    subscriptions = subscriptions,
                    selectedDiscipline = selectedDiscipline,
                    selectedLocationId = selectedLocationId,
                    searchQuery = searchQuery,
                    isSearchVisible = isSearchVisible,
                    onDisciplineSelect = viewModel::onDisciplineSelect,
                    onLocationSelect = viewModel::onLocationSelect,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onToggleSubscription = viewModel::toggleSubscription
                )
                is PhysEdScheduleUiState.Error -> ExpressiveErrorState(
                    message = state.message,
                    onUpdate = { viewModel.loadSchedule() }
                )
            }
        }
    }
}

@Composable
private fun PhysEdScheduleSuccessState(
    response: PhysEdScheduleResponse,
    subscriptions: List<PhysEdSubscribedClass>,
    selectedDiscipline: String?,
    selectedLocationId: String?,
    searchQuery: String,
    isSearchVisible: Boolean,
    onDisciplineSelect: (String?) -> Unit,
    onLocationSelect: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSubscription: (PhysEdClass, PhysEdTimeSlot, PhysEdLocation?) -> Unit
) {
    val locationsMap = remember(response.locations) {
        response.locations.associateBy { it.id }
    }

    val disciplines = remember(response.schedule) {
        response.schedule
            .flatMap { slot -> slot.classes.flatMap { it.disciplines } }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    val locations = remember(response.locations) {
        response.locations
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PhysEdScheduleHeaderCard(metadata = response.metadata)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedVisibility(
                    visible = isSearchVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    PhysEdScheduleSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange
                    )
                }

                if (disciplines.isNotEmpty()) {
                    PhysEdDisciplineChips(
                        disciplines = disciplines,
                        selectedDiscipline = selectedDiscipline,
                        onDisciplineSelect = onDisciplineSelect
                    )
                }

                if (locations.isNotEmpty()) {
                    PhysEdLocationChips(
                        locations = locations,
                        selectedLocationId = selectedLocationId,
                        onLocationSelect = onLocationSelect
                    )
                }
            }
        }

        items(response.schedule) { slot ->
            val filteredClasses = remember(slot.classes, selectedDiscipline, selectedLocationId, searchQuery, locationsMap) {
                slot.classes.filter { classItem ->
                    val matchesDiscipline = selectedDiscipline == null || classItem.disciplines.any {
                        it.contains(selectedDiscipline, ignoreCase = true)
                    }
                    val matchesLocation = selectedLocationId == null || classItem.locationIds.contains(selectedLocationId)
                    val matchesSearch = searchQuery.isBlank() ||
                        classItem.disciplines.any { it.contains(searchQuery, ignoreCase = true) } ||
                        classItem.locationIds.any { locId ->
                            val loc = locationsMap[locId]
                            loc?.name?.contains(searchQuery, ignoreCase = true) == true ||
                                loc?.address?.contains(searchQuery, ignoreCase = true) == true
                        }

                    matchesDiscipline && matchesLocation && matchesSearch
                }
            }

            if (filteredClasses.isNotEmpty()) {
                PhysEdSlotCard(
                    timeSlot = slot,
                    filteredClasses = filteredClasses,
                    locationsMap = locationsMap,
                    subscriptions = subscriptions,
                    onToggleSubscription = onToggleSubscription
                )
            }
        }

        item {
            Spacer(Modifier.height(UiSize().getNavBarPaddingSize()))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PhysEdScheduleHeaderCard(metadata: PhysEdMetadata?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    painter = painterResource(R.drawable.physical),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = metadata?.title ?: stringResource(R.string.physed_schedule_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!metadata?.semester.isNullOrBlank()) {
                    Text(
                        text = metadata.semester,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PhysEdScheduleSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Поиск дисциплины или спортзала...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = searchQuery.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.common_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = CircleShape,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun PhysEdDisciplineChips(
    disciplines: List<String>,
    selectedDiscipline: String?,
    onDisciplineSelect: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            FilterChip(
                selected = selectedDiscipline == null,
                onClick = { onDisciplineSelect(null) },
                label = { Text("Все виды спорта") },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        items(disciplines) { discipline ->
            val isSelected = selectedDiscipline == discipline
            val chipColor = generateColorFromHash(discipline)

            FilterChip(
                selected = isSelected,
                onClick = { onDisciplineSelect(discipline) },
                label = { Text(discipline) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = chipColor,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun PhysEdLocationChips(
    locations: List<PhysEdLocation>,
    selectedLocationId: String?,
    onLocationSelect: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            FilterChip(
                selected = selectedLocationId == null,
                onClick = { onLocationSelect(null) },
                label = { Text(stringResource(R.string.all_locations)) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        items(locations) { loc ->
            val isSelected = selectedLocationId == loc.id

            FilterChip(
                selected = isSelected,
                onClick = { onLocationSelect(loc.id) },
                label = { Text(loc.name) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PhysEdSlotCard(
    timeSlot: PhysEdTimeSlot,
    filteredClasses: List<PhysEdClass>,
    locationsMap: Map<String, PhysEdLocation>,
    subscriptions: List<PhysEdSubscribedClass>,
    onToggleSubscription: (PhysEdClass, PhysEdTimeSlot, PhysEdLocation?) -> Unit
) {
    val slotNumber = when (timeSlot.timeSlot.trim()) {
        "09:00 - 10:30" -> "1"
        "10:40 - 12:10" -> "2"
        "12:20 - 13:50" -> "3"
        "14:30 - 16:00" -> "4"
        "16:10 - 17:40" -> "5"
        else -> "•"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Slot Time Header Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = slotNumber,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = timeSlot.timeSlot,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Classes for this slot
        filteredClasses.forEach { classItem ->
            val primaryLocation = classItem.locationIds.firstOrNull()?.let { locationsMap[it] }
            val discName = classItem.disciplines.firstOrNull() ?: "Физическая культура"
            val slotTime = timeSlot.timeSlot.trim()
            val subId = "$discName|$slotTime|${primaryLocation?.name.orEmpty()}"
            val isSubscribed = subscriptions.any { it.id == subId }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Disciplines FlowRow
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        classItem.disciplines.forEach { disc ->
                            val chipColor = generateColorFromHash(disc)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(chipColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = disc,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = chipColor
                                )
                            }
                        }
                    }

                    // Locations List
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        classItem.locationIds.forEach { locId ->
                            val loc = locationsMap[locId]
                            if (loc != null) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.loc_point),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )

                                    Column {
                                        Text(
                                            text = loc.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (loc.address.isNotBlank()) {
                                            Text(
                                                text = loc.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Subscription Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSubscribed) {
                            FilledTonalButton(
                                onClick = { onToggleSubscription(classItem, timeSlot, primaryLocation) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Вы подписаны", style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onToggleSubscription(classItem, timeSlot, primaryLocation) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Подписаться", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
