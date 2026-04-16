package com.rozetka.presentation.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rozetka.domain.util.StringObject
import com.rozetka.model.User
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.ExpressiveErrorState
import com.rozetka.presentation.util.UiSize
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(com.rozetka.domain.util.StringObject.ApiToken) {
        profileViewModel.getProfile()
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> LoadingState()
        is ProfileUiState.Success -> ProfileSuccessState(
            state = state,
            navController = navController
        )
        is ProfileUiState.Error -> ExpressiveErrorState(
            message = state.message,
            profileViewModel::getProfile
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSuccessState(
    state: ProfileUiState.Success,
    navController: NavHostController,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ProfileTopAppBar(
                onSettingsClick = { navController.navigate("Settings") }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { contentPadding ->
        StudentDataLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding()),
            navController = navController,
            data = state.data
        )
    }
}

@Composable
private fun StudentDataLayout(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    data: User,
) {
    StringObject.avatar = data.avatar

    val onPayment = { navController.navigate(Screen.Payment.route) }
    val onAcademic = { navController.navigate(Screen.AcademicPerScreen.route) }
    val onSearch = { navController.navigate(Screen.SearchStudentsScreen.route) }
    val onDigitalService = { navController.navigate(Screen.DigitalService.route) }
    val onPhysJournal = { navController.navigate(Screen.PhysEdJournalScreen.route) }
    val onProjectActivity = { navController.navigate(Screen.ProjectActivity.route) }
    val onStudentCard = { navController.navigate(Screen.StudentCardScreen.route) }
    val onMoodle = { navController.navigate(Screen.Moodle.route) }

    BoxWithConstraints(modifier = modifier) {
        val useTwoPane = this.maxWidth > 600.dp

        if (useTwoPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileCard(
                        "${data.name} ${data.surname}",
                        data.group,
                        data.avatar,
                        onStudentCard
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .verticalScroll(rememberScrollState())
                ) {
                    CategoryListContent(
                        onPayment,
                        onAcademic,
                        onSearch,
                        onDigitalService,
                        onPhysJournal,
                        onProjectActivity,
                        onMoodle
                    )
                    Spacer(Modifier.height(UiSize().getNavBarPaddingSize()))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileCard(
                    "${data.name} ${data.surname}",
                    data.group,
                    data.avatar,
                    onStudentCard
                )

                Spacer(Modifier.height(24.dp))

                CategoryListContent(
                    onPayment,
                    onAcademic,
                    onSearch,
                    onDigitalService,
                    onPhysJournal,
                    onProjectActivity,
                    onMoodle
                )

                Spacer(Modifier.height(UiSize().getNavBarPaddingSize()))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopAppBar(onSettingsClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.services), fontWeight = FontWeight.Bold) },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painterResource(R.drawable.settings_outline_28),
                    contentDescription = stringResource(R.string.settings),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}