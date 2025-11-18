package com.example.rcp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.viewpager2.widget.ViewPager2
import java.io.File
import java.io.FileOutputStream

class PdfActivity : ComponentActivity() {

    private var adapter: PdfPageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)

        val viewPager = findViewById<ViewPager2>(R.id.pdfViewPager)

        // Copiar PDF desde raw
        val inputStream = resources.openRawResource(R.raw.manual_rcp)
        val file = File(filesDir, "manual_rcp.pdf")

        inputStream.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }

        adapter = PdfPageAdapter(file)
        viewPager.adapter = adapter
    }

    override fun onDestroy() {
        adapter?.close()
        super.onDestroy()
    }
}
