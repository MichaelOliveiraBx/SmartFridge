package com.moliveira.app.smartfridge.modules.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitViewController
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureStillImageOutput
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecJPEG
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.position
import platform.CoreGraphics.CGRect
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreVideo.kCVPixelBufferPixelFormatTypeKey
import platform.CoreVideo.kCVPixelFormatType_32BGRA
import platform.Foundation.NSError
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRequest
import platform.Vision.VNRequestCompletionHandler
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import platform.posix.warn

@OptIn(ExperimentalForeignApi::class)
class UIViewCapturer(
    private val onTextRecognized: (String) -> Unit,
    private val coroutineScope: CoroutineScope,
//    coder: NSCoder,
) : UIViewController("UIViewCapturer", null), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {

    val queue = dispatch_queue_create("videoQueueController", null)
    val request = VNRecognizeTextRequest(object : VNRequestCompletionHandler {
        override fun invoke(p1: VNRequest?, p2: NSError?) {
            warn("Recognized text: ${p1?.results}")
            onTextRecognized(p1?.results.toString())
            error("Recognized text: ${p1?.results}")
        }
    })

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection
    ) {
        warn("captureOutput")
        if (didOutputSampleBuffer == null) return

        dispatch_async(queue) {
            val requestHandler = VNImageRequestHandler(
                didOutputSampleBuffer, options = emptyMap<Any?, Any>()
            )

            runCatching {
                requestHandler.performRequests(listOf(request), null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun CameraViewPlatformKmm(
    modifier: Modifier,
    onTextRecognized: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val device = AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo).firstOrNull { device ->
        (device as AVCaptureDevice).position == AVCaptureDevicePositionBack
    }!! as AVCaptureDevice

    val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null) as AVCaptureDeviceInput

    val output = AVCaptureStillImageOutput()
    output.outputSettings = mapOf(AVVideoCodecKey to AVVideoCodecJPEG)

    val session = AVCaptureSession()

    session.sessionPreset = AVCaptureSessionPresetPhoto

    session.addInput(input)
    session.addOutput(output)

    val videoOutput = AVCaptureVideoDataOutput()
    videoOutput.videoSettings = mapOf(kCVPixelBufferPixelFormatTypeKey to kCVPixelFormatType_32BGRA)
    videoOutput.alwaysDiscardsLateVideoFrames = true

    session.addOutput(videoOutput)

    val cameraPreviewLayer = remember { AVCaptureVideoPreviewLayer(session = session) }
    val queue = dispatch_queue_create("videoQueue", null)

    UIKitViewController(modifier = modifier, background = Color.Black, factory = {
        val controller = UIViewCapturer(
            onTextRecognized = onTextRecognized,
            coroutineScope = coroutineScope,
        )
        val container = UIView()
        controller.setView(container)
        warn("--------- controller.view: ${controller.view}")
        container.layer.addSublayer(cameraPreviewLayer)
        cameraPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        session.startRunning()
        videoOutput.setSampleBufferDelegate(controller, queue)
        controller
    }, onResize = { container: UIViewController, rect: CValue<CGRect> ->
        CATransaction.begin()
        CATransaction.setValue(true, kCATransactionDisableActions)
        container.view.layer.setFrame(rect)
        cameraPreviewLayer.setFrame(rect)
        CATransaction.commit()
    })
}