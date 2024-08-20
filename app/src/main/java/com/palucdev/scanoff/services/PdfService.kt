package com.palucdev.scanoff.services

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

fun createPdf(fileUri: Uri, filename: String, contentResolver: ContentResolver): PdfDocument {
    val bitmap: Bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(fileUri))

    val document = PdfDocument()
    val pageInfo: PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
    val page: PdfDocument.Page = document.startPage(pageInfo)

    val canvas: Canvas = page.canvas
    canvas.drawBitmap(bitmap, 0f, 0f, null)
    document.finishPage(page)

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents")
        }
    }


    val documentUri: Uri =
        contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            ?: throw Exception("Can't create document Uri for PDF file")

    document.writeTo(contentResolver.openOutputStream(documentUri))
    document.close()

    return document
}