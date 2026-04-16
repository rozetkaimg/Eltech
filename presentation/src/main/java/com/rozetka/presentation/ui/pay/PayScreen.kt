package com.rozetka.presentation.ui.pay


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rozetka.presentation.R
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.ExpressiveErrorStateTop
import org.koin.androidx.compose.koinViewModel


@Composable
fun PayScreen(
    navController: NavController,
    payViewModel: PayViewModel = koinViewModel(),
) {
    val uiState by payViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is PayUiState.Loading -> LoadingState()
        is PayUiState.Success -> PaySuccessState(
            state = state,
            navController = navController
        )

        is PayUiState.Error -> ExpressiveErrorStateTop(
            message = state.message,
            onUpdate = { payViewModel.getPayInfo() },
            onBack = {navController.navigateUp()},
            title =  stringResource(R.string.pay_title))
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





