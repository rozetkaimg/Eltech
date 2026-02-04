package com.rozetka.presentation.ui.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.toShape
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rozetka.model.MessageModelItem
import com.rozetka.presentation.ui.dialog.DialogScreen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.ui.settings.components.MonetItem
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

    when (val state = uiState) {
        is MessagesUiState.Loading -> LoadingState()
        is MessagesUiState.Error -> ExpressiveErrorState(
            state.message,
            messagesViewModel::getMessages
        )
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
                                    userName = selectedMessage!!.opponent.name ?: "",
                                    avatarURL = selectedMessage!!.opponent.avatar?: "",
                                    isTablet = true,
                                    isSubject = !selectedMessage!!.subject.isNullOrEmpty()
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyChatPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row() {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(com.rozetka.presentation.R.drawable.chats_outline_28, ),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.surface
                )
            }
            Spacer(Modifier.size(16.dp))
            Text(
                text = "Выберите чат, чтобы прочитать сообщения",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

