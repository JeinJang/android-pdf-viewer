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
        if (string == null || textPositions == null || textPositions.isEmpty()) return

        val pageHeight = currentPage?.mediaBox?.height ?: 0f

        var currentWord = StringBuilder()
        var wordStart: TextPosition? = null

        for ((i, pos) in textPositions.withIndex()) {
            val char = pos.unicode
            if (char.isNotBlank()) {
                if (currentWord.isEmpty()) {
                    wordStart = pos
                }
                currentWord.append(char)
            }

            val isLastChar = i == textPositions.size - 1
            val nextChar = if (!isLastChar) textPositions[i + 1].unicode else " "

            if (
                char.all { it.isWhitespace() } ||
                nextChar.all { it.isWhitespace() } ||
                isLastChar
            ) {
                if (currentWord.isNotEmpty() && wordStart != null) {
                    val wordEnd = pos
                    val left = wordStart.xDirAdj
                    val top = pageHeight - wordStart.yDirAdj
                    val right = wordEnd.xDirAdj + wordEnd.widthDirAdj
                    val bottom = pageHeight - (wordStart.yDirAdj - wordStart.heightDir)
                    val rect = RectF(left, top, right, bottom)
                    collectedWords.add(TextWord(currentWord.toString(), rect))
                    currentWord = StringBuilder()
                    wordStart = null
                }
            }
        }

        super.writeString(string, textPositions)
    }
}