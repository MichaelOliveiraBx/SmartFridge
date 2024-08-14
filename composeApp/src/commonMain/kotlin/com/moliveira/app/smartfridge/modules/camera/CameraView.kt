package com.moliveira.app.smartfridge.modules.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
) {
    CameraViewPlatform(
        modifier = modifier,
        onTextRecognized = onTextRecognized,
        onBarcodeRecognized =   onBarcodeRecognized,
    )
}

@Composable
expect fun CameraViewPlatform(
    modifier: Modifier,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
)