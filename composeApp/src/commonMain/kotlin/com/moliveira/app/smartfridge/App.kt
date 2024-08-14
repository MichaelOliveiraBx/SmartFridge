package com.moliveira.app.smartfridge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moliveira.app.smartfridge.modules.camera.CameraView
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var text by remember { mutableStateOf("") }
        var barcode by remember { mutableStateOf("") }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text)
            Spacer(Modifier.height(8.dp))
            Text(barcode)
            Spacer(Modifier.height(8.dp))
            CameraView(
                modifier = Modifier.fillMaxWidth().weight(1f),
                onTextRecognized = { text = it },
                onBarcodeRecognized = { barcode = it }
            )
        }
    }
}