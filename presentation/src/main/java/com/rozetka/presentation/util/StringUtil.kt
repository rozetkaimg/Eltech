package com.rozetka.presentation.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun removeEmojis(text: String): String {

    val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+|[\\u2600-\\u27BF]+|[\\uD83E\uDD00-\\uD83E\uDFFF]+")

    return emojiRegex.replace(text, "")
}
@Composable
fun getNavigationBarHeightDp(): Dp {
    val navigationBarInset = WindowInsets.navigationBars
    val navigationBarHeightPx = navigationBarInset.getBottom(LocalDensity.current)
    return with(LocalDensity.current) {
        navigationBarHeightPx.toDp()
    }
}
fun formatLessonDates(dateRange: String): String {
    return try {
        val parts = dateRange.split(" - ")
        if (parts.size != 2) return dateRange

        val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatterOutput = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))

        val dateStart = LocalDate.parse(parts[0].trim(), formatterInput)
        val dateEnd = LocalDate.parse(parts[1].trim(), formatterInput)

        "${dateStart.format(formatterOutput)} - ${dateEnd.format(formatterOutput)}"
    } catch (e: Exception) {
        dateRange
    }
}
fun formatToRussianDate(dateString: String): String {
    return try {

        val inputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)
        val date = LocalDate.parse(dateString, inputFormatter)

        val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateString
    }
}