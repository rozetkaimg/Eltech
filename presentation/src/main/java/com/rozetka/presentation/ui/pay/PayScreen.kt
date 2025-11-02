package com.rozetka.presentation.ui.pay


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rozetka.presentation.R



@Composable
fun PayScreen(
    navController: NavController,
    payViewModel: PayViewModel = PayViewModel()
) {
    val uiState by payViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is PayUiState.Loading -> LoadingState()
        is PayUiState.Success -> PaySuccessState(
            state = state,
            navController = navController
        )

        is PayUiState.Error -> ErrorState(
            message = state.message,
            onUpdate = { payViewModel.getPayInfo() })
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ContainedLoadingIndicator(Modifier.size(78.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaySuccessState(
    navController: NavController,
    state: PayUiState.Success,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),

        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.pay_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

        },

        containerColor = MaterialTheme.colorScheme.surface
    ) { contentPadding ->
        PayDataLayout(data = state.data, contentPadding = contentPadding)
    }
}





