package com.rozetka.presentation.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rozetka.presentation.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = viewModel::onErrorDialogDismissed,
            title = { Text(stringResource(R.string.error_title)) },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onErrorDialogDismissed
                ) {
                    Text(stringResource(R.string.ok_button))
                }
            }
        )
    }

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
        }
    }

    val sheetState = rememberModalBottomSheetState()

    if (uiState.showUnofficialAppWarning) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onWarningDismissed,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(128.dp)
                        .clip(Cookie9Sided.toShape())
                        .background(Color(0xFFE62147))
                ) {
                    Icon(
                        painter = painterResource(R.drawable.education_outline_28),
                        contentDescription = stringResource(R.string.academic_year_content_description),
                        tint = Color.White,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(Modifier.size(4.dp))
                Text(
                    text = stringResource(id = R.string.warning_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = stringResource(id = R.string.unofficial_app_warning_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(
                        onClick = viewModel::onWarningDismissed,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(id = R.string.cancel_button))
                    }
                    Button(
                        onClick = viewModel::onWarningAccepted,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(stringResource(id = R.string.allow_and_login_button))
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.mosplitech_logo_en),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.login_screen_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    OutlinedTextField(
                        value = uiState.login,
                        onValueChange = viewModel::onLoginChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.login_label)) },
                        singleLine = true,
                        isError = uiState.error != null
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    var passwordVisible by rememberSaveable { mutableStateOf(false) }
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.password_label)) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description =
                                if (passwordVisible) stringResource(R.string.hide_password_description) else stringResource(
                                    R.string.show_password_description
                                )
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, description)
                            }
                        },
                        isError = uiState.error != null
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = viewModel::onLoginClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(stringResource(R.string.login_button))
                        }
                    }
                }
            }
        }
    }
}