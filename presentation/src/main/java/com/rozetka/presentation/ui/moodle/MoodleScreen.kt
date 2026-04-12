package com.rozetka.presentation.ui.moodle

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.AssignmentLate
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes.Companion.Cookie9Sided
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.rozetka.model.MoodleCourse
import com.rozetka.presentation.R
import com.rozetka.presentation.ui.pay.LoadingState
import com.rozetka.presentation.util.generateColorFromHash
import com.rozetka.presentation.util.getNavigationBarHeightDp
import com.rozetka.presentation.util.getSubjectIcon
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodleScreen(
    onBackClick: () -> Unit,
    onCourseClick: (MoodleCourse) -> Unit,
    onDeadlinesClick: () -> Unit,
    viewModel: MoodleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moodle (LMS)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onDeadlinesClick) {
                        Icon(ImageVector.vectorResource(R.drawable.calendar_outline_24), contentDescription = "Дедлайны")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is MoodleUiState.Initial -> InitialState(onAuthClick = { viewModel.startAuth() })
                is MoodleUiState.Authenticating -> MoodleWebView(onSessionCaptured = { viewModel.onSessionCaptured(it) })
                is MoodleUiState.Loading -> LoadingState()
                is MoodleUiState.Success -> SuccessState(
                    courses = state.courses,
                    onCourseClick = onCourseClick,
                    onDeadlinesClick = onDeadlinesClick
                )
                is MoodleUiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.startAuth() })
                else -> {}
            }
        }
    }
}

@Composable
private fun InitialState(onAuthClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Требуется авторизация",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Для доступа к вашим курсам и оценкам войдите в систему Московского Политеха",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Button(onClick = onAuthClick, shape = MaterialTheme.shapes.medium) {
            Text("Войти через WebView")
        }
    }
}

@Composable
private fun SuccessState(
    courses: List<MoodleCourse>,
    onCourseClick: (MoodleCourse) -> Unit,
    onDeadlinesClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DeadlinesEntryCard(onClick = onDeadlinesClick)
        }
        items(courses, key = { it.id }) { course ->
            MoodleExpressiveCard(course, onCourseClick)
        }
        item { Spacer(Modifier.height(getNavigationBarHeightDp() + 80.dp)) }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeadlinesEntryCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(Cookie9Sided.toShape())
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.AssignmentLate,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Ближайшие дедлайны",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Посмотреть список заданий и тестов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MoodleExpressiveCard(course: MoodleCourse, onCourseClick: (MoodleCourse) -> Unit) {
    Card(
        onClick = { onCourseClick(course) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(Cookie9Sided.toShape())
                    .background(generateColorFromHash(course.title).copy(alpha = 0.15f)).align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getSubjectIcon(course.title),
                    contentDescription = null,
                    tint = generateColorFromHash(course.title)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (course.category.isNotEmpty()) {
                    Text(
                        text = course.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val grade = course.grade
                    if (grade != null && grade != "-") {
                        BadgeInfo(
                            icon = Icons.Rounded.Grade,
                            label = "$grade / ${course.gradeMax ?: "100"}",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }

                    val progress = course.progress
                    if (progress != null && progress.contains("%")) {
                        BadgeInfo(
                            label = "Прогресс: $progress",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeInfo(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String,
    containerColor: Color
) {
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Попробовать снова") }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MoodleWebView(onSessionCaptured: (String) -> Unit) {
    var isCaptured by remember { mutableStateOf(false) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"

                webViewClient = object : WebViewClient() {
                    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                        if (error?.url?.contains("mospolytech.ru") == true) handler?.proceed() else handler?.cancel()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        val currentUrl = url ?: ""
                        if (currentUrl.contains("lms.mospolytech.ru") && !currentUrl.contains("/login/")) {
                            evaluateJavascript(
                                "(function() { " +
                                "  return !!(document.querySelector('a[href*=\"logout.php\"]') || " +
                                "            document.querySelector('.usermenu') || " +
                                "            document.querySelector('.userbutton') || " +
                                "            document.querySelector('#user-menu-toggle-1')); " +
                                "})()"
                            ) { result ->
                                if (result == "true") {
                                    checkAndCaptureSession()
                                }
                            }
                        }
                    }

                    private fun checkAndCaptureSession() {
                        if (isCaptured) return
                        val cookies = CookieManager.getInstance().getCookie("https://lms.mospolytech.ru")
                        val session = cookies?.split(";")?.map { it.trim() }
                            ?.find { it.startsWith("MoodleSession") }?.substringAfter("=")

                        if (!session.isNullOrEmpty() && session != "deleted") {
                            isCaptured = true
                            onSessionCaptured(session)
                        }
                    }
                }
                CookieManager.getInstance().removeAllCookies(null)
                loadUrl("https://lms.mospolytech.ru/login/index.php")
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}