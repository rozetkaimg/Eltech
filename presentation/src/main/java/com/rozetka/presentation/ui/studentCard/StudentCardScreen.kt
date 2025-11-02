package com.rozetka.presentation.ui.studentCard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.rozetka.model.User
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.ui.profile.DetailsCard
import com.rozetka.presentation.ui.profile.ProfileInfoRow
import com.rozetka.presentation.util.ThemeObject
import com.rozetka.presentation.util.getNavigationBarHeightDp
import org.koin.androidx.compose.koinViewModel

@Composable
fun StudentCardScreen(
    navController: NavHostController,
    viewModel: StudentCardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is StudentCardUiState.Loading -> LoadingState()
        is StudentCardUiState.Success -> StudentCardSuccessState(
            state = state,
            navController = navController
        )

        is StudentCardUiState.Error -> ErrorState(message = state.message) { viewModel.refreshData() }
    }
}

@Composable
private fun ErrorState(message: String, onUpdate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onUpdate) {
            Text(
                text = stringResource(R.string.error_load_data_retry, message),
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
private fun StudentCardSuccessState(
    state: StudentCardUiState.Success,
    navController: NavHostController,
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        stringResource(R.string.student_label),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface

                )
            )
        }
    ) { contentPadding ->
        StudentCardDataLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding()),
            navController = navController,
            user = state.data
        )
    }
}

@Composable
private fun StudentCardDataLayout(
    modifier: Modifier,
    navController: NavHostController,
    user: User,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ProfileCardLite(
            userName = "${user.name} ${user.surname}",
            description = user.group,

            imageUrl = user.avatar.ifBlank { "" }
        )

        Spacer(Modifier.height(24.dp))
        StudentCardDetails(user = user)

        Spacer(Modifier.height(getNavigationBarHeightDp() + ThemeObject.BottomNavBarPaddingValue.dp - 45.dp))
    }
}

@Composable
private fun StudentCardDetails(user: User) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(bottom = 80.dp)
    ) {
        DetailsCard(
            roundedCornerShape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp,
                bottomEnd = 8.dp,
                bottomStart = 8.dp
            )
        ) {
            Spacer(Modifier.size(8.dp))
            ProfileInfoRow(stringResource(R.string.label_status), user.userStatus)
            ProfileInfoRow(stringResource(R.string.label_student_code), user.code)
            ProfileInfoRow(stringResource(R.string.label_group), user.group)
            ProfileInfoRow(stringResource(R.string.label_gender), user.sex)
            ProfileInfoRow(stringResource(R.string.label_birth_date), user.birthday)
            Spacer(Modifier.size(8.dp))
        }

        DetailsCard(RoundedCornerShape(8.dp)) {
            Spacer(Modifier.size(8.dp))
            ProfileInfoRow(stringResource(R.string.label_faculty), user.faculty)
            ProfileInfoRow(stringResource(R.string.label_course_ru), user.course)
            ProfileInfoRow(stringResource(R.string.label_specialty), user.specialty)
            ProfileInfoRow(stringResource(R.string.label_specialization), user.specialization)
            Spacer(Modifier.size(8.dp))
        }

        DetailsCard(
            RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = 28.dp,
                bottomEnd = 28.dp
            )
        ) {
            Spacer(Modifier.size(8.dp))
            ProfileInfoRow(stringResource(R.string.label_degree_level), user.degreeLevel)
            ProfileInfoRow(stringResource(R.string.label_education_form), user.educationForm)
            ProfileInfoRow(stringResource(R.string.label_finance_type), user.finance)
            ProfileInfoRow(stringResource(R.string.label_degree_length), user.degreeLength)
            Spacer(Modifier.size(8.dp))
        }

        DetailsCard(
            RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = 28.dp,
                bottomEnd = 28.dp
            )
        ) {
            Spacer(Modifier.size(8.dp))
            ProfileInfoRow(stringResource(R.string.label_email_ru), user.email)
            ProfileInfoRow(stringResource(R.string.label_phone), user.phone)
            ProfileInfoRow(stringResource(R.string.label_last_access), user.lastaccess)
            Spacer(Modifier.size(8.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileCardLite(
    userName: String,
    description: String,
    imageUrl: String
) {
    DetailsCard(
        roundedCornerShape = RoundedCornerShape(
            28.dp
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        ) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(77.dp)
                    .clip(Cookie9Sided.toShape())
                    .align(Alignment.CenterVertically),
                loading = {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        LoadingIndicator()
                    }
                },
                error = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_profile),
                            contentDescription = stringResource(R.string.cd_profile_photo),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            )
            Spacer(Modifier.size(16.dp))
            Column(Modifier.align(alignment = Alignment.CenterVertically)) {
                Text(
                    text = userName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier

                )
                Text(
                    text = description,
                    fontSize = 16.sp,
                    modifier = Modifier
                )

            }

        }

    }
}