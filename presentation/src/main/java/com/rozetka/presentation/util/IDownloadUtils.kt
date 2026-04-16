package com.rozetka.presentation.util

import android.graphics.Bitmap

interface IDownloadUtils {
    fun saveFileToDownloads(url: String)
    fun copyBitmapToClipboard(bitmap: Bitmap)
    fun saveBitmapToGallery(bitmap: Bitmap)
}