package com.moliveira.app.smartfridge.modules.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun inPreviewMode(): Boolean = LocalInspectionMode.current

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
) {
    if (inPreviewMode()) {
        Box(
            modifier = modifier.background(Color.LightGray),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "CameraView",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
        }
    } else {
        CameraViewPlatform(
            modifier = modifier,
            onTextRecognized = onTextRecognized,
            onBarcodeRecognized = onBarcodeRecognized,
        )
    }
}

@Composable
expect fun CameraViewPlatform(
    modifier: Modifier,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
)