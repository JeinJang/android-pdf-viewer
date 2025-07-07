package com.example.androidpdfviewer.pdfviewer

import android.graphics.RectF
import com.example.androidpdfviewer.text.TextWord

data class PdfSelection(
    val selectedWords: List<TextWord>,
    val selectedRects: List<RectF> = selectedWords.map { it.boundingBox },
    val selectedText: String = selectedWords.joinToString(" ") { it.text },
    val pageIndex: Int
)