@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationApi::class
)

package com.rozetka.presentation.ui.gallery

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sign

// --- MODELS ---
enum class GalleryFilter { All, Photos, Videos }

sealed class BucketFilter(val key: String) {
    data object All : BucketFilter("all")
    data object Camera : BucketFilter("camera")
    data object Screenshots : BucketFilter("screenshots")
    data class Custom(val name: String) : BucketFilter("custom_$name")
}

data class GalleryMediaItem(
    val uri: Uri,
    val dateAdded: Long,
    val isVideo: Boolean,
    val bucketName: String,
    val relativePath: String,
    val isCamera: Boolean,
    val isScreenshot: Boolean
)

// --- UTILS ---
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun getMimeType(url: String): String? {
    val extension = MimeTypeMap.getFileExtensionFromUrl(url) ?: return null
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
}

private fun isVideoPath(path: String, mimeType: String?): Boolean {
    if (path.isBlank()) return false
    if (mimeType?.startsWith("image/") == true) return false
    return mimeType?.startsWith("video/") == true ||
            path.endsWith(".mp4", ignoreCase = true) ||
            path.endsWith(".mkv", ignoreCase = true) ||
            path.endsWith(".mov", ignoreCase = true) ||
            path.endsWith(".webm", ignoreCase = true) ||
            path.endsWith(".avi", ignoreCase = true) ||
            path.endsWith(".3gp", ignoreCase = true) ||
            path.endsWith(".m4v", ignoreCase = true)
}

// --- MAIN SCREEN ---
@Composable
fun GalleryScreen(
    onMediaSelected: (List<Uri>) -> Unit,
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    hasMediaAccess: Boolean,
    isPartialAccess: Boolean,
    onPickFromOtherSources: () -> Unit,
    onRequestMediaAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaList = remember { mutableStateListOf<GalleryMediaItem>() }
    val selectedMedia = remember { mutableStateListOf<Uri>() }
    var filter by remember { mutableStateOf(GalleryFilter.All) }
    var bucketFilter by remember { mutableStateOf<BucketFilter>(BucketFilter.All) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(hasMediaAccess) {
        if (!hasMediaAccess) {
            mediaList.clear()
            selectedMedia.clear()
            return@LaunchedEffect
        }
        isLoading = true
        val loaded = withContext(Dispatchers.IO) {
            val images = queryImages(context)
            val videos = queryVideos(context)
            (images + videos).sortedByDescending { it.dateAdded }
        }
        Log.d("GalleryDebug", "Найдено файлов: ${loaded.size}")
        mediaList.clear()
        mediaList.addAll(loaded)
        isLoading = false
    }

    val buckets by remember { derivedStateOf { buildBuckets(mediaList) } }

    LaunchedEffect(filter, buckets) {
        if (filter != GalleryFilter.Photos) return@LaunchedEffect
        if (bucketFilter !in buckets) bucketFilter = BucketFilter.All
    }

    val filteredMedia = mediaList.filter { item ->
        val byType = when (filter) {
            GalleryFilter.All -> true
            GalleryFilter.Photos -> !item.isVideo
            GalleryFilter.Videos -> item.isVideo
        }
        val byBucket = if (filter == GalleryFilter.Photos) {
            when (val b = bucketFilter) {
                BucketFilter.All -> true
                BucketFilter.Camera -> item.isCamera
                BucketFilter.Screenshots -> item.isScreenshot
                is BucketFilter.Custom -> item.bucketName.equals(b.name, ignoreCase = true)
            }
        } else true
        byType && byBucket
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Вложения", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "Закрыть") }
                },
                actions = {
                    IconButton(onClick = onPickFromOtherSources) {
                        Icon(Icons.Filled.Folder, "Файлы")
                    }
                    IconButton(onClick = onCameraClick) {
                        Icon(Icons.Filled.PhotoCamera, "Камера")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = selectedMedia.isNotEmpty(),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    FloatingActionButton(
                        onClick = { onMediaSelected(selectedMedia.toList()) },
                        containerColor = Color(0xFF2E7D32), // Dark green for sending
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedMedia.size.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.Send, "Отправить")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
        ) {
            if (!hasMediaAccess) {
                Text("Разрешения", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.PermMedia, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("Приложению нужен доступ к медиа", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = onRequestMediaAccess) { Text("Предоставить доступ") }
                    }
                }
                return@Column
            }

            if (isPartialAccess) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PermMedia, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Частичный доступ", fontWeight = FontWeight.Bold)
                            Text("Доступ не ко всем фото", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = onRequestMediaAccess) { Text("Управление") }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Tabs
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(GalleryFilter.All to "Все", GalleryFilter.Photos to "Фото", GalleryFilter.Videos to "Видео").forEach { (f, label) ->
                            val selected = filter == f
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .selectable(selected = selected, onClick = { filter = f }, role = Role.Tab)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Folders
                    AnimatedVisibility(visible = filter == GalleryFilter.Photos) {
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(buckets, key = { it.key }) { bucket ->
                                val label = when(bucket) {
                                    BucketFilter.All -> "Все папки"
                                    BucketFilter.Camera -> "Камера"
                                    BucketFilter.Screenshots -> "Скриншоты"
                                    is BucketFilter.Custom -> bucket.name
                                }
                                FilterChip(
                                    selected = bucket == bucketFilter,
                                    onClick = { bucketFilter = bucket },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    // Grid
                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(bottom = 80.dp) // space for FAB
                        ) {
                            itemsIndexed(filteredMedia, key = { _, item -> item.uri.toString() }) { _, item ->
                                val isSelected = selectedMedia.contains(item.uri)
                                val scale by animateFloatAsState(targetValue = if (isSelected) 0.9f else 1f, label = "")
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .scale(scale)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { if (isSelected) selectedMedia.remove(item.uri) else selectedMedia.add(item.uri) }
                                        .border(if (isSelected) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(model = item.uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    if (item.isVideo) {
                                        Surface(color = Color.Black.copy(0.6f), shape = RoundedCornerShape(4.dp), modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)) {
                                            Text("ВИДЕО", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                    if (isSelected) {
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)))
                                        Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- QUERIES ---
private fun buildBuckets(media: List<GalleryMediaItem>): List<BucketFilter> {
    val seen = mutableSetOf<String>()
    val custom = media.asSequence().map { it.bucketName.trim() }.filter { it.isNotBlank() }
        .filterNot { it.equals("Camera", true) || it.equals("Screenshots", true) }
        .sortedBy { it.lowercase() }.filter { seen.add(it.lowercase()) }
        .map { BucketFilter.Custom(it) }.toList()
    return listOf(BucketFilter.All, BucketFilter.Camera, BucketFilter.Screenshots) + custom
}

private fun queryImages(context: Context): List<GalleryMediaItem> {
    val result = mutableListOf<GalleryMediaItem>()
    val projection = mutableListOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_ADDED,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            add(MediaStore.Images.Media.RELATIVE_PATH)
        } else {
            add("bucket_display_name")
        }
    }.toTypedArray()

    try {
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val bucketCol = cursor.getColumnIndex("bucket_display_name")
            val relCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
            } else -1

            while (cursor.moveToNext()) {
                val bucket = if (bucketCol != -1) cursor.getString(bucketCol) ?: "" else ""
                val relative = if (relCol != -1) cursor.getString(relCol) ?: "" else ""
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                val date = cursor.getLong(dateCol)
                
                result.add(
                    GalleryMediaItem(
                        uri = uri,
                        dateAdded = date,
                        isVideo = false,
                        bucketName = bucket,
                        relativePath = relative,
                        isCamera = bucket.lowercase().contains("camera") || relative.lowercase().contains("dcim"),
                        isScreenshot = bucket.lowercase().contains("screenshot") || relative.lowercase().contains("screenshot")
                    )
                )
            }
        }
    } catch (e: Exception) {
        Log.e("GalleryDebug", "Error querying images", e)
    }
    return result
}

private fun queryVideos(context: Context): List<GalleryMediaItem> {
    val result = mutableListOf<GalleryMediaItem>()
    val projection = mutableListOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DATE_ADDED,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            add(MediaStore.Video.Media.RELATIVE_PATH)
        } else {
            add("bucket_display_name")
        }
    }.toTypedArray()

    try {
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val bucketCol = cursor.getColumnIndex("bucket_display_name")
            val relCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
            } else -1

            while (cursor.moveToNext()) {
                val bucket = if (bucketCol != -1) cursor.getString(bucketCol) ?: "" else ""
                val relative = if (relCol != -1) cursor.getString(relCol) ?: "" else ""
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                val date = cursor.getLong(dateCol)

                result.add(
                    GalleryMediaItem(
                        uri = uri,
                        dateAdded = date,
                        isVideo = true,
                        bucketName = bucket,
                        relativePath = relative,
                        isCamera = bucket.lowercase().contains("camera") || relative.lowercase().contains("dcim"),
                        isScreenshot = bucket.lowercase().contains("screenshot") || relative.lowercase().contains("screenshot")
                    )
                )
            }
        }
    } catch (e: Exception) {
        Log.e("GalleryDebug", "Error querying videos", e)
    }
    return result
}


// ============================================================================
// MEDIA VIEWER & GESTURES
// ============================================================================

class ZoomState {
    val scale = Animatable(1f)
    val offsetX = Animatable(0f)
    val offsetY = Animatable(0f)

    fun resetInstant(scope: CoroutineScope) {
        scope.launch {
            scale.snapTo(1f)
            offsetX.snapTo(0f)
            offsetY.snapTo(0f)
        }
    }

    fun onDoubleTap(scope: CoroutineScope, tap: Offset, targetScale: Float, size: IntSize) {
        scope.launch {
            if (targetScale == 1f) {
                launch { scale.animateTo(1f, spring()) }
                launch { offsetX.animateTo(0f, spring()) }
                launch { offsetY.animateTo(0f, spring()) }
                return@launch
            }

            val currentScale = scale.value
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val zoomFactor = targetScale / currentScale
            val tapXFromCenter = tap.x - centerX
            val tapYFromCenter = tap.y - centerY

            val targetOffsetX = (tapXFromCenter * (1 - zoomFactor)) + (offsetX.value * zoomFactor)
            val targetOffsetY = (tapYFromCenter * (1 - zoomFactor)) + (offsetY.value * zoomFactor)

            val maxOffsetX = (size.width * (targetScale - 1f)) / 2f
            val maxOffsetY = (size.height * (targetScale - 1f)) / 2f

            val clampedX = targetOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
            val clampedY = targetOffsetY.coerceIn(-maxOffsetY, maxOffsetY)

            launch { scale.animateTo(targetScale, spring()) }
            launch { offsetX.animateTo(clampedX, spring()) }
            launch { offsetY.animateTo(clampedY, spring()) }
        }
    }

    fun onTransform(scope: CoroutineScope, pan: Offset, zoomFactor: Float, size: IntSize, maxZoom: Float) {
        scope.launch {
            val newScale = (scale.value * zoomFactor).coerceIn(1f, maxZoom)
            scale.snapTo(newScale)

            if (scale.value > 1f) {
                val newX = offsetX.value + pan.x
                val newY = offsetY.value + pan.y
                val maxX = (size.width * (scale.value - 1f)) / 2f
                val maxY = (size.height * (scale.value - 1f)) / 2f
                offsetX.snapTo(newX.coerceIn(-maxX, maxX))
                offsetY.snapTo(newY.coerceIn(-maxY, maxY))
            } else {
                offsetX.snapTo(0f)
                offsetY.snapTo(0f)
            }
        }
    }

    fun ensureBounds(screenW: Float, screenH: Float, scope: CoroutineScope) {
        scope.launch {
            val maxX = (screenW * (scale.value - 1f)) / 2f
            val maxY = (screenH * (scale.value - 1f)) / 2f
            launch { offsetX.animateTo(offsetX.value.coerceIn(-maxX, maxX), spring()) }
            launch { offsetY.animateTo(offsetY.value.coerceIn(-maxY, maxY), spring()) }
            if (scale.value < 1f) launch { scale.animateTo(1f, spring()) }
        }
    }
}

class DismissRootState {
    val offsetY = Animatable(0f)
    val scale = Animatable(0.8f)
    val backgroundAlpha = Animatable(0f)

    fun resetInstant(scope: CoroutineScope) {
        scope.launch {
            offsetY.snapTo(0f)
            scale.snapTo(1f)
            backgroundAlpha.snapTo(1f)
        }
    }

    suspend fun animateExit(targetY: Float) = coroutineScope {
        launch {
            backgroundAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 150, easing = LinearEasing)
            )
        }
        launch {
            offsetY.animateTo(
                targetValue = targetY,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )
        }
    }

    suspend fun animateRestore() = coroutineScope {
        launch { offsetY.animateTo(0f, tween(150, easing = FastOutSlowInEasing)) }
        launch { scale.animateTo(1f, tween(150)) }
        launch { backgroundAlpha.animateTo(1f, tween(150)) }
    }

    suspend fun drag(delta: Float) {
        val currentY = offsetY.value
        val targetY = currentY + delta
        offsetY.snapTo(targetY)
        val progress = (targetY / 1000f).absoluteValue.coerceIn(0f, 1f)
        scale.snapTo(1f - (progress * 0.15f))
        backgroundAlpha.snapTo(1f - (progress * 0.8f))
    }
}

@Composable
fun rememberZoomState(): ZoomState = remember { ZoomState() }

@Composable
fun rememberDismissRootState(): DismissRootState = remember { DismissRootState() }

suspend fun PointerInputScope.detectZoomAndDismissGestures(
    zoomState: ZoomState,
    rootState: DismissRootState,
    screenHeightPx: Float,
    dismissThreshold: Float,
    dismissVelocityThreshold: Float,
    onDismiss: () -> Unit,
    scope: CoroutineScope
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val tracker = VelocityTracker()
        tracker.addPointerInputChange(down)

        val touchSlop = viewConfiguration.touchSlop
        var pan = Offset.Zero
        var isZooming = false
        var isVerticalDrag = false

        while (true) {
            val event = awaitPointerEvent()
            if (event.changes.any { it.isConsumed }) break

            val pointerCount = event.changes.size
            event.changes.forEach { tracker.addPointerInputChange(it) }

            val zoomChange = event.calculateZoom()
            val panChange = event.calculatePan()

            if (pointerCount > 1) isZooming = true

            if (!isZooming && !isVerticalDrag && zoomState.scale.value == 1f && pointerCount == 1) {
                pan += panChange
                val totalPan = pan.getDistance()
                if (totalPan > touchSlop) {
                    if (abs(pan.y) > abs(pan.x) * 2f) {
                        isVerticalDrag = true
                    } else if (abs(pan.x) > touchSlop) {
                        return@awaitEachGesture
                    }
                }
            }

            if (isZooming || zoomState.scale.value > 1f) {
                zoomState.onTransform(scope, panChange, zoomChange, IntSize(size.width, size.height), 3f)
                event.changes.forEach { if (it.positionChanged()) it.consume() }
            } else if (isVerticalDrag) {
                scope.launch { rootState.drag(panChange.y) }
                event.changes.forEach { if (it.positionChanged()) it.consume() }
            }

            if (!event.changes.any { it.pressed }) break
        }

        val velocity = tracker.calculateVelocity()
        if (zoomState.scale.value > 1f) {
            zoomState.ensureBounds(size.width.toFloat(), size.height.toFloat(), scope)
        } else if (isVerticalDrag) {
            val offsetY = rootState.offsetY.value
            val shouldDismiss = abs(offsetY) > dismissThreshold || abs(velocity.y) > dismissVelocityThreshold
            if (shouldDismiss) {
                scope.launch {
                    rootState.animateExit(screenHeightPx * sign(offsetY))
                    onDismiss()
                }
            } else {
                scope.launch { rootState.animateRestore() }
            }
        }
    }
}

// --- MEDIA VIEWER CORE ---

@Composable
fun ImageViewer(
    images: List<String>,
    startIndex: Int = 0,
    onDismiss: () -> Unit,
    onPageChanged: ((Int) -> Unit)? = null,
    onForward: (String) -> Unit = {},
    onDelete: ((String) -> Unit)? = null,
    onCopyLink: ((String) -> Unit)? = null,
    onCopyText: ((String) -> Unit)? = null,
    onDownload: ((String) -> Unit)? = null,
    captions: List<String?> = emptyList(),
    imageDownloadingStates: List<Boolean> = emptyList(),
    imageDownloadProgressStates: List<Float> = emptyList(),
    showImageNumber: Boolean = true
) {
    MediaViewer(
        mediaItems = images,
        startIndex = startIndex,
        onDismiss = onDismiss,
        onPageChanged = onPageChanged,
        onForward = onForward,
        onDelete = onDelete,
        onCopyLink = onCopyLink,
        onCopyText = onCopyText,
        onDownload = onDownload,
        captions = captions,
        imageDownloadingStates = imageDownloadingStates,
        imageDownloadProgressStates = imageDownloadProgressStates,
        showImageNumber = showImageNumber
    )
}

@Composable
fun MediaViewer(
    mediaItems: List<String>,
    startIndex: Int = 0,
    onDismiss: () -> Unit,
    onPageChanged: ((Int) -> Unit)? = null,
    onForward: (String) -> Unit = {},
    onDelete: ((String) -> Unit)? = null,
    onCopyLink: ((String) -> Unit)? = null,
    onCopyText: ((String) -> Unit)? = null,
    onDownload: ((String) -> Unit)? = null,
    captions: List<String?> = emptyList(),
    imageDownloadingStates: List<Boolean> = emptyList(),
    imageDownloadProgressStates: List<Float> = emptyList(),
    showImageNumber: Boolean = true,
    isAlwaysVideo: Boolean = false
) {
    require(mediaItems.isNotEmpty()) { "mediaItems can't be empty" }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { mediaItems.size }
    )

    val rootState = rememberDismissRootState()
    val zoomState = rememberZoomState()

    var showControls by remember { mutableStateOf(true) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }
    val dismissDistancePx = with(density) { 160.dp.toPx() }
    val dismissVelocityThreshold = with(density) { 1000.dp.toPx() }

    LaunchedEffect(Unit) {
        launch { rootState.scale.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)) }
        launch { rootState.backgroundAlpha.animateTo(1f, tween(150)) }
    }

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged?.invoke(pagerState.currentPage)
        zoomState.resetInstant(scope)
        rootState.resetInstant(scope)
        showSettingsMenu = false
    }

    LaunchedEffect(showControls) {
        if (!showControls) showSettingsMenu = false
        context.findActivity()?.let {
            val insetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (showControls) insetsController.show(WindowInsetsCompat.Type.systemBars())
            else insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    DisposableEffect(context) {
        onDispose {
            context.findActivity()?.let {
                WindowCompat.getInsetsController(it.window, it.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    BackHandler {
        if (showSettingsMenu) showSettingsMenu = false
        else onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = rootState.backgroundAlpha.value))
            .graphicsLayer {
                translationY = rootState.offsetY.value
                scaleX = rootState.scale.value
                scaleY = rootState.scale.value
            }
    ) {
        HorizontalPager(
            state = pagerState,
            key = { page -> "media_page_${page}" },
            pageSize = PageSize.Fill,
            pageSpacing = 0.dp,
            beyondViewportPageCount = 0,
            userScrollEnabled = zoomState.scale.value == 1f && rootState.offsetY.value == 0f
        ) { page ->
            val path = mediaItems.getOrNull(page) ?: return@HorizontalPager
            val mimeType = getMimeType(path)
            val isVideo = (isAlwaysVideo && path.isNotBlank()) || isVideoPath(path, mimeType)

            if (isVideo) {
                // Video Stub (Заглушка, так как VideoPage не был предоставлен)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Видео плеер (заглушка)", color = Color.White)
                }
            } else {
                ImagePage(
                    path = path,
                    isDownloading = imageDownloadingStates.getOrNull(page) == true,
                    downloadProgress = imageDownloadProgressStates.getOrNull(page) ?: 0f,
                    zoomState = zoomState,
                    rootState = rootState,
                    screenHeightPx = screenHeightPx,
                    dismissDistancePx = dismissDistancePx,
                    dismissVelocityThreshold = dismissVelocityThreshold,
                    onDismiss = onDismiss,
                    showControls = showControls,
                    onToggleControls = { showControls = !showControls },
                    pageIndex = page,
                    pagerIndex = pagerState.currentPage
                )
            }
        }

        val currentPath = mediaItems.getOrNull(pagerState.currentPage) ?: ""
        val currentMimeType = getMimeType(currentPath)
        val isCurrentVideo = (isAlwaysVideo && currentPath.isNotBlank()) || isVideoPath(currentPath, currentMimeType)

        if (!isCurrentVideo) {
            ImageOverlay(
                showControls = showControls,
                rootState = rootState,
                pagerState = pagerState,
                mediaItems = mediaItems,
                captions = captions,
                showImageNumber = showImageNumber,
                onDismiss = onDismiss,
                showSettingsMenu = showSettingsMenu,
                onToggleSettings = { showSettingsMenu = !showSettingsMenu },
                onDownload = onDownload,
                onForward = onForward,
                onDelete = onDelete,
                onCopyLink = onCopyLink,
                onCopyText = onCopyText
            )
        }
    }
}

// --- VIEWER UI COMPONENTS ---

@Composable
fun ImagePage(
    path: String,
    isDownloading: Boolean,
    downloadProgress: Float,
    zoomState: ZoomState,
    rootState: DismissRootState,
    screenHeightPx: Float,
    dismissDistancePx: Float,
    dismissVelocityThreshold: Float,
    onDismiss: () -> Unit,
    showControls: Boolean,
    onToggleControls: () -> Unit,
    pageIndex: Int,
    pagerIndex: Int
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        val currentScale = zoomState.scale.value
                        val targetScale = if (currentScale > 1.1f) 1f else 3f
                        zoomState.onDoubleTap(scope, offset, targetScale, size)
                    },
                    onTap = { onToggleControls() }
                )
            }
            .pointerInput(pagerIndex) {
                detectZoomAndDismissGestures(
                    zoomState = zoomState,
                    rootState = rootState,
                    screenHeightPx = screenHeightPx,
                    dismissThreshold = dismissDistancePx,
                    dismissVelocityThreshold = dismissVelocityThreshold,
                    onDismiss = onDismiss,
                    scope = scope
                )
            }
    ) {
        ZoomableImage(
            data = path,
            isDownloading = isDownloading,
            downloadProgress = downloadProgress,
            zoomState = zoomState,
            pageIndex = pageIndex,
            pagerIndex = pagerIndex
        )
    }
}

@Composable
fun ImageOverlay(
    showControls: Boolean,
    rootState: DismissRootState,
    pagerState: PagerState,
    mediaItems: List<String>,
    captions: List<String?>,
    showImageNumber: Boolean,
    onDismiss: () -> Unit,
    showSettingsMenu: Boolean,
    onToggleSettings: () -> Unit,
    onDownload: ((String) -> Unit)?,
    onForward: (String) -> Unit,
    onDelete: ((String) -> Unit)?,
    onCopyLink: ((String) -> Unit)?,
    onCopyText: ((String) -> Unit)?
) {
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = showControls && rootState.offsetY.value == 0f,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.fillMaxSize()) {
            ViewerTopBar(
                onBack = onDismiss,
                onActionClick = onToggleSettings,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val currentCaption = captions.getOrNull(pagerState.currentPage)
                if (!currentCaption.isNullOrBlank()) {
                    Text(
                        text = currentCaption,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                if (mediaItems.size > 1) {
                    ThumbnailStrip(
                        images = mediaItems,
                        pagerState = pagerState,
                        scope = scope
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    if (showImageNumber) {
                        PageIndicator(
                            current = pagerState.currentPage + 1,
                            total = mediaItems.size
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showSettingsMenu && showControls,
        enter = fadeIn(tween(150)) + scaleIn(
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
            initialScale = 0.8f,
            transformOrigin = TransformOrigin(1f, 0f)
        ),
        exit = fadeOut(tween(150)) + scaleOut(
            animationSpec = tween(150),
            targetScale = 0.9f,
            transformOrigin = TransformOrigin(1f, 0f)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onToggleSettings()
                }
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 56.dp, end = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            val currentIndex = pagerState.currentPage
            val currentItem = mediaItems.getOrNull(currentIndex)

            if (currentItem != null) {
                val currentCaption = captions.getOrNull(currentIndex)
                ImageSettingsMenu(
                    onDownload = onDownload?.let {
                        { it(currentItem); onToggleSettings() }
                    },
                    onCopyLink = onCopyLink?.let {
                        { it(currentItem); onToggleSettings() }
                    },
                    onCopyText = if (!currentCaption.isNullOrBlank()) {
                        { onCopyText?.invoke(currentItem); onToggleSettings() }
                    } else null,
                    onForward = {
                        onForward(currentItem)
                        onToggleSettings()
                    },
                    onDelete = onDelete?.let {
                        {
                            it(currentItem)
                            onToggleSettings()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ViewerTopBar(
    onBack: () -> Unit,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
        },
        actions = {
            IconButton(onClick = onActionClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Меню", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier
    )
}

@Composable
fun ImageSettingsMenu(
    onDownload: (() -> Unit)?,
    onCopyLink: (() -> Unit)?,
    onCopyText: (() -> Unit)? = null,
    onForward: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        modifier = Modifier.widthIn(min = 200.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (onDownload != null) {
                MenuOptionRow(icon = Icons.Rounded.Download, title = "Скачать", onClick = onDownload)
            }
            if (onCopyText != null) {
                MenuOptionRow(icon = Icons.Rounded.ContentCopy, title = "Копировать текст", onClick = onCopyText)
            }
            if (onCopyLink != null) {
                MenuOptionRow(icon = Icons.Rounded.Link, title = "Копировать ссылку", onClick = onCopyLink)
            }
            MenuOptionRow(icon = Icons.AutoMirrored.Rounded.Forward, title = "Переслать", onClick = onForward)

            if (onDelete != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                MenuOptionRow(
                    icon = Icons.Rounded.Delete,
                    title = "Удалить",
                    onClick = onDelete,
                    iconTint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun MenuOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = textColor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ThumbnailStrip(
    images: List<Any>,
    pagerState: PagerState,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    thumbnailSize: Dp = 60.dp,
    thumbnailSpacing: Dp = 8.dp
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val context = LocalContext.current

    LaunchedEffect(pagerState.currentPage) {
        val viewportWidth = listState.layoutInfo.viewportSize.width
        if (viewportWidth > 0) {
            val itemSizePx = with(density) { thumbnailSize.toPx() }
            val centerOffset = (viewportWidth / 2) - (itemSizePx / 2)
            listState.animateScrollToItem(
                index = pagerState.currentPage,
                scrollOffset = -centerOffset.toInt()
            )
        } else {
            listState.animateScrollToItem(pagerState.currentPage)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .wrapContentWidth()
            .height(thumbnailSize + 24.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(thumbnailSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(
            images,
            key = { index, _ -> "thumb_$index" }
        ) { index, image ->
            val isSelected = pagerState.currentPage == index
            val scale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 0.9f, label = "scale")
            val alpha by animateFloatAsState(targetValue = if (isSelected) 1f else 0.5f, label = "alpha")
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "border"
            )
            val borderWidth by animateDpAsState(targetValue = if (isSelected) 2.dp else 0.dp, label = "width")

            val request = remember(image) {
                ImageRequest.Builder(context)
                    .data(image)
                    .crossfade(true)
                    .build()
            }

            Box(
                modifier = Modifier
                    .size(thumbnailSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
                    .clickable {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
            ) {
                AsyncImage(
                    model = request,
                    contentDescription = "Миниатюра $index",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun PageIndicator(modifier: Modifier = Modifier, current: Int, total: Int) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = CircleShape
    ) {
        Text(
            text = "$current из $total",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
    }
}

@Composable
fun ZoomableImage(
    data: Any,
    isDownloading: Boolean,
    downloadProgress: Float,
    zoomState: ZoomState,
    pageIndex: Int,
    pagerIndex: Int
) {
    val applyTransforms = pageIndex == pagerIndex
    val context = LocalContext.current
    var isHighResLoading by remember(data) { mutableStateOf(true) }

    val thumbnailRequest = remember(data) {
        ImageRequest.Builder(context)
            .data(data)
            .size(100, 100)
            .crossfade(true)
            .build()
    }

    val fullRequest = remember(data) {
        ImageRequest.Builder(context)
            .data(data)
            .size(Size.ORIGINAL)
            .precision(Precision.EXACT)
            .crossfade(true)
            .build()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = thumbnailRequest,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (applyTransforms) {
                        translationX = zoomState.offsetX.value
                        translationY = zoomState.offsetY.value
                        scaleX = zoomState.scale.value
                        scaleY = zoomState.scale.value
                    }
                }
        )

        AsyncImage(
            model = fullRequest,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (applyTransforms) {
                        translationX = zoomState.offsetX.value
                        translationY = zoomState.offsetY.value
                        scaleX = zoomState.scale.value
                        scaleY = zoomState.scale.value
                    }
                },
            onState = { state ->
                isHighResLoading = state is AsyncImagePainter.State.Loading
            }
        )

        if (isHighResLoading || isDownloading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                )
                Text(
                    text = if (isDownloading) "Скачивание оригинала..." else "Загрузка...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}