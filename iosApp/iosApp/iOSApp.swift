import SwiftUI
import AVFoundation
import ComposeApp

@main
struct iOSApp: App {
    
    init() {
        IOSKmmSetup.shared.setup()
        CameraViewPlatformNativeKt.cameraViewControllerProvider = {
            let controller = CameraViewController()
            controller.onTextChange = {
                CameraViewPlatformNativeKt.textRecognizedProvider($0)
            }
            controller.onBarCodeFound = {
                CameraViewPlatformNativeKt.barCodeRecognizedProvider($0)
            }
            return controller
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
