import SwiftUI
import AVFoundation
import shared

@main
struct iOSApp: App {

    init() {
        IOSKmmSetup.shared.setup()
        let cameraInterface = KMMCameraRecognizerInterfaceProvider().kmmInterface
        cameraInterface.setup {
            let controller = CameraViewController()
            controller.onTextChange = cameraInterface.onTextRecognized
            controller.onBarCodeFound = cameraInterface.onBarcodeRecognized
            return controller
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
