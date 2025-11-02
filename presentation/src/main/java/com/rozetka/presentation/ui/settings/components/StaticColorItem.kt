package com.rozetka.presentation.ui.settings.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
    onClick: () -> Unit,
    position: Int,
    colorOne: Color,
    colorTwo: Color,
    colorThree: Color,
) {

    Card(
        Modifier
            .size(82.dp)
            .border(
                if (ColorThemeState.value == position) 2.dp else 0.dp,
                if (ColorThemeState.value == position) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(22.dp)
            ),
        RoundedCornerShape(22.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Card(
                Modifier
                    .height(72.dp)
                    .width(72.dp)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onClick
                            ColorThemeState.value = position

                        }
                ) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .height(60.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(100)
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            Box(
                                Modifier
                                    .height(30.dp)
                                    .fillMaxWidth()
                                    .background(
                                        colorOne
                                    )
                            ) {

                            }
                            Box(
                                Modifier
                                    .size(30.dp)
                                    .background(
                                        colorTwo
                                    )
                                    .align(Alignment.BottomStart)
                            ) {

                            }
                            Box(
                                Modifier
                                    .size(30.dp)
                                    .background(
                                        colorThree
                                    )
                                    .align(Alignment.BottomEnd)
                            ) {

                            }
                        }

                    }

                }
            }
        }

    }
}
