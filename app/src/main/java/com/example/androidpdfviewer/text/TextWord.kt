package com.example.androidpdfviewer.text

import android.graphics.RectF

data class TextWord(
    val text: String,
    val boundingBox: RectF
)