package com.rozetka.presentation.ui.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rozetka.presentation.util.IDownloadUtils

@Composable
fun ImageViewer(
    images: List<String>,
    startIndex: Int = 0,
    onDismiss: () -> Unit,
    autoDownload: Boolean = true,
    onPageChanged: ((Int) -> Unit)? = null,
    onForward: (String) -> Unit = {},
    onDelete: ((String) -> Unit)? = null,
    onCopyLink: ((String) -> Unit)? = null,
    onCopyText: ((String) -> Unit)? = null,
    onVideoClick: ((String) -> Unit)? = null,
    captions: List<String?> = emptyList(),
    imageDownloadingStates: List<Boolean> = emptyList(),
    imageDownloadProgressStates: List<Float> = emptyList(),
    downloadUtils: IDownloadUtils,
    showImageNumber: Boolean = true
) {
    MediaViewer(
        mediaItems = images,
        startIndex = startIndex,
        onDismiss = onDismiss,
        onPageChanged = onPageChanged,
        showImageNumber = showImageNumber
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewer(
    mediaItems: List<String>,
    startIndex: Int = 0,
    onDismiss: () -> Unit,
    autoDownload: Boolean = true,
    onPageChanged: ((Int) -> Unit)? = null,
    onForward: (String) -> Unit = {},
    onDelete: ((String) -> Unit)? = null,
    onCopyLink: ((String) -> Unit)? = null,
    onCopyText: ((String) -> Unit)? = null,
    captions: List<String?> = emptyList(),
    imageDownloadingStates: List<Boolean> = emptyList(),
    imageDownloadProgressStates: List<Float> = emptyList(),
    downloadUtils: IDownloadUtils? = null,
    showImageNumber: Boolean = true
) {
    val pagerState = rememberPagerState(initialPage = startIndex) { mediaItems.size }
    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged?.invoke(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val imageUrl = mediaItems.getOrNull(page) ?: ""
            ZoomableImage(
                imageUrl = imageUrl,
                onTap = { showControls = !showControls }
            )
        }

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }

                if (showImageNumber && mediaItems.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1} из ${mediaItems.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    onTap: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        val maxOffsetX = (size.width * (scale - 1)) / 2
                        val maxOffsetY = (size.height * (scale - 1)) / 2
                        offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                        offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            }
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onTap
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }
}