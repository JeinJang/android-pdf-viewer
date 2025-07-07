package com.example.androidpdfviewer.pdfviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import androidx.core.graphics.createBitmap

class PdfRendererController(private val context: Context) {
    private lateinit var fileDescriptor: ParcelFileDescriptor
    private lateinit var pdfRenderer: PdfRenderer
    private var currentPage: PdfRenderer.Page? = null

    fun openPdf(file: File) {
        fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(fileDescriptor)
    }

    fun getPageCount(): Int = pdfRenderer.pageCount

    fun renderPage(index: Int): Bitmap {
        currentPage?.close()
        currentPage = pdfRenderer.openPage(index)

        val bitmap = createBitmap(currentPage!!.width, currentPage!!.height)
        currentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    fun close() {
        currentPage?.close()
        pdfRenderer.close()
        fileDescriptor.close()
    }
}