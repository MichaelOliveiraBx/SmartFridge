package com.moliveira.app.smartfridge.modules.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitViewController
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.compose.koinInject
import platform.CoreGraphics.CGRect
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIViewController


@OptIn(ExperimentalForeignApi::class)
@Composable
fun CameraViewPlatformNative(
    modifier: Modifier,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
) {
    val cameraInterface: KMMCameraRecognizerInterface = koinInject()
    UIKitViewController(
        modifier = modifier,
        background = Color.Black,
        factory = {
            cameraInterface.listen(
                textRecognizedListener = onTextRecognized,
                barCodeRecognizedListener = onBarcodeRecognized,
            )
            cameraInterface.viewControllerProvider.invoke()
        }, onResize = { container: UIViewController, rect: CValue<CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            container.view.layer.setFrame(rect)
            CATransaction.commit()
        }
    )
}