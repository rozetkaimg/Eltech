package com.rozetka.presentation.ui.settings.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rozetka.presentation.R
import com.rozetka.presentation.util.ThemeObject.DynamicColorState

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MonetItem(
    defaultState: Boolean,
    label: String,
    onClick: () -> Unit,
    colorOne: Color,
    colorTwo: Color,
    colorThree: Color
) {

    Row(
        Modifier
            .padding(
                16.dp, 0.dp
            )
            .fillMaxWidth()
    ) {
        Column(Modifier.weight(1f)) {
            Card(
                Modifier
                    .height(90.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (DynamicColorState.value == defaultState) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }

                        )
                        .clickable {
                            onClick()

                        }
                ) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .height(50.dp)
                            .width(50.dp),
                        shape = RoundedCornerShape(100)
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            Box(
                                Modifier
                                    .height(25.dp)
                                    .fillMaxWidth()
                                    .background(
                                        colorOne
                                    )
                            ) {

                            }
                            Box(
                                Modifier
                                    .size(25.dp)
                                    .background(
                                        colorTwo
                                    )
                                    .align(Alignment.BottomStart)
                            ) {

                            }
                            Box(
                                Modifier
                                    .size(25.dp)
                                    .background(
                                        colorThree
                                    )
                                    .align(Alignment.BottomEnd)
                            ) {

                            }
                        }

                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedVisibility(
                            visible = DynamicColorState.value == defaultState,
                            enter = scaleIn(),
                            exit = scaleOut(),

                        ) {

                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(28.dp))
                                    .height(50.dp)
                                    .width(50.dp)

                            ) {

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_done),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(5.dp)
                                )

                            }
                        }
                    }
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
}

