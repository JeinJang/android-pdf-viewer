package com.example.androidpdfviewer.text

import android.graphics.RectF
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition

class PositionAwareStripper : PDFTextStripper() {
    val collectedWords = mutableListOf<TextWord>()

    private var currentLine = mutableListOf<TextPosition>()

    override fun processTextPosition(text: TextPosition) {
        val unicode = text.unicode.trim()
        if (unicode.isNotEmpty()) {
            currentLine.add(text)
        }
    }

    override fun writeString(string: String?, textPositions: MutableList<TextPosition>?) {
        textPositions ?: return
        val word = buildString { textPositions.forEach { append(it.unicode) } }.trim()
        if (word.isNotEmpty()) {
            val first = textPositions.first()
            val last = textPositions.last()

            val left = first.xDirAdj
            val top = first.yDirAdj - first.heightDir
            val right = last.xDirAdj + last.widthDirAdj
            val bottom = first.yDirAdj

            val rect = RectF(left, top, right, bottom)
            collectedWords.add(TextWord(word, rect))
        }
    }
}