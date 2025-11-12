package com.rozetka.presentation.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rozetka.presentation.util.ThemeObject.ColorThemeState

@Composable
fun StaticColorItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    position: Int,
    colorOne: Color,
    colorTwo: Color,
    colorThree: Color,
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
    ) {
        val totalSize = maxWidth

        val outerPadding = totalSize * (5f / 82f)
        val innerPadding = totalSize * (6f / 82f)

        val outerCornerRadius = totalSize * (22f / 82f)
        val innerCornerRadius = totalSize * (18f / 82f)

        val borderWidth = if (ColorThemeState.value == position) totalSize * (2f / 82f) else 0.dp

        Card(
            Modifier
                .fillMaxSize()
                .border(
                    width = borderWidth,
                    color = if (ColorThemeState.value == position) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(outerCornerRadius)
                ),
            shape = RoundedCornerShape(outerCornerRadius)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(outerPadding),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    Modifier
                        .fillMaxSize(),
                    shape = RoundedCornerShape(innerCornerRadius)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                onClick()
                                ColorThemeState.value = position
                            }
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize(),
                            shape = CircleShape
                        ) {
                            Box(Modifier.fillMaxSize()) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.5f)
                                        .background(colorOne)
                                        .align(Alignment.TopCenter)
                                )
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.5f)
                                        .fillMaxHeight(0.5f)
                                        .background(colorTwo)
                                        .align(Alignment.BottomStart)
                                )
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.5f)
                                        .fillMaxHeight(0.5f)
                                        .background(colorThree)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}