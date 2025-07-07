package com.example.androidpdfviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidpdfviewer.pdfviewer.PdfRendererController
import com.example.androidpdfviewer.pdfviewer.PdfViewerScreen
import com.example.androidpdfviewer.ui.theme.AndroidpdfviewerTheme
import com.example.androidpdfviewer.utils.copyAssetToInternalStorage

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