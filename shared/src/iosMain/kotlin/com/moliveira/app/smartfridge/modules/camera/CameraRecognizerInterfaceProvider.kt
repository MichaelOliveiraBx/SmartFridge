package com.moliveira.app.smartfridge.modules.camera

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.UIKit.UIViewController

class KMMCameraRecognizerInterface {
    lateinit var viewControllerProvider: () -> UIViewController
        private set
    private var textRecognizedListener: (String) -> Unit = {}
    private var barCodeRecognizedListener: (String) -> Unit = {}

    fun setup(
        viewControllerProvider: () -> UIViewController,
    ) {
        this.viewControllerProvider = viewControllerProvider
    }

    fun onTextRecognized(text: String) {
        textRecognizedListener(text)
    }

    fun onBarcodeRecognized(code: String) {
        barCodeRecognizedListener(code)
    }

    fun listen(
        textRecognizedListener: (String) -> Unit,
        barCodeRecognizedListener: (String) -> Unit,
    ) {
        this.textRecognizedListener = textRecognizedListener
        this.barCodeRecognizedListener = barCodeRecognizedListener
    }
}

class KMMCameraRecognizerInterfaceProvider : KoinComponent {
    val kmmInterface by inject<KMMCameraRecognizerInterface>()
}