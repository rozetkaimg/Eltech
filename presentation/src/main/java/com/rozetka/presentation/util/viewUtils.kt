package com.rozetka.presentation.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.Window
import android.widget.Toast
import androidx.appcompat.R
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes.Companion.Arch
import androidx.compose.material3.MaterialShapes.Companion.Arrow
import androidx.compose.material3.MaterialShapes.Companion.Cookie4Sided
import androidx.compose.material3.MaterialShapes.Companion.Gem
import androidx.compose.material3.MaterialShapes.Companion.Oval
import androidx.compose.material3.MaterialShapes.Companion.Square
import androidx.compose.material3.MaterialShapes.Companion.Sunny
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.rozetka.presentation.util.ThemeObject.DarkThemeState

import androidx.compose.material.icons.filled.*

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

fun openUrlInBrowser(context: Context, url: String) {
    val completeUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
        "https://$url"
    } else {
        url
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, completeUrl.toUri())
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Не найдено приложение для открытия ссылки", Toast.LENGTH_SHORT)
            .show()
    } catch (e: Exception) {
        Toast.makeText(context, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
    }
}

fun formatSimpleDateTime(context: Context, isoDateTimeString: String): String {
    val systemLocale =
        context.resources.configuration.locales[0]

    try {
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormatter.parse(isoDateTimeString) ?: return isoDateTimeString
        val outputFormatter = SimpleDateFormat("d MMMM yyyy, HH:mm", systemLocale)
        return outputFormatter.format(date)

    } catch (e: Exception) {
        e.printStackTrace()
        return isoDateTimeString
    }
}


fun AppVersionText(context: Context): String {

    val appVersion = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {

    }
    return appVersion.toString()
}

@Composable
fun getStatusBarHeightDp(): Dp {
    val navigationBarInset = WindowInsets.navigationBars
    val navigationBarHeightPx = navigationBarInset.getTop(LocalDensity.current)
    return with(LocalDensity.current) {
        navigationBarHeightPx.toDp()
    }
}

@Composable
fun ChangeStatusBarIconsColor(darkIcons: Boolean) {
    val view = LocalView.current

    val window = view.context.findWindow()

    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkIcons

}

fun Context.findWindow(): Window {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context.window
        }
        context = context.baseContext
    }
    throw IllegalStateException("no window found for this Context")
}

@Composable
fun getStatusBarIconState(): Boolean {
    return when (DarkThemeState.value) {
        0 ->
            isSystemInDarkTheme()

        1 -> true
        else -> false
    }

}
 fun shareImage(context: Context, photoUri: String) {
    val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, photoUri)
        type = "text/plain"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Поделиться изображением через..."))
}

private val Context.systemLocale: Locale
    get() = this.resources.configuration.locales[0]

fun formatLocalizedDate(context: Context, dateString: String): String {
    val systemLocale = context.systemLocale

    val inputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

    try {
        val localDate = LocalDate.parse(dateString, inputFormatter)

        val outputFormatter = DateTimeFormatter
            .ofLocalizedDate(FormatStyle.LONG)
            .withLocale(systemLocale)

        return localDate.format(outputFormatter)

    } catch (e: DateTimeParseException) {
        return dateString
    } catch (e: Exception) {
        return dateString
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun getRandomRoundedCornerShape(): Shape {

    return when ((0..5).random()) {
        0 -> Sunny.toShape()
        1 -> Cookie4Sided.toShape()
        2 -> RoundedCornerShape(100)
        3 -> Arch.toShape()
        4 -> Oval.toShape()
        else -> Square.toShape()
    }
}
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AutoSizingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 10.sp
) {
    BoxWithConstraints(modifier = modifier) {
        var textStyle by remember { mutableStateOf(style) }
        val density = LocalDensity.current

        var currentFontSizePx by remember {
            mutableFloatStateOf(with(density) { style.fontSize.toPx() })
        }
        Text(
            text = text,
            style = textStyle,
            softWrap = false,
            maxLines = 1,
            color = style.color,
            fontWeight = style.fontWeight,
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    val newFontSizePx = currentFontSizePx * 0.95f
                    val minFontSizePx = with(density) { minFontSize.toPx() }

                    if (newFontSizePx >= minFontSizePx) {
                        currentFontSizePx = newFontSizePx
                        textStyle = textStyle.copy(fontSize = with(density) { newFontSizePx.toSp() })
                    }
                }
            }
        )
    }
}
@Composable
fun getSubjectIcon(subjectName: String): ImageVector {
    val normalized = subjectName.lowercase()
        .replace("\\s".toRegex(), "")
        .replace("$", "s")
        .replace("-", "")

    return when {
        normalized.containsAny("безопасн", "защит") -> Icons.Default.Security
        normalized.containsAny("основыинформацион", "программир", "сайт") ->  ImageVector.vectorResource(com.rozetka.presentation.R.drawable.code_24)
        normalized.containsAny(  "коммуникац", "коммуникации") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.chats_outline_28)
        normalized.containsAny("информацион", "программир", "сайт") ->  ImageVector.vectorResource(com.rozetka.presentation.R.drawable.code_24)
        normalized.containsAny("web", "веб") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.computer_outline_24)
        normalized.containsAny("компьют", "алгоритм", "электрон", "вычислит") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.computer_outline_24)
        normalized.contains("данны") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.outline_database_24)
        normalized.containsAny("сети", "сетей") -> Icons.Default.Hub
        normalized.contains("мобильн") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.smartphone_outline_28)
        normalized.contains("сервер") -> Icons.Default.Dns
        normalized.contains("интеллект") -> Icons.Default.Psychology
        normalized.containsAny("матем", "линейн", "вероятн") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.function_24dp_e3e3e3_fill0_wght400_grad0_opsz24)
        normalized.contains("физик") -> Icons.Default.Science
        normalized.contains("химич") -> Icons.Default.Biotech
        normalized.contains("анализ") -> Icons.Default.Analytics
        normalized.contains("эколог") -> Icons.Default.Eco
        normalized.contains("научн") -> Icons.Default.HistoryEdu
        normalized.contains("язык") -> Icons.Default.Translate
        normalized.contains("философ") -> Icons.Default.AutoStories
        normalized.contains("истор") -> Icons.Default.History
        normalized.contains("эконом") -> Icons.Default.MonetizationOn
        normalized.contains("управлени") -> Icons.Default.AccountTree
        normalized.contains("российской") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.flag_outline_28)
        normalized.containsAny("спорт", "физичес") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.physical)
        normalized.containsAny("вкр", "итоговая", "аттестация") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.school_outline_28)
        normalized.contains("практика") -> Icons.Default.Work
        normalized.contains("проектн") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.lightbulb_star_outline)
        normalized.contains("тайм") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.calendar_outline)
        normalized.contains("презентац") -> Icons.Default.CoPresent
        normalized.containsAny("машин", "инжен") -> Icons.Default.Engineering
        normalized.containsAny("проектир", "расчет") -> Icons.Default.Architecture
        normalized.containsAny("обработ", "переработ") -> Icons.Default.SettingsInputComponent
        normalized.contains("автоматиз") -> Icons.Default.PrecisionManufacturing
        normalized.contains("производ") -> Icons.Default.Factory
        normalized.contains("материал") -> Icons.Default.Layers
        normalized.contains("патент") -> Icons.Default.Verified
        normalized.contains("распознаван") -> Icons.Default.Face
        normalized.contains("принт") -> Icons.Default.Print
        normalized.contains("игр") -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.game_outline_28)
        normalized.contains("жизнед") -> Icons.Default.HealthAndSafety
        normalized.contains("операционн") -> Icons.Default.Terminal
        else -> ImageVector.vectorResource(com.rozetka.presentation.R.drawable.book_spread_outline_24)
    }
}

private fun String.containsAny(vararg keywords: String): Boolean {
    return keywords.any { this.contains(it) }
}
fun generateColorFromHash(str: String): Color {
    val hash = str.hashCode()
    val r = (hash shr 16 and 0xFF)
    val g = (hash shr 8 and 0xFF)
    val b = (hash and 0xFF)
    return Color(r, g, b)
}
fun getAcademicEventColor(eventType: String): Color {
    return when (eventType.lowercase().trim()) {
        "консультация" -> Color(0xFF2196F3)
        "зачет"        -> Color(0xFF4CAF50)
        "диф. зачет" -> Color(0xFFFF9800)
        "экзамен"      -> Color(0xFFF44336)
        else           -> Color(0xFF9E9E9E)
    }
}