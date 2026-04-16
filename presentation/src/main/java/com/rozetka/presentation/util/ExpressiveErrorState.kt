package com.rozetka.presentation.util


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveErrorState(
    message: String,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(Cookie9Sided.toShape())
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Произошла ошибка",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onUpdate,
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Повторить",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveErrorStateTop(
    message: String,
    onUpdate: () -> Unit,
    title: String,
    onBack:  () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),

        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =""
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Произошла ошибка",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onUpdate,
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 32.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Повторить",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}