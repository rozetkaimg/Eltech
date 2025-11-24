package com.rozetka.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.rozetka.model.User
import com.rozetka.presentation.R
import com.rozetka.presentation.navigation.Screen
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.domain.util.StringObject
import com.rozetka.presentation.util.ThemeObject.BottomNavBarPaddingValue
import com.rozetka.presentation.util.getNavigationBarHeightDp
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ProfileUiState.Loading -> LoadingState()
        is ProfileUiState.Success -> ProfileSuccessState(
            state = state,
            navController = navController
        )
        is ProfileUiState.Error -> ErrorState(message = state.message) { profileViewModel.getProfile() }
    }
}



@Composable
private fun ErrorState(message: String, onUpdate: () -> Unit) {
  Column (
        modifier = Modifier.fillMaxSize(),

    ) {


      Button( onUpdate) {
          Text(
              text = message,
              modifier = Modifier.padding(16.dp),
              textAlign = TextAlign.Center,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.error
          )
      }
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
                onSettingsClick = {  navController.navigate("Settings")  }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { contentPadding ->
        StudentDataLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
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
    BoxWithConstraints(modifier = modifier) {
        val useTwoPane = this.maxWidth > 600.dp

        if (useTwoPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {

                }
                Column(
                    modifier = Modifier.weight(0.7f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileCard(
                        "$data.name ${data.surname}",
                        data.group,
                        data.avatar,
                        {navController.navigate(Screen.StudentCardScreen.route)}
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                data.name + ProfileCard(
                    data.name + " " + data.surname,
                    data.group,
                    data.avatar,
                    {navController.navigate(Screen.StudentCardScreen.route)}
                )
                Spacer(Modifier.height(24.dp))




                CategoryListContent({navController.navigate(Screen.Payment.route)}, {navController.navigate(Screen.AcademicPerScreen.route)}, {navController.navigate(Screen.SearchStudentsScreen.route)}, {
                    navController.navigate(Screen.DigitalService.route)
                }, {
                    navController.navigate(Screen.PhysEdJournalScreen.route)
                }, {
                    navController.navigate(Screen.ProjectActivity.route)
                })

                Spacer(Modifier.height(getNavigationBarHeightDp() + BottomNavBarPaddingValue.dp -45.dp))

            }
        }
    }
}






@Composable
fun DetailsCard(roundedCornerShape: RoundedCornerShape, content: @Composable ColumnScope.() -> Unit ) {

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = roundedCornerShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 24.dp)) {
            content()
        }
    }
}




@Composable
fun ProfileInfoRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$label ")
                }
                append(value)
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopAppBar(onSettingsClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.profile), fontWeight = FontWeight.Bold) },
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