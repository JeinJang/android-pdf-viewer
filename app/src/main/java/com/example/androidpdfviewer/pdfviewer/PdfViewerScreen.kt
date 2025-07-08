package com.example.androidpdfviewer.pdfviewer

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.androidpdfviewer.graphics.normalize
import com.example.androidpdfviewer.text.TextWord
import com.example.androidpdfviewer.utils.extractTextWordsFromPage
import com.example.androidpdfviewer.utils.mapPdfRectToBitmapRect
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import java.io.File
import kotlin.math.abs

@Composable
fun PdfViewerScreen(pdfController: PdfRendererController, pdfFile: File) {
    val context = LocalContext.current
    var currentPage by remember { mutableIntStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var textWords by remember { mutableStateOf<List<TextWord>>(emptyList()) }
    var selectedWords by remember { mutableStateOf<List<TextWord>>(emptyList()) }
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragEnd by remember { mutableStateOf<Offset?>(null) }
    var lines by remember { mutableStateOf<List<List<TextWord>>>(emptyList()) }

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
        val selected = mutableListOf<TextWord>()
        for (line in lines) {
            val lineRect = line.fold(RectF(line[0].boundingBox)) { acc, w -> acc.apply { union(w.boundingBox) } }
            if (RectF.intersects(lineRect, selectionRect)) {
                val lineSelected = line.filter { RectF.intersects(it.boundingBox, selectionRect) }
                selected.addAll(lineSelected)
            }
        }
        return selected
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val boxWidthPx = with(density) { maxWidth.toPx().toInt() }
        val boxHeightPx = with(density) { maxHeight.toPx().toInt() }

        val pageSize = pdfController.getPdfSize(currentPage)
        val pdfWidth = pageSize.first
        val pdfHeight = pageSize.second

        val scale = minOf(boxWidthPx / pdfWidth, boxHeightPx / pdfHeight)
        val bitmapWidth = (pdfWidth * scale).toInt()
        val bitmapHeight = (pdfHeight * scale).toInt()

        val offsetX = (boxWidthPx - bitmapWidth) / 2f
        val offsetY = (boxHeightPx - bitmapHeight) / 2f

        LaunchedEffect(currentPage, boxWidthPx, boxHeightPx) {
            PDFBoxResourceLoader.init(context)
            bitmap = pdfController.renderPage(currentPage, bitmapWidth, bitmapHeight)

            val pageSize = pdfController.getPdfSize(currentPage)
            val rawWords = extractTextWordsFromPage(pdfFile, currentPage)
            textWords = rawWords.map {
                it.copy(boundingBox = mapPdfRectToBitmapRect(
                    it.boundingBox,
                    pageSize.first,
                    pageSize.second,
                    bitmap!!.width,
                    bitmap!!.height
                ))
            }
            lines = groupWordsByLine(textWords)

            selectedWords = emptyList()
            dragStart = null
            dragEnd = null
        }

        Column(modifier = Modifier.fillMaxSize()) {
            bitmap?.let { bmp ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { dragStart = it },
                                onDrag = { change, _ ->
                                    dragEnd = change.position
                                    // 실시간 하이라이트
                                    val start = dragStart
                                    val end = dragEnd
                                    if (start != null && end != null) {
                                        var x0 = start.x - offsetX
                                        var y0 = start.y - offsetY
                                        var x1 = end.x - offsetX
                                        var y1 = end.y - offsetY

                                        x0 = x0.coerceIn(0f, bitmapWidth.toFloat())
                                        y0 = y0.coerceIn(0f, bitmapHeight.toFloat())
                                        x1 = x1.coerceIn(0f, bitmapWidth.toFloat())
                                        y1 = y1.coerceIn(0f, bitmapHeight.toFloat())

                                        if ((x0 != x1) && (y0 != y1)) {
                                            val selectionRect = RectF(x0, y0, x1, y1).normalize()
                                            selectedWords = selectWordsInRect(lines, selectionRect)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val start = dragStart
                                    val end = dragEnd
                                    if (start != null && end != null) {
                                        var x0 = start.x - offsetX
                                        var y0 = start.y - offsetY
                                        var x1 = end.x - offsetX
                                        var y1 = end.y - offsetY

                                        x0 = x0.coerceIn(0f, bitmapWidth.toFloat())
                                        y0 = y0.coerceIn(0f, bitmapHeight.toFloat())
                                        x1 = x1.coerceIn(0f, bitmapWidth.toFloat())
                                        y1 = y1.coerceIn(0f, bitmapHeight.toFloat())

                                        if ((x0 == x1) || (y0 == y1)) {
                                            selectedWords = emptyList()
                                            return@detectDragGestures
                                        }

                                        val selectionRect = RectF(x0, y0, x1, y1).normalize()
                                        selectedWords = selectWordsInRect(lines, selectionRect)

                                        val selectedText = selectedWords.joinToString(" ") { it.text }
                                        Log.d("PdfViewer", "Selected text: $selectedText")
                                    }
                                    dragStart = null
                                    dragEnd = null
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawImage(
                            bmp.asImageBitmap(),
                            topLeft = Offset(offsetX, offsetY)
                        )

                        if (dragStart != null && dragEnd != null) {
                            drawRect(
                                color = Color(0xFF42A5F5).copy(alpha = 0.3f),
                                topLeft = Offset(minOf(dragStart!!.x, dragEnd!!.x), minOf(dragStart!!.y, dragEnd!!.y)),
                                size = Size(abs(dragEnd!!.x - dragStart!!.x), abs(dragEnd!!.y - dragStart!!.y))
                            )
                        }

                        selectedWords.forEach {
                            drawRect(
                                color = Color.Yellow.copy(alpha = 0.4f),
                                topLeft = Offset(
                                    it.boundingBox.left + offsetX,
                                    it.boundingBox.top + offsetY
                                ),
                                size = Size(
                                    it.boundingBox.width(),
                                    it.boundingBox.height()
                                )
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (currentPage > 0) {
                            currentPage--
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text("PREV")
                }
                Button(
                    onClick = {
                        if (currentPage < pdfController.getPageCount() - 1) {
                            currentPage++
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text("NEXT")
                }
            }
        }
    }
}
