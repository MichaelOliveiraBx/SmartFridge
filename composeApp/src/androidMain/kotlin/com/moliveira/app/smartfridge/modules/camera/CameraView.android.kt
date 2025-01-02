package com.moliveira.app.smartfridge.modules.camera

import android.util.SparseIntArray
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
            it?.rawValue?.let { onBarcodeRecognized(it) }
        },
        onTextRecognized = {
            onTextRecognized(it)
        }
    )
}