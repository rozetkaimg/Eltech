package com.rozetka.presentation.ui.studentCard

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.rozetka.model.User
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.CollapsingToolbarScaffold
import com.rozetka.presentation.util.ScrollStrategy
import com.rozetka.presentation.util.getNavigationBarHeightDp
import com.rozetka.presentation.util.rememberCollapsingToolbarScaffoldState
import org.koin.androidx.compose.koinViewModel

@Composable
fun StudentCardScreen(
    navController: NavHostController,
    viewModel: StudentCardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (val state = uiState) {
        is StudentCardUiState.Loading -> LoadingState()
        is StudentCardUiState.Success -> StudentCardSuccessState(
            state = state,
            navController = navController,
            onUpdateAvatar = { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() }
                if (bytes != null) {
                    viewModel.updateAvatar(bytes)
                }
            },
            onUpdateEmail = { email ->
                viewModel.updateEmail(email)
            },
            onUpdatePhone = { phone ->
                viewModel.updatePhone(phone)
            }
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StudentCardSuccessState(
    state: StudentCardUiState.Success,
    navController: NavHostController,
    onUpdateAvatar: (Uri) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onUpdatePhone: (String) -> Unit
) {
    val states = rememberCollapsingToolbarScaffoldState()
    val currentRadius = 28.dp * states.toolbarState.progress
    val collapsedColor = MaterialTheme.colorScheme.surface
    val expandedColor = MaterialTheme.colorScheme.primaryContainer
    val dynamicContainerColor = lerp(
        start = collapsedColor,
        stop = expandedColor,
        fraction = states.toolbarState.progress
    )

    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateAvatar(uri)
        }
    }

    var showEditDialog by remember { mutableStateOf<EditType?>(null) }

    if (showEditDialog != null) {
        EditContactDialog(
            type = showEditDialog!!,
            initialValue = when (showEditDialog) {
                EditType.EMAIL -> state.data.email ?: ""
                EditType.PHONE -> state.data.phone ?: ""
                else -> ""
            },
            onDismiss = { showEditDialog = null },
            onConfirm = { newValue ->
                when (showEditDialog) {
                    EditType.EMAIL -> onUpdateEmail(newValue)
                    EditType.PHONE -> onUpdatePhone(newValue)
                    else -> {}
                }
                showEditDialog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.student_label),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.alpha(1f - states.toolbarState.progress)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = dynamicContainerColor,
                    scrolledContainerColor = dynamicContainerColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )
        }
    ) {
        CollapsingToolbarScaffold(
            modifier = Modifier
                .background(dynamicContainerColor)
                .padding(top = it.calculateTopPadding()),
            state = states,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            toolbar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                        .pin()
                        .background(dynamicContainerColor)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .road(Alignment.Center, Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .alpha(states.toolbarState.progress),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            SubcomposeAsyncImage(
                                model = state.data.avatar,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(Cookie9Sided.toShape()),
                                loading = {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
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
                                            contentDescription = stringResource(R.string.cd_profile_photo_placeholder),
                                            modifier = Modifier
                                                .size(150.dp)
                                                .padding(16.dp)
                                        )
                                    }
                                }
                            )

                            FloatingActionButton(
                                onClick = {
                                    avatarLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 16.dp)

                            ) {
                                Icon(
                                    painterResource(R.drawable.camera_outline_24),
                                    contentDescription = null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${state.data.surname} ${state.data.name} ${state.data.patronymic}",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 25.sp,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                    }
                }
            }) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = currentRadius, topEnd = currentRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {}
            StudentCardDataLayout(
                modifier = Modifier.fillMaxSize(),
                user = state.data,
                onEditEmail = { showEditDialog = EditType.EMAIL },
                onEditPhone = { showEditDialog = EditType.PHONE }
            )
        }
    }
}

@Composable
private fun StudentCardDataLayout(
    modifier: Modifier,
    user: User,
    onEditEmail: () -> Unit,
    onEditPhone: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StudentCardDetails(
            user = user,
            onEditEmail = onEditEmail,
            onEditPhone = onEditPhone
        )
        Spacer(Modifier.height(getNavigationBarHeightDp()))
    }
}

@Composable
fun StudentCardDetails(
    user: User,
    onEditEmail: () -> Unit,
    onEditPhone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoSectionCard(
            title = "Личные данные",
            icon = painterResource(R.drawable.ic_profile)
        ) {
            ProfileInfoRow(stringResource(R.string.label_student_code), user.code)
            ProfileInfoRow(stringResource(R.string.label_group), user.group)
            ProfileInfoRow(stringResource(R.string.label_birth_date), user.birthday)
        }

        InfoSectionCard(
            title = "Учеба",
            icon = painterResource(R.drawable.education_outline_28)
        ) {
            ProfileInfoRow(stringResource(R.string.label_faculty), user.faculty)
            ProfileInfoRow(stringResource(R.string.label_course_ru), user.course)
            ProfileInfoRow(stringResource(R.string.label_specialty), user.specialty)
            ProfileInfoRow(stringResource(R.string.label_specialization), user.specialization)
        }

        InfoSectionCard(
            title = "Детали образования",
            icon = painterResource(R.drawable.newsfeed)
        ) {
            ProfileInfoRow(stringResource(R.string.label_degree_level), user.degreeLevel)
            ProfileInfoRow(stringResource(R.string.label_education_form), user.educationForm)
            ProfileInfoRow(stringResource(R.string.label_finance_type), user.finance)
            ProfileInfoRow(stringResource(R.string.label_degree_length), user.degreeLength)
        }

        InfoSectionCard(
            title = "Контакты",
            icon = painterResource(R.drawable.document_outline_28)
        ) {
            ProfileInfoRow(
                label = stringResource(R.string.label_email_ru),
                value = user.email,
                onEditClick = onEditEmail
            )
            ProfileInfoRow(
                label = stringResource(R.string.label_phone),
                value = user.phone,
                onEditClick = onEditPhone
            )
            ProfileInfoRow(stringResource(R.string.label_last_access), user.lastaccess)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InfoSectionCard(
    title: String,
    icon: Painter,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Card(
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(
                topEnd = 24.dp,
                topStart = 24.dp,
                bottomStart = 4.dp,
                bottomEnd = 4.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
            ) {
                Box(
                    Modifier
                        .clip(Cookie9Sided.toShape())
                        .background(MaterialTheme.colorScheme.onPrimary)
                        .size(32.dp),
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.size(2.dp))
        Card(
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(
                topEnd = 4.dp,
                topStart = 4.dp,
                bottomStart = 24.dp,
                bottomEnd = 24.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String?,
    onEditClick: (() -> Unit)? = null
) {
    val displayValue = if (value.isNullOrBlank()) "—" else value

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )

        Row(
            modifier = Modifier.weight(0.6f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )

            if (onEditClick != null) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

enum class EditType {
    EMAIL, PHONE
}

@Composable
fun EditContactDialog(
    type: EditType,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (type) {
                    EditType.EMAIL -> "Редактировать Email"
                    EditType.PHONE -> "Редактировать телефон"
                }
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = {
                    Text(
                        when (type) {
                            EditType.EMAIL -> "Email"
                            EditType.PHONE -> "Телефон"
                        }
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = when (type) {
                        EditType.EMAIL -> KeyboardType.Email
                        EditType.PHONE -> KeyboardType.Phone
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}