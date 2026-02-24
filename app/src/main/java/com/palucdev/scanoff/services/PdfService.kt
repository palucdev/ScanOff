package com.palucdev.scanoff.services

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.IOException

fun createPdf(fileUri: Uri, filename: String, contentResolver: ContentResolver): PdfDocument {
    val inputStream = contentResolver.openInputStream(fileUri)
    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()

    // Read EXIF orientation
    val exifStream = contentResolver.openInputStream(fileUri)
    val exif = ExifInterface(exifStream!!)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    exifStream.close()

    // Rotate bitmap if needed
    val rotatedBitmap = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
        else -> bitmap
    }

    val document = PdfDocument()
    val pageInfo: PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(rotatedBitmap.width, rotatedBitmap.height, 1).create()
    val page: PdfDocument.Page = document.startPage(pageInfo)

    val canvas: Canvas = page.canvas
    canvas.drawBitmap(rotatedBitmap, 0f, 0f, null)
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

    // Clean up bitmaps
    if (rotatedBitmap != bitmap) {
        rotatedBitmap.recycle()
    }
    bitmap.recycle()

    return document
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply {
        postRotate(degrees)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}