package com.rozetka.presentation.ui.settings.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rozetka.presentation.util.ThemeObject.DarkThemeState

@Composable
fun ThemeItem(onClick: () -> Unit, label: String, icon: ImageVector, isActive: Int) {
    Column {
        Card(
            Modifier
                .height(85.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(25.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (DarkThemeState.value == isActive) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable {
                        onClick()
                    }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    Modifier.align(Alignment.Center),
                    tint = if (DarkThemeState.value == 2) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
        Text(
            text = label,
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }

}