package com.rozetka.presentation.ui.message

import androidx.activity.compose.BackHandler
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.presentation.navigation.Screen
import com.rozetka.model.MessageModelItem
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.dialog.DialogScreen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.getNavigationBarHeightDp
import org.koin.androidx.compose.koinViewModel

@Composable
fun MessagesScreen(
    messagesViewModel: MessagesViewModel = koinViewModel(),
    navController: NavController,
    windowSizeClass: WindowWidthSizeClass
) {
    val uiState by messagesViewModel.uiState.collectAsStateWithLifecycle()
    val isTablet = windowSizeClass != WindowWidthSizeClass.Compact

    var selectedMessage by remember { mutableStateOf<MessageModelItem?>(null) }
    var listPanelWidthDp by remember { mutableStateOf(320.dp) }
    val density = LocalDensity.current
    var totalWidthPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    when (val state = uiState) {
        is MessagesUiState.Loading -> LoadingState()
        is MessagesUiState.Error -> ExpressiveErrorState(
            state.message,
            messagesViewModel::getMessages
        )
        is MessagesUiState.Success -> {
            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { totalWidthPx = it.size.width.toFloat() }
                ) {
                    Box(modifier = Modifier.width(listPanelWidthDp)) {
                        MessagesListContent(
                            messages = state.data,
                            selectedId = selectedMessage?.id,
                            isTablet = true,
                            onSelect = { selectedMessage = it },
                            navController = navController
                        )
                    }

                    val dividerColor by animateColorAsState(
                        targetValue = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )

                    val indicatorWidth by animateDpAsState(
                        targetValue = if (isDragging) 8.dp else 4.dp
                    )

                    val indicatorHeight by animateDpAsState(
                        targetValue = if (isDragging) 56.dp else 48.dp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(32.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragStart = { isDragging = true },
                                    onDragEnd = { isDragging = false },
                                    onDragCancel = { isDragging = false }
                                ) { change, dragAmount ->
                                    change.consume()
                                    with(density) {
                                        val currentWidthPx = listPanelWidthDp.toPx()
                                        val newWidthPx = currentWidthPx + dragAmount

                                        val minWidthPx = 280.dp.toPx()
                                        val maxWidthPx = totalWidthPx * 0.5f

                                        if (newWidthPx in minWidthPx..maxWidthPx) {
                                            listPanelWidthDp = newWidthPx.toDp()
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(indicatorWidth)
                                .height(indicatorHeight)
                                .clip(RoundedCornerShape(4.dp))
                                .background(dividerColor)
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (selectedMessage != null) {
                            key(selectedMessage?.id) {
                                DialogScreen(
                                    navController = navController,
                                    userId = selectedMessage!!.id,
                                    userName = selectedMessage!!.opponent.name ?: "",
                                    avatarURL = selectedMessage!!.opponent.avatar ?: "",
                                    opponentData = selectedMessage!!.opponent.data ?: "",
                                    isTablet = true,
                                    isSubject = !selectedMessage!!.subject.isNullOrBlank()
                                )
                            }
                        } else {
                            EmptyChatPlaceholder()
                        }
                    }
                }
            } else {
                MessagesListContent(
                    messages = state.data,
                    isTablet = false,
                    onSelect = { message ->
                        val encodedAvatar = Uri.encode(message.opponent.avatar ?: "")
                        val encodedData = Uri.encode(message.opponent.data ?: "")
                        val isSubject = !message.subject.isNullOrBlank()
                        navController.navigate("dialo/${message.opponent.name}/${message.id}?avatarUrl=$encodedAvatar&isSubject=$isSubject&opponentData=$encodedData")
                    },
                    navController = navController
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagesListContent(
    messages: List<MessageModelItem>,
    selectedId: String? = null,
    isTablet: Boolean,
    onSelect: (MessageModelItem) -> Unit,
    navController: NavController
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) {
            messages
        } else {
            messages.filter { message ->
                val nameMatch = message.opponent.name?.contains(searchQuery, ignoreCase = true) == true
                val subjectMatch = message.subject?.contains(searchQuery, ignoreCase = true) == true
                val textMatch = message.lastmessage.text?.contains(searchQuery, ignoreCase = true) == true

                nameMatch || subjectMatch || textMatch
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            val fabBottomPadding = if (isTablet) {
                0.dp
            } else {
                getNavigationBarHeightDp() + 64.dp
            }

            FloatingActionButton(
                onClick = { navController.navigate(Screen.SearchPeople.route) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(
                    bottom = fabBottomPadding
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.add_chat),
                    contentDescription = stringResource(R.string.new_message)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.search_chats_placeholder),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = stringResource(com.rozetka.presentation.R.string.mail),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isSearchActive) {
                                isSearchActive = false
                                searchQuery = ""
                            } else {
                                isSearchActive = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchActive) stringResource(R.string.close_search) else stringResource(R.string.search)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredMessages.isEmpty() && messages.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_results_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = filteredMessages,
                    key = { it.id }
                ) { message ->
                    MessageItem(
                        message = message,
                        isSelected = message.id == selectedId,
                        onClick = { onSelect(message) }
                    )
                }
            }
            item {
                Spacer(Modifier.height(getNavigationBarHeightDp() + 80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyChatPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(Cookie9Sided.toShape())
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(com.rozetka.presentation.R.drawable.chats_outline_28),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.select_chat_placeholder),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}