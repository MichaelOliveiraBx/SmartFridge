package com.moliveira.app.smartfridge.modules.camera

import android.app.Activity
import android.content.Context.CAMERA_SERVICE
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.SparseIntArray
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.moliveira.app.smartfridge.LocalActivity
import io.github.aakira.napier.Napier
import java.util.concurrent.Executors

val ORIENTATIONS = SparseIntArray()

@OptIn(ExperimentalGetImage::class)
@Composable
actual fun CameraViewPlatform(
    modifier: Modifier,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
) {
    AndroidCameraView(
        modifier = modifier,
        onBarcodeScanned = {
            Napier.d("HHHH - onBarcodeRecognized:${it?.rawValue}")
            it?.rawValue?.let { onBarcodeRecognized(it) }
        },
        onTextRecognized = {
            Napier.d("HHHH - onTextRecognized:$it")
            onTextRecognized(it)
        }
    )

//    CameraPreviewPlatform(
//        modifier = modifier,
//        onTextRecognized = onTextRecognized,
//        onBarcodeRecognized = onBarcodeRecognized,
//    )
}

@ExperimentalGetImage
@Composable
fun CameraPreviewPlatform(
    modifier: Modifier,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current

//    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
//    val previewView = remember { PreviewView(context) }
//
//    AndroidView(
//        factory = { previewView },
//        modifier = modifier,
//        update = {
//            val cameraProvider = cameraProviderFuture.get()
//            val preview = Preview.Builder().build()
//            preview.setSurfaceProvider(previewView.surfaceProvider)
//            val cameraSelector =
//                CameraSelector.Builder()
//                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//                    .build()
//
//            cameraProvider.bindToLifecycle(lifeCycleOwner, cameraSelector, preview)
//        }
//    )

    val previewView = remember { PreviewView(context) }
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalActivity.current

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        // Configure le flux de la caméra
        val preview = Preview.Builder()
            .build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analyzer ->
                analyzer.setAnalyzer(Executors.newSingleThreadExecutor(), { imageProxy ->
//                    coroutineScope.launch {
                    processImageProxy(
                        imageProxy,
                        activity = activity,
                        onBarcodeRecognized,
                        onTextRecognized
                    )
//                    }
                })
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifeCycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@ExperimentalGetImage
fun processImageProxy(
    imageProxy: ImageProxy,
    activity: Activity,
    onBarcodeDetected: (String) -> Unit,
    onTextRecognized: (String) -> Unit,
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
//                    Barcode.FORMAT_EAN_8,
//                    Barcode.FORMAT_EAN_13,
//                    Barcode.FORMAT_QR_CODE,
//                    Barcode.FORMAT_PDF417,
                    Barcode.FORMAT_ALL_FORMATS,
                )
                .enableAllPotentialBarcodes()
                .build()
        )
//        val textScanner = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
//            textScanner.process(inputImage).addOnSuccessListener { texts ->
//                Napier.d("HHHH - texts:${texts.text}")
//                for (block in texts.textBlocks) {
//                    for (line in block.lines) {
//                        for (element in line.elements) {
//                            onTextRecognized(element.text)
//                        }
//                    }
//                }
//            }.continueWithTask {
//                scanner.process(inputImage)
//            }.addOnSuccessListener { barcodes ->
//                Napier.d("HHHH - barcodes:$barcodes")
//                for (barcode in barcodes) {
//                    barcode.rawValue?.let { onBarcodeDetected(it) }
//                }
//            }
//                .addOnCompleteListener {
////                    textScanner.close()
//                    imageProxy.close()
//                }
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    Napier.d("HHHH - barcodes:${barcodes.joinToString { it?.rawValue.orEmpty() }}")
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { onBarcodeDetected(it) }
                    }
                }.addOnCompleteListener {
                    Napier.d("HHHH addOnCompleteListener")
                    imageProxy.image?.close()
                    imageProxy.close()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Napier.e { "HHHH - Error processing image: $e" }
        } finally {
            imageProxy.close()
        }
    } else {
        imageProxy.close()
    }
}

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
@Throws(CameraAccessException::class)
private fun getRotationCompensation(
    cameraId: String,
    activity: Activity,
    isFrontFacing: Boolean
): Int {
    // Get the device's current rotation relative to its "native" orientation.
    // Then, from the ORIENTATIONS table, look up the angle the image must be
    // rotated to compensate for the device's rotation.
    val deviceRotation = activity.windowManager.defaultDisplay.rotation
    var rotationCompensation = ORIENTATIONS.get(deviceRotation)

    // Get the device's sensor orientation.
    val cameraManager = activity.getSystemService(CAMERA_SERVICE) as CameraManager
    val sensorOrientation = cameraManager
        .getCameraCharacteristics(cameraId)
        .get(CameraCharacteristics.SENSOR_ORIENTATION)!!

    if (isFrontFacing) {
        rotationCompensation = (sensorOrientation + rotationCompensation) % 360
    } else { // back-facing
        rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360
    }
    return rotationCompensation
}
