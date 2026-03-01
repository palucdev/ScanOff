package com.palucdev.scanoff.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.File

private const val TAG = "PdfService"

/**
 * Creates a PDF from an image file with proper orientation handling.
 * 
 * @param fileUri URI of the image file
 * @param filename Display name (currently unused, uses timestamp)
 * @param context Android context for content resolver and file access
 * @return Result with PDF file path on success or error message on failure
 */
fun createPdf(fileUri: Uri, filename: String, context: Context): Result<String> {
    return try {
        Log.d(TAG, "Starting PDF creation from: $fileUri")

        // Read bitmap from file URI
        val inputStream = if (fileUri.scheme == "file") {
            val filePath =
                fileUri.path ?: throw IllegalArgumentException("Invalid file URI: path is null")
            File(filePath).inputStream()
        } else {
            context.contentResolver.openInputStream(fileUri)
                ?: throw IllegalArgumentException("Unable to open input stream for URI: $fileUri")
        }

        val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (bitmap == null) {
            throw IllegalArgumentException("Failed to decode image: invalid format or corrupted file")
        }

        Log.d(TAG, "Image decoded successfully: ${bitmap.width}x${bitmap.height}")

        // Read EXIF orientation
        val exif = if (fileUri.scheme == "file") {
            val filePath = fileUri.path
            if (filePath == null) {
                throw IllegalArgumentException("Invalid file URI: path is null")
            }
            ExifInterface(filePath)
        } else {
            val exifStream = context.contentResolver.openInputStream(fileUri)
                ?: throw IllegalArgumentException("Unable to open EXIF stream for URI: $fileUri")
            ExifInterface(exifStream)
        }

        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        Log.d(TAG, "Image orientation: $orientation")

        // Rotate bitmap if needed
        val rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }

        Log.d(TAG, "Creating PDF document with dimensions: ${rotatedBitmap.width}x${rotatedBitmap.height}")

        val document = PdfDocument()
        val pageInfo: PdfDocument.PageInfo =
            PdfDocument.PageInfo.Builder(rotatedBitmap.width, rotatedBitmap.height, 1).create()
        val page: PdfDocument.Page = document.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        canvas.drawBitmap(rotatedBitmap, 0f, 0f, null)
        document.finishPage(page)

        // Create PDFs directory in private app storage if it doesn't exist
        val pdfDir = context.getExternalFilesDir("pdfs")
            ?: throw IllegalStateException("Unable to access app external files directory")
        pdfDir.mkdirs()
        Log.d(TAG, "PDF directory ready: ${pdfDir.absolutePath}")

        // Create PDF file in private app directory
        val pdfFile = File(pdfDir, "scan_${System.currentTimeMillis()}.pdf")
        val outputStream = pdfFile.outputStream()
        document.writeTo(outputStream)
        outputStream.close()
        document.close()

        Log.d(TAG, "PDF created successfully: ${pdfFile.absolutePath}")

        // Clean up bitmaps
        if (rotatedBitmap != bitmap) {
            rotatedBitmap.recycle()
        }
        bitmap.recycle()

        Result.success(pdfFile.absolutePath)
    } catch (e: Exception) {
        Log.e(TAG, "PDF creation failed: ${e.message}", e)
        Result.failure(e)
    }
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply {
        postRotate(degrees)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}