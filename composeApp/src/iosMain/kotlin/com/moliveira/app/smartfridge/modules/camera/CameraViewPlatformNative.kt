package com.moliveira.app.smartfridge.modules.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitViewController
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRect
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIViewController

var cameraViewControllerProvider: (() -> UIViewController)? = null
var textRecognizedProvider: (String) -> Unit = { }
var barCodeRecognizedProvider: (String) -> Unit = { }

@OptIn(ExperimentalForeignApi::class)
@Composable
fun CameraViewPlatformNative(
    modifier: Modifier,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
) {
    UIKitViewController(
        modifier = modifier,
        background = Color.Black,
        factory = {
            val controller = cameraViewControllerProvider?.invoke()
                ?: throw IllegalStateException("cameraViewControllerProvider is not set")
            textRecognizedProvider = onTextRecognized
            barCodeRecognizedProvider = onBarcodeRecognized
            controller
        }, onResize = { container: UIViewController, rect: CValue<CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            container.view.layer.setFrame(rect)
            CATransaction.commit()
        }
    )
}