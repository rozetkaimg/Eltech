package com.rozetka.presentation.ui.searchGroup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.UiSize
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchGroupScreen(
    navController: NavController,
    viewModel: SearchGroupViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val favorites by viewModel.favoritesFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = {

            Box(
                Modifier
                    .height(64.dp)
                    .padding(end = 32.dp)
                    .semantics { isTraversalGroup = true }
            ) {
                SearchBar(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .semantics { traversalIndex = 0f },
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            onSearch = { viewModel.searchGroups() },
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text(stringResource(R.string.search_groups_placeholder)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.search_icon_desc)
                                )
                            }
                        )
                    },
                    expanded = false,
                    onExpandedChange = { },
                ) {

                }
            }
        },

            navigationIcon = {
                Box(
                    Modifier.height(64.dp)) {
                    IconButton(onClick = { navController.navigateUp() }, modifier = Modifier.align(
                        Alignment.Center)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            }
        ) }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            if (favorites.isNotEmpty()) {
                FavoritesSection(
                    favorites = favorites,
                    onGroupClick = { groupName ->
                        navController.navigate(Screen.ScheduleLink.route + "/$groupName")
                    },
                    onRemoveFavorite = viewModel::removeFromFavorites
                )
                Spacer(Modifier.height(16.dp))
            }


            when (val state = uiState) {
                is SearchGroupUiState.Loading -> LoadingState()
                is SearchGroupUiState.Success -> {
                    Text(
                        text = "Найдено групп: ${state.groups.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    GroupList(
                        groups = state.groups,
                        favorites = favorites,
                        onGroupClick = { groupName ->
                            navController.navigate(Screen.ScheduleLink.route + "/$groupName")
                        },
                        onToggleFavorite = { groupName, isFavorite ->
                            if (isFavorite) {
                                viewModel.removeFromFavorites(groupName)
                            } else {
                                viewModel.addToFavorites(groupName)
                            }
                        }
                    )
                }

                is SearchGroupUiState.Error -> ExpressiveErrorState(
                    message = state.message,
                    { viewModel.searchGroups() }
                )
                is SearchGroupUiState.Empty -> EmptyState(state.query)
                is SearchGroupUiState.Initial -> InitialState()
            }
        }
    }
}



@Composable
private fun FavoritesSection(
    favorites: List<String>,
    onGroupClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.favorites_section_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(favorites) { groupName ->
                GroupItem(
                    groupName = groupName,
                    isFavorite = true,
                    onClick = { onGroupClick(groupName) },
                    onToggleFavorite = { onRemoveFavorite(groupName) }
                )
            }
        }
    }
}

@Composable
private fun GroupList(
    groups: List<String>,
    favorites: List<String>,
    onGroupClick: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit
) {
    Column {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups) { groupName ->
                val isFavorite = groupName in favorites
                GroupItem(
                    groupName = groupName,
                    isFavorite = isFavorite,
                    onClick = { onGroupClick(groupName) },
                    onToggleFavorite = { onToggleFavorite(groupName, isFavorite) }
                )
            }
            item { Spacer(Modifier.height(UiSize().getNavBarPaddingSize())) }
        }
    }
}


@Composable
private fun GroupItem(
    groupName: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {

    Card(
        modifier = Modifier.height(32.dp),
        shape = RoundedCornerShape( if(isFavorite) 12.dp else 14.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {

        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.8f)
            )
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.2f)
            ) {
                val description = if (isFavorite) {
                    stringResource(R.string.remove_from_favorites_desc)
                } else {
                    stringResource(R.string.add_to_favorites_desc)
                }
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                    contentDescription = description,
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,

                )
            }
        }
    }
}



@Composable
private fun EmptyState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(R.string.empty_state_message, query))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InitialState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 128.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialShapes.Cookie6Sided.toShape()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.search_groups_placeholder),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

        }
    }
}