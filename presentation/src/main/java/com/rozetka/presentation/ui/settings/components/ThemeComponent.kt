package com.rozetka.presentation.ui.settings.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rozetka.presentation.R
import com.rozetka.presentation.util.ChangeStatusBarIconsColor
import com.rozetka.presentation.util.ThemeObject.DarkThemeState
import com.rozetka.presentation.util.ThemeObject.StatusBarIconColor
import com.rozetka.presentation.util.getStatusBarIconState

@Composable
fun ThemeComponent(
    onCheckedChange_System: () -> Unit,
    onCheckedChange_Light: () -> Unit,
    onCheckedChange_Dark: () -> Unit,
) {
    var statusBarIcon by remember { mutableStateOf(value = !StatusBarIconColor.value) }
    ChangeStatusBarIconsColor(statusBarIcon)
    var statusBarIconColor = getStatusBarIconState()

    Row(
        Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 16.dp
        ),
    ) {

        Box(Modifier.weight(1f)) {
            ThemeItem({
                DarkThemeState.value = 0
                StatusBarIconColor.value = statusBarIconColor
                onCheckedChange_System()
                statusBarIcon = !statusBarIconColor
            }, stringResource(id = R.string.systemic), Icons.Rounded.Android, 0)
        }
        Spacer(modifier = Modifier.size(8.dp))
        Box(Modifier.weight(1f)) {
            ThemeItem(
                {
                    DarkThemeState.value = 1
                    StatusBarIconColor.value = true
                    statusBarIcon = false
                    onCheckedChange_Light()
                },
                stringResource(id = R.string.light),
                Icons.Rounded.LightMode,
                1
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Box(Modifier.weight(1f)) {
            ThemeItem(
                {
                    DarkThemeState.value = 2
                    StatusBarIconColor.value = false
                    statusBarIcon = true
                    onCheckedChange_Dark()
                },
                stringResource(id = R.string.dark),
                Icons.Rounded.DarkMode,
                2
            )
        }


    }
}