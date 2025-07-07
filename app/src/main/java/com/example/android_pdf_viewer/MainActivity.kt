package com.example.android_pdf_viewer

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.android_pdf_viewer.pdfviewer.PdfRendererController
import com.example.android_pdf_viewer.pdfviewer.PdfPageView
import com.example.android_pdf_viewer.pdfviewer.PdfViewerScreen
import com.example.android_pdf_viewer.ui.theme.AndroidpdfviewerTheme
import com.example.android_pdf_viewer.utils.copyAssetToInternalStorage
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = PdfRendererController(this)
        val file = copyAssetToInternalStorage(this, "bitcoin.pdf")
        controller.openPdf(file)

        setContent {
            MaterialTheme {
                PdfViewerScreen(pdfController = controller)
            }
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidpdfviewerTheme {
        Greeting("Android")
    }
}