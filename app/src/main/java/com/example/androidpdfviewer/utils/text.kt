package com.example.androidpdfviewer.utils

import com.example.androidpdfviewer.text.PositionAwareStripper
import com.example.androidpdfviewer.text.TextWord
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File


fun extractTextWordsFromPage(pdfFile: File, pageIndex: Int): List<TextWord> {
    val document = PDDocument.load(pdfFile)
    val stripper = PositionAwareStripper().apply {
        startPage = pageIndex + 1
        endPage = pageIndex + 1
    }
    stripper.getText(document) // 내부적으로 writeString 호출됨
    document.close()
    return stripper.collectedWords
}