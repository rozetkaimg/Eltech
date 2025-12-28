package com.rozetka.presentation.ui.aboutApplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Web
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.settings.ItemPosition
import com.rozetka.presentation.ui.settings.ModernSettingsItem
import com.rozetka.presentation.util.AppVersionText
import com.rozetka.presentation.util.generateColorFromHash
import com.rozetka.presentation.util.getNavigationBarHeightDp
import com.rozetka.presentation.util.openUrlInBrowser

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(navController: NavController) {

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_app), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), start = 16.dp, end = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_politech_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(220.dp)
                        .align(Alignment.Center)
                        .padding(36.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                )
            }

            Spacer(Modifier.padding(28.dp))
            ModernSettingsItem(
                icon = Icons.Outlined.Info,
                iconBackgroundColor = generateColorFromHash(stringResource(R.string.app_info_title_label)),
                title = stringResource(R.string.app_info_title_label),
                subtitle = stringResource(R.string.about_app_info_subtitle),
                position = ItemPosition.TOP,
                onClick = {
                    openUrlInBrowser(context, "https://t.me/rozetka_iso")
                }
            )
            Spacer(Modifier.size(2.dp))
            ModernSettingsItem(
                icon = Icons.Outlined.Android,
                iconBackgroundColor = generateColorFromHash(stringResource(R.string.app_info_version_label)),
                title = stringResource(R.string.app_info_version_label),
                subtitle = AppVersionText(LocalContext.current),
                position = ItemPosition.MIDDLE,
                onClick = {
                    handleButtonClickWithToast(context)
                }
            )
            Spacer(Modifier.size(2.dp))
            ModernSettingsItem(
                icon = Icons.Outlined.AccountCircle,
                iconBackgroundColor = generateColorFromHash(stringResource(R.string.app_info_author_label)),
                title = stringResource(R.string.app_info_author_label),
                subtitle = stringResource(R.string.about_author_subtitle),
                position = ItemPosition.MIDDLE,
                onClick = {
                    openUrlInBrowser(context, "https://t.me/Rozetka_img")
                }
            )
            Spacer(Modifier.size(2.dp))
            ModernSettingsItem(
                icon = ImageVector.vectorResource(R.drawable.document_outline_28),
                iconBackgroundColor = generateColorFromHash("Лицензия"),
                title = "Лицензия",
                subtitle = "Приложение распространяется по GPL-3.0",
                position = ItemPosition.MIDDLE,
                onClick = {
                    openUrlInBrowser(context, "https://github.com/rozetkaimg/Eltech/blob/main/LICENSE")
                }
            )
            Spacer(Modifier.size(2.dp))
            ModernSettingsItem(
                icon = ImageVector.vectorResource(R.drawable.code_24),
                iconBackgroundColor = generateColorFromHash("Исходный код"),
                title = "Исходный код",
                subtitle = "Github.com",
                position = ItemPosition.MIDDLE,
                onClick = {
                    openUrlInBrowser(context, "https://github.com/rozetkaimg/Eltech")
                }
            )
            Spacer(Modifier.size(2.dp))
            ModernSettingsItem(
                icon = Icons.Outlined.Web,
                iconBackgroundColor = generateColorFromHash(stringResource(R.string.app_info_website_label)),
                title = stringResource(R.string.app_info_website_label),
                subtitle = stringResource(R.string.app_info_website_value),
                position = ItemPosition.BOTTOM,
                onClick = {
                    openUrlInBrowser(context, "https://mospolytech.ru/")
                }
            )
            Spacer(Modifier.size(getNavigationBarHeightDp()))
        }
    }
}


var clickCount = 0
private fun handleButtonClickWithToast(context: Context) {
    clickCount++

    when (clickCount) {
        1 -> {
            Toast.makeText(context, context.getString(R.string.about_toast_1), Toast.LENGTH_SHORT).show()
        }
        2 -> {
            Toast.makeText(context, context.getString(R.string.about_toast_2), Toast.LENGTH_SHORT).show()
        }
        3 -> {
            Toast.makeText(context, context.getString(R.string.about_toast_3), Toast.LENGTH_SHORT).show()
            val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
            }
            context.startActivity(intent)

            clickCount = 0
        }
    }
}