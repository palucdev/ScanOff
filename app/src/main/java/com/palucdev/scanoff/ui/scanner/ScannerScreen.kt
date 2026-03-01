package com.palucdev.scanoff.ui.scanner

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.palucdev.scanoff.R
import com.palucdev.scanoff.services.createPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val TAG = "ScanOff"
private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
private const val RATIO_4_3_VALUE = 4.0 / 3.0
private const val RATIO_16_9_VALUE = 16.0 / 9.0

/** Helper type alias used for analysis use case callbacks. */
private typealias LumaListener = (luma: Double) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    // ── Camera state ────────────────────────────────────────────────
    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var canSwitchCamera by remember { mutableStateOf(false) }

    // Use cases kept in remember so they survive recomposition
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var imageAnalyzer by remember { mutableStateOf<ImageAnalysis?>(null) }

    // ── Capture state ───────────────────────────────────────────────
    var savedUri by remember { mutableStateOf<Uri?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var showFlash by remember { mutableStateOf(false) }
    var isPdfLoading by remember { mutableStateOf(false) }

    // ── Background executor ─────────────────────────────────────────
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // ── Display rotation tracking ───────────────────────────────────
    val displayManager = remember {
        context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    DisposableEffect(displayManager) {
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit
            override fun onDisplayChanged(displayId: Int) {
                val rotation = view.display?.rotation ?: return
                imageCapture?.targetRotation = rotation
                imageAnalyzer?.targetRotation = rotation
            }
        }
        displayManager.registerDisplayListener(listener, null)
        onDispose {
            displayManager.unregisterDisplayListener(listener)
        }
    }

    // Shutdown executor when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // ── Aspect ratio helper ─────────────────────────────────────────
    fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            AspectRatio.RATIO_4_3
        } else {
            AspectRatio.RATIO_16_9
        }
    }

    // ── Bind camera use cases ───────────────────────────────────────
    fun bindCameraUseCases(provider: ProcessCameraProvider) {
        val metrics = context.resources.displayMetrics
        val screenAspectRatio = AspectRatio.RATIO_DEFAULT

        val rotation = view.display?.rotation ?: android.view.Surface.ROTATION_0

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val newPreview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        val newImageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        val newImageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    Log.d(TAG, "Average luminosity: $luma")
                })
            }

        provider.unbindAll()

        // Remove observers from previous camera instance
        camera?.cameraInfo?.cameraState?.removeObservers(lifecycleOwner)

        try {
            val newCamera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                newPreview,
                newImageCapture,
                newImageAnalyzer,
            )

            // Listen for SurfaceRequests from the Preview use case
            newPreview.setSurfaceProvider { request ->
                surfaceRequest = request
            }

            // Observe camera state for error toasts
            newCamera.cameraInfo.cameraState.observe(lifecycleOwner) { cameraState ->
                cameraState.error?.let { error ->
                    val msg = when (error.code) {
                        CameraState.ERROR_STREAM_CONFIG -> "Stream config error"
                        CameraState.ERROR_CAMERA_IN_USE -> "Camera in use"
                        CameraState.ERROR_MAX_CAMERAS_IN_USE -> "Max cameras in use"
                        CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> "Other recoverable error"
                        CameraState.ERROR_CAMERA_DISABLED -> "Camera disabled"
                        CameraState.ERROR_CAMERA_FATAL_ERROR -> "Fatal error"
                        CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> "Do not disturb mode enabled"
                        else -> return@let
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }

            camera = newCamera
            preview = newPreview
            imageCapture = newImageCapture
            imageAnalyzer = newImageAnalyzer

            // Update camera switch availability
            canSwitchCamera = try {
                provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) &&
                    provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            } catch (_: CameraInfoUnavailableException) {
                false
            }
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    // ── Initialize camera ───────────────────────────────────────────
    LaunchedEffect(lensFacing) {
        val provider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider = provider

        // Select available lens
        val actualFacing = when {
            provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) &&
                lensFacing == CameraSelector.LENS_FACING_BACK -> CameraSelector.LENS_FACING_BACK
            provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) &&
                lensFacing == CameraSelector.LENS_FACING_FRONT -> CameraSelector.LENS_FACING_FRONT
            provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.LENS_FACING_BACK
            provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.LENS_FACING_FRONT
            else -> {
                Toast.makeText(context, "No camera available", Toast.LENGTH_LONG).show()
                onNavigateBack()
                return@LaunchedEffect
            }
        }
        if (actualFacing != lensFacing) {
            lensFacing = actualFacing
            return@LaunchedEffect // will re-trigger with corrected facing
        }

        bindCameraUseCases(provider)
    }

    // ── Capture photo ───────────────────────────────────────────────
    fun capturePhoto() {
        val capture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME, Locale.getDefault())
            .format(System.currentTimeMillis())
        val scanDir = context.getExternalFilesDir("scans")
        scanDir?.mkdirs()
        val imageFile = File(scanDir, "scan_${name}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    (context as? android.app.Activity)?.runOnUiThread {
                        Toast.makeText(context, "Photo capture failed: ${exc.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    savedUri = Uri.fromFile(imageFile)
                    Log.d(TAG, "Photo capture succeeded: $savedUri")
                    (context as? android.app.Activity)?.runOnUiThread {
                        pageCount++
                        Toast.makeText(context, "Photo saved: ${imageFile.absolutePath}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )

        // Flash animation feedback
        showFlash = true
    }

    // ── Convert to PDF ──────────────────────────────────────────────
    fun convertToPdf() {
        val uri = savedUri ?: return
        isPdfLoading = true

        scope.launch {
            val result = withContext(Dispatchers.Default) {
                createPdf(uri, "test", context)
            }

            isPdfLoading = false

            result.onSuccess { pdfPath ->
                Toast.makeText(context, "PDF created: $pdfPath", Toast.LENGTH_LONG).show()
                openPdf(context, pdfPath)
            }

            result.onFailure { exception ->
                Toast.makeText(context, "PDF creation failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ── UI ───────────────────────────────────────────────────────────
    Box(modifier = modifier.fillMaxSize()) {

        // Camera viewfinder
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Flash overlay animation
        AnimatedVisibility(
            visible = showFlash,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
        }

        // Dismiss flash after brief delay
        LaunchedEffect(showFlash) {
            if (showFlash) {
                kotlinx.coroutines.delay(150L)
                showFlash = false
            }
        }

        // Top bar overlay
        TopAppBar(
            title = {
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(stringResource(R.string.page_counter_format, pageCount.coerceAtLeast(1)))
                    },
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.previous),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            ),
        )

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Align document within the frame",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Camera switch button
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                            CameraSelector.LENS_FACING_BACK
                        } else {
                            CameraSelector.LENS_FACING_FRONT
                        }
                    },
                    enabled = canSwitchCamera,
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch camera",
                        modifier = Modifier.size(32.dp),
                        tint = if (canSwitchCamera) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                    )
                }

                // Shutter button
                IconButton(
                    onClick = { capturePhoto() },
                    modifier = Modifier.size(72.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = stringResource(R.string.fab_label_scan),
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                // PDF convert button
                IconButton(
                    onClick = { convertToPdf() },
                    enabled = savedUri != null && !isPdfLoading,
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = stringResource(R.string.fab_create_pdf_desc),
                        modifier = Modifier.size(32.dp),
                        tint = if (savedUri != null && !isPdfLoading) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                    )
                }
            }
        }

        // PDF loading overlay
        if (isPdfLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

// ── Helper: open PDF with system viewer ─────────────────────────────
private fun openPdf(context: Context, filePath: String) {
    try {
        val pdfFile = File(filePath)
        val pdfUri = FileProvider.getUriForFile(
            context,
            "com.palucdev.scanoff.fileprovider",
            pdfFile,
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Log.d(TAG, "Opening PDF: $filePath")
        } else {
            Toast.makeText(context, "No PDF viewer app available to open the file", Toast.LENGTH_LONG).show()
            Log.w(TAG, "No PDF viewer app found to open: $filePath")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to open PDF: ${e.message}", e)
        Toast.makeText(context, "Failed to open PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// ── Luminosity analyzer (ported from ScannerFragment) ───────────────
private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
    private val frameRateWindow = 8
    private val frameTimestamps = ArrayDeque<Long>(5)
    private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
    private var lastAnalyzedTimestamp = 0L
    var framesPerSecond: Double = -1.0
        private set

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    override fun analyze(image: ImageProxy) {
        if (listeners.isEmpty()) {
            image.close()
            return
        }

        val currentTime = System.currentTimeMillis()
        frameTimestamps.addLast(currentTime)

        while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
        val timestampFirst = frameTimestamps.firstOrNull() ?: currentTime
        val timestampLast = frameTimestamps.lastOrNull() ?: currentTime
        framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
            frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

        lastAnalyzedTimestamp = frameTimestamps.first()

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

        listeners.forEach { it(luma) }
        image.close()
    }
}
