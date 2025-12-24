package com.rozetka.presentation.ui.message

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.MessageModelItem
import com.rozetka.presentation.ui.dialog.DialogScreen
import com.rozetka.presentation.ui.pay.LoadingState
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

    when (val state = uiState) {
        is MessagesUiState.Loading -> LoadingState()
        is MessagesUiState.Error -> ErrorState(state.message) { messagesViewModel.getMessages() }
        is MessagesUiState.Success -> {
            if (isTablet) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(0.4f)) {
                        MessagesListContent(
                            messages = state.data,
                            selectedId = selectedMessage?.id,
                            onSelect = { selectedMessage = it }
                        )
                    }

                    VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    Box(modifier = Modifier.weight(0.6f)) {
                        if (selectedMessage != null) {
                            key(selectedMessage?.id) {
                                DialogScreen(
                                    navController = navController,
                                    userId = selectedMessage!!.id,
                                    userName = selectedMessage!!.opponent.name ?: ""
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
                    onSelect = { message ->
                        navController.navigate("dialo/${message.opponent.name}/${message.id}")
                    }
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
    onSelect: (MessageModelItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(com.rozetka.presentation.R.string.mail), fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageItem(
                    message = message,
                    isSelected = message.id == selectedId,
                    onClick = { onSelect(message) }
                )
            }
            item { Spacer(Modifier.height(getNavigationBarHeightDp() + 80.dp)) }
        }
    }
}

@Composable
private fun EmptyChatPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Выберите чат, чтобы прочитать сообщения",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Повторить")
        }
    }
}