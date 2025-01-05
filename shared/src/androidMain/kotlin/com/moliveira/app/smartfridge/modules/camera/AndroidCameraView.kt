package com.moliveira.app.smartfridge.modules.camera

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.aakira.napier.Napier
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun AndroidCameraView(
    modifier: Modifier,
    onBarcodeScanned: (Barcode?) -> Unit,
    onTextRecognized: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val imageCapture = remember {
        ImageCapture
            .Builder()
            .build()
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PreviewView(context).apply {

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    // Will be implemented
                    startCamera(
                        context = context,
                        previewView = this,
                        imageCapture = imageCapture,
                        lifecycleOwner = lifecycleOwner,
                        onBarcodeScanned = onBarcodeScanned,
                        cameraProvider = cameraProviderFuture.get(),
                        onTextRecognized = onTextRecognized,
                    )
                }, ContextCompat.getMainExecutor(context))
            }
        }
    )
}

@ExperimentalGetImage
private fun startCamera(
    context: Context,
    previewView: PreviewView,
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    imageCapture: ImageCapture,
    onBarcodeScanned: (Barcode) -> Unit,
    onTextRecognized: (String) -> Unit,
) {
    // Preview
    val preview = Preview.Builder()
        .build()
        .also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

    val executor = Executors.newSingleThreadExecutor()

    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    // Types of barcodes
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_QR_CODE,
        )
        .build()

    val scanner = BarcodeScanning.getClient(options)

    // Analyze the image
    imageAnalysis.setAnalyzer(executor) { imageProxy ->
        // Will be implemented
        processImageProxy(
            barcodeScanner = scanner,
            imageProxy = imageProxy,
            onSuccess = onBarcodeScanned,
            onTextRecognized = onTextRecognized,
        )
    }

    // Select back camera as a default
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        // Unbind use cases before rebinding
        cameraProvider.unbindAll()

        // Bind use cases to camera
        cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalysis
        )

    } catch (exc: Exception) {
        Napier.e("Use case binding failed", exc)
    }
}

@ExperimentalGetImage
private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onSuccess: (Barcode) -> Unit,
    onTextRecognized: (String) -> Unit,
) {
    val textScanner = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    imageProxy.image?.let { image ->
        val inputImage =
            InputImage.fromMediaImage(
                image,
                imageProxy.imageInfo.rotationDegrees
            )

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodeList ->
                barcodeList.forEach(onSuccess)
            }
            .addOnFailureListener {
                // This failure will happen if the barcode scanning model
                // fails to download from Google Play Services
                Napier.e(it.message.orEmpty())
            }
            .continueWithTask {
                textScanner.process(inputImage)
            }
            .addOnSuccessListener { texts ->
                for (block in texts.textBlocks) {
                    for (line in block.lines) {
                        for (element in line.elements) {
                            onTextRecognized(element.text)
                        }
                    }
                }
            }.addOnCompleteListener {
                // When the image is from CameraX analysis use case, must
                // call image.close() on received images when finished
                // using them. Otherwise, new images may not be received
                // or the camera may stall.
                imageProxy.image?.close()
                imageProxy.close()
            }
    }
}