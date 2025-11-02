package com.rozetka.presentation.ui.searchGroup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
                Modifier.height(64.dp).padding(end = 32.dp)
                    .semantics { isTraversalGroup = true }
            ) {
                SearchBar(
                    modifier = Modifier.padding(bottom = 8.dp)
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
            modifier = Modifier.padding(contentPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            if (favorites.isNotEmpty()) {
                FavoritesSection(
                    favorites = favorites,
                    onGroupClick = { groupName ->
                        navController.navigate(Screen.Schedule.route + "/$groupName")
                    },
                    onRemoveFavorite = viewModel::removeFromFavorites
                )
                Spacer(Modifier.height(16.dp))
            }


            when (val state = uiState) {
                is SearchGroupUiState.Loading -> LoadingState()
                is SearchGroupUiState.Success -> GroupList(
                    groups = state.groups,
                    favorites = favorites,
                    onGroupClick = { groupName ->
                        navController.navigate(Screen.Schedule.route + "/$groupName")
                    },
                    onToggleFavorite = { groupName, isFavorite ->
                        if (isFavorite) {
                            viewModel.removeFromFavorites(groupName)
                        } else {
                            viewModel.addToFavorites(groupName)
                        }
                    }
                )

                is SearchGroupUiState.Error -> ErrorState(message = state.message) { viewModel.searchGroups() }
                is SearchGroupUiState.Empty -> EmptyState(state.query)
                is SearchGroupUiState.Initial -> InitialState(hasFavorites = favorites.isNotEmpty())
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
        Text(
            text = stringResource(R.string.search_results_title, groups.size),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
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
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {

        Row(Modifier.fillMaxSize().padding(horizontal = 12.dp).align(Alignment.CenterHorizontally)) {
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                val description = if (isFavorite) {
                    stringResource(R.string.remove_from_favorites_desc)
                } else {
                    stringResource(R.string.add_to_favorites_desc)
                }
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                    contentDescription = description,
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry_search_button))
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

@Composable
private fun InitialState(hasFavorites: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val textRes = if (hasFavorites) {
            R.string.initial_state_with_favorites
        } else {
            R.string.initial_state_no_favorites
        }
        Text(
            text = stringResource(textRes),
            textAlign = TextAlign.Center
        )
    }
}