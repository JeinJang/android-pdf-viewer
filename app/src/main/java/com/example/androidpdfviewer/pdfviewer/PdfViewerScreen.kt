package com.example.androidpdfviewer.pdfviewer

import android.graphics.Bitmap
import android.graphics.RectF
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androidpdfviewer.graphics.normalize
import com.example.androidpdfviewer.text.TextWord
import com.example.androidpdfviewer.utils.extractTextWordsFromPage
import com.example.androidpdfviewer.utils.mapPdfRectToBitmapRect
import com.example.androidpdfviewer.utils.groupWordsByLine
import com.example.androidpdfviewer.utils.selectWordsInRect
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import java.io.File
import kotlin.math.abs
import kotlin.math.max

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
    var scale by remember { mutableStateOf(1.0f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var isTransforming by remember { mutableStateOf(false) }

    fun updateSelection(
        start: Offset?,
        end: Offset?,
        zoomOffsetX: Float,
        zoomOffsetY: Float,
        bitmapWidth: Int,
        bitmapHeight: Int
    ) {
        if (start != null && end != null) {
            var x0 = (dragStart!!.x - zoomOffsetX  - pan.x) / scale
            var y0 = (dragStart!!.y - zoomOffsetY  - pan.y) / scale
            var x1 = (dragEnd!!.x - zoomOffsetX  - pan.x) / scale
            var y1 = (dragEnd!!.y - zoomOffsetY  - pan.y) / scale

            x0 = x0.coerceIn(0f, bitmapWidth.toFloat())
            y0 = y0.coerceIn(0f, bitmapHeight.toFloat())
            x1 = x1.coerceIn(0f, bitmapWidth.toFloat())
            y1 = y1.coerceIn(0f, bitmapHeight.toFloat())

            if ((x0 != x1) && (y0 != y1)) {
                val selectionRect = RectF(x0, y0, x1, y1).normalize()
                selectedWords = selectWordsInRect(lines, selectionRect)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val boxWidthPx = with(density) { maxWidth.toPx().toInt() }
        val boxHeightPx = with(density) { maxHeight.toPx().toInt() }

        val pageSize = pdfController.getPdfSize(currentPage)
        val pdfWidth = pageSize.first
        val pdfHeight = pageSize.second

        val baseScale = minOf(boxWidthPx / pdfWidth, boxHeightPx / pdfHeight)
        val bitmapWidth = (pdfWidth * baseScale).toInt()
        val bitmapHeight = (pdfHeight * baseScale).toInt()

        val offsetX = (boxWidthPx - bitmapWidth) / 2f
        val offsetY = (boxHeightPx - bitmapHeight) / 2f

        val zoomOffsetX = (boxWidthPx - bitmapWidth * scale) / 2f
        val zoomOffsetY = (boxHeightPx - bitmapHeight * scale) / 2f

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
                ).normalize())
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
                        // 한 손가락 드래그(텍스트 선택)
                        .pointerInput(isTransforming, scale, pan) {
                            detectDragGestures(
                                onDragStart = {
                                    if (!isTransforming) dragStart = it
                                },
                                onDrag = { change, _ ->
                                    if (!isTransforming) {
                                        dragEnd = change.position
                                        updateSelection(
                                            dragStart,
                                            dragEnd,
                                            zoomOffsetX,
                                            zoomOffsetY,
                                            bitmapWidth,
                                            bitmapHeight
                                        )
                                    }
                                },
                                onDragEnd = {
                                    if (!isTransforming) {
                                        updateSelection(
                                            dragStart,
                                            dragEnd,
                                            zoomOffsetX,
                                            zoomOffsetY,
                                            bitmapWidth,
                                            bitmapHeight
                                        )
                                        dragStart = null
                                        dragEnd = null
                                    }
                                    isTransforming = false
                                }
                            )
                        }
                ) {

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            object : View(context) {
                                private val scaleGestureDetector = ScaleGestureDetector(context,
                                    object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                                        override fun onScale(detector: ScaleGestureDetector): Boolean {
                                            scale = max((scale * (detector.scaleFactor))
                                                .coerceIn(1f, 5f), 1f)
                                            if (scale == 1f) {
                                                pan = Offset(0f, 0f)
                                            }
                                            return true
                                        }
                                    }
                                )


                                override fun onTouchEvent(event: MotionEvent): Boolean {
                                    scaleGestureDetector.onTouchEvent(event)
                                    return true
                                }
                            }
                        }
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        withTransform({
                            translate(pan.x, pan.y)
                            scale(scale, scale)
                        }) {
                            drawImage(
                                bmp.asImageBitmap(),
                                topLeft = Offset(offsetX, offsetY)

                            )

                            if (dragStart != null && dragEnd != null && !isTransforming) {
                                val x0 = (dragStart!!.x - zoomOffsetX - pan.x) / scale
                                val y0 = (dragStart!!.y - zoomOffsetY - pan.y) / scale
                                val x1 = (dragEnd!!.x  - zoomOffsetX - pan.x) / scale
                                val y1 = (dragEnd!!.y - zoomOffsetY - pan.y) / scale
                                drawRect(
                                    color = Color(0xFF42A5F5).copy(alpha = 0.3f),
                                    topLeft = Offset(minOf(x0, x1) + offsetX, minOf(y0, y1) + offsetY),
                                    size = Size(abs(x1 - x0), abs(y1 - y0))
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
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
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
