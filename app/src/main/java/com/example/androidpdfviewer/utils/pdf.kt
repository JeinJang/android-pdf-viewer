package com.example.androidpdfviewer.utils

import android.graphics.RectF

// android 좌표계와 PDF 좌표계 차이 보정
fun mapPdfRectToBitmapRect(
    pdfRect: RectF,
    pdfPageWidth: Float,
    pdfPageHeight: Float,
    bitmapWidth: Int,
    bitmapHeight: Int
): RectF {
    val scaleX = bitmapWidth / pdfPageWidth
    val scaleY = bitmapHeight / pdfPageHeight

    // Y 축 뒤집기 + 스케일 보정
    return RectF(
        pdfRect.left * scaleX,
        (pdfPageHeight - pdfRect.top) * scaleY,
        pdfRect.right * scaleX,
        (pdfPageHeight - pdfRect.bottom) * scaleY
    )
}