package com.moliveira.app.smartfridge.modules.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraViewPlatform(
    modifier: Modifier,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
) {
    CameraViewPlatformNative(
        modifier = modifier,
        onTextRecognized = onTextRecognized,
        onBarcodeRecognized = onBarcodeRecognized,
    )
}