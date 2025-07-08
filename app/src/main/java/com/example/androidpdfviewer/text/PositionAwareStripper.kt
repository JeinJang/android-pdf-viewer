package com.example.androidpdfviewer.text

import android.graphics.RectF
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition

class PositionAwareStripper : PDFTextStripper() {
    val collectedWords = mutableListOf<TextWord>()

    private var currentLine = mutableListOf<TextPosition>()

    @Override
    override fun processTextPosition(text: TextPosition) {
        val unicode = text.unicode.trim()
        if (unicode.isNotEmpty()) {
            currentLine.add(text)
        }
        super.processTextPosition(text)
    }

    @Override
    override fun writeString(string: String?, textPositions: MutableList<TextPosition>?) {
        if (string == null || textPositions == null) return
        if (textPositions.isEmpty()) return

        val word = buildString { textPositions.forEach { append(it.unicode) } }.trim()
        if (word.isNotEmpty()) {
            val first = textPositions.first()
            val last = textPositions.last()

            val pageHeight = currentPage?.mediaBox?.height ?: 0f

            val left = first.xDirAdj
            val top = pageHeight - first.yDirAdj
            val right = last.xDirAdj + last.widthDirAdj
            val bottom = pageHeight - (first.yDirAdj - first.heightDir)

            val rect = RectF(left, top, right, bottom)
            collectedWords.add(TextWord(word, rect))
        }
        super.writeString(string, textPositions)
    }
}