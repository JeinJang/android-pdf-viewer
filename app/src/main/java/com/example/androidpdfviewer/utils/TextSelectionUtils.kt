package com.example.androidpdfviewer.utils

import android.graphics.RectF
import com.example.androidpdfviewer.text.TextWord
import kotlin.math.abs

fun groupWordsByLine(words: List<TextWord>, lineThreshold: Float = 10f): List<List<TextWord>> {
    val sorted = words.sortedBy { it.boundingBox.top }
    val lines = mutableListOf<MutableList<TextWord>>()
    for (word in sorted) {
        val y = word.boundingBox.top
        val lastLine = lines.lastOrNull()
        if (lastLine == null || abs(lastLine.last().boundingBox.top - y) > lineThreshold) {
            lines.add(mutableListOf(word))
        } else {
            lastLine.add(word)
        }
    }
    return lines.map { it.sortedBy { w -> w.boundingBox.left } }
}

fun selectWordsInRect(
    lines: List<List<TextWord>>,
    selectionRect: RectF
): List<TextWord> {
    var startLineIndex: Int? = null
    var startWordIndex: Int? = null
    var endLineIndex: Int? = null
    var endWordIndex: Int? = null

    lines.forEachIndexed { lineIdx, line ->
        line.forEachIndexed { wordIdx, word ->
            if (RectF.intersects(word.boundingBox, selectionRect)) {
                if (startLineIndex == null) {
                    startLineIndex = lineIdx
                    startWordIndex = wordIdx
                }
                endLineIndex = lineIdx
                endWordIndex = wordIdx
            }

        }
    }

    if (startLineIndex == null || endLineIndex == null) return emptyList()

    val result = mutableListOf<TextWord>()

    for (i in startLineIndex!!..endLineIndex!!) {
        val line = lines[i]
        val from = when (i) {
            startLineIndex -> startWordIndex!!
            else -> 0
        }
        val to = when (i) {
            endLineIndex -> endWordIndex!!
            else -> line.lastIndex
        }
        if (from <= to) {
            result.addAll(line.subList(from, to + 1))
        }
    }

    return result
}
