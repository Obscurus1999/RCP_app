package com.example.rcp

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import java.io.File

class PdfPageAdapter(
    private val file: File
) : RecyclerView.Adapter<PdfPageAdapter.PageViewHolder>() {

    private val fileDescriptor: ParcelFileDescriptor =
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

    private val renderer = PdfRenderer(fileDescriptor)

    inner class PageViewHolder(val photoView: PhotoView) :
        RecyclerView.ViewHolder(photoView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = PhotoView(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        view.maximumScale = 8f
        return PageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = renderer.openPage(position)

        // 🔥 Aumenta la resolución real del PDF
        val qualityFactor = 1.2f   // 1.0 = calidad real del PDF, 1.2 aumenta un poco

        val width = (page.width * qualityFactor).toInt()
        val height = (page.height * qualityFactor).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val rect = Rect(0, 0, width, height)

        page.render(bitmap, rect, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        holder.photoView.setImageBitmap(bitmap)
        page.close()
    }

    override fun getItemCount(): Int = renderer.pageCount

    fun close() {
        renderer.close()
        fileDescriptor.close()
    }
}
