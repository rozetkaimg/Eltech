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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
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

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale


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
        2 -> Gem.toShape()
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