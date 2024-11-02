package com.moliveira.app.smartfridge.modules.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.cinterop.ExperimentalForeignApi


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraViewPlatform(
    modifier: Modifier,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
) {
//    Box(
//        modifier = modifier.background(
//            color = Color.LightGray
//        ),
//    )
    CameraViewPlatformNative(
        modifier = modifier,
        onTextRecognized = onTextRecognized,
        onBarcodeRecognized = onBarcodeRecognized,
    )
}