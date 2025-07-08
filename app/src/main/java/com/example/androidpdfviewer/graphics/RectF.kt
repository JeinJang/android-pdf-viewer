package com.example.androidpdfviewer.graphics

import android.graphics.RectF

fun RectF.normalize(): RectF {
    return RectF(
        minOf(left, right),
        minOf(top, bottom),
        maxOf(left, right),
        maxOf(top, bottom)
    )
}