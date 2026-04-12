package com.rozetka.presentation.ui.home

import android.net.Uri
import java.util.regex.Pattern

fun extractImageUrl(htmlContent: String): String {
    val pattern = Pattern.compile("src=\"([^\"]+)\"")
    val matcher = pattern.matcher(htmlContent)
    return if (matcher.find()) {
        val src = matcher.group(1) ?: ""
        if (src.startsWith("http")) src else "https://e.mospolytech.ru/old/$src"
    } else ""
}
