package com.example.android_pdf_viewer.utils

import android.content.Context
import java.io.File

fun copyAssetToInternalStorage(context: Context, fileName: String): File {
    val file = File(context.filesDir, fileName)
    if (!file.exists()) {
        context.assets.open(fileName).use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
    return file
}