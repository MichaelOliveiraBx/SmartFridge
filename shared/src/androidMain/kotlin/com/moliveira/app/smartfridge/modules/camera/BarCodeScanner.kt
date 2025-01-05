package com.moliveira.app.smartfridge.modules.camera

import android.content.Context
import com.google.android.odml.image.MlImage
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.tasks.await

class BarCodeScanner(
    context: Context,
) {
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    suspend fun scanBarCode(mlImage: MlImage): String? {
        return runCatching {
            scanner.process(mlImage).await().joinToString { it.rawValue ?: "" }
        }.getOrNull()
    }
}