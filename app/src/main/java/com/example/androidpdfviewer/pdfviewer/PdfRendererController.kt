package com.example.androidpdfviewer.pdfviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import androidx.core.graphics.createBitmap
import com.tom_roush.pdfbox.pdmodel.PDDocument

class PdfRendererController(private val context: Context) {
    private lateinit var fileDescriptor: ParcelFileDescriptor
    private lateinit var pdfRenderer: PdfRenderer
    private var currentPage: PdfRenderer.Page? = null
    private lateinit var pdfDocument: PDDocument


    fun openPdf(file: File) {
        fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)
        pdfDocument = PDDocument.load(file)
    }

    fun getPageCount(): Int = pdfRenderer.pageCount

    fun renderPage(index: Int, width: Int, height: Int): Bitmap {
        currentPage?.close()
        currentPage = pdfRenderer.openPage(index)
    
        val bitmap = createBitmap(width, height)
        currentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    fun getPdfSize(pageIndex: Int): Pair<Float, Float> {
        val page = pdfDocument.getPage(pageIndex)
        val mediaBox = page.mediaBox
        return Pair(mediaBox.width, mediaBox.height)
    }

    fun close() {
        currentPage?.close()
        pdfRenderer.close()
        fileDescriptor.close()
    }
}