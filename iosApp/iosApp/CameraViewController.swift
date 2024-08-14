//
//  CameraViewController.swift
//  iosApp
//
//  Created by Michael Oliveira on 14/08/2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import AVFoundation
import UIKit
import Vision
import SwiftUI

struct CameraView: UIViewControllerRepresentable {
    @Binding var recognizedText: String

    func makeUIViewController(context: Context) -> CameraViewController {
        let controller = CameraViewController()
        controller.onTextChange = {
            recognizedText = $0
        }
        return controller
    }
    
    func updateUIViewController(_ uiViewController: CameraViewController, context: Context) {
    }
}

class CameraViewController: UIViewController, AVCaptureVideoDataOutputSampleBufferDelegate, AVCaptureMetadataOutputObjectsDelegate {
    var session: AVCaptureSession?
    var onTextChange : (String) -> () = { $0 }
    var onBarCodeFound : (String) -> () = { $0 }

    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupCamera()
        setupVision()
    }
    
    private func setupCamera() {
        session = AVCaptureSession()
        guard let captureDevice = AVCaptureDevice.default(for: .video) else { return }
        guard let input = try? AVCaptureDeviceInput(device: captureDevice) else { return }
        session?.addInput(input)
        session?.startRunning()
        let output = AVCaptureVideoDataOutput()
        output.setSampleBufferDelegate(self, queue: DispatchQueue(label: "videoQueue"))
        session?.addOutput(output)
        
        let metadataOutput = AVCaptureMetadataOutput()

        if (session?.canAddOutput(metadataOutput) == true) {
            session?.addOutput(metadataOutput)
            
            metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
            metadataOutput.metadataObjectTypes = [.ean8, .ean13, .pdf417, .qr] // Ajouter d'autres types de codes-barres si nécessaire
        } else {
            return
        }
        
        let previewLayer = AVCaptureVideoPreviewLayer(session: session!)
        previewLayer.frame = view.bounds
        view.layer.addSublayer(previewLayer)
    }
    
    private func setupVision() {
        let textRecognitionRequest = VNRecognizeTextRequest { [weak self] (request, error) in
            if let observations = request.results as? [VNRecognizedTextObservation] {
                let text = observations.compactMap { $0.topCandidates(1).first?.string }.joined(separator: ", ")
                DispatchQueue.main.async {
                    self?.onTextChange(text)
                }
            }
        }
        textRecognitionRequest.recognitionLevel = .accurate
        textRecognitionRequest.usesLanguageCorrection = true
    }
    
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        guard let pixelBuffer: CVPixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }

        let request = VNRecognizeTextRequest { (request, error) in
            if let results = request.results as? [VNRecognizedTextObservation] {
                for result in results {
                    if let candidate = result.topCandidates(1).first {
                        print("Detected text: \(candidate.string)")
                        DispatchQueue.main.async {
                            self.onTextChange(candidate.string)
                        }
                    }
                }
            }
        }

        request.recognitionLevel = .accurate
        request.recognitionLanguages = ["fr"] // Ou autre langue si nécessaire
        request.usesLanguageCorrection = true
        
        let requestHandler = VNImageRequestHandler(cvPixelBuffer: pixelBuffer, options: [:])
        try? requestHandler.perform([request])
    }
    
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
         if let metadataObject = metadataObjects.first {
             guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject else { return }
             guard let stringValue = readableObject.stringValue else { return }
             self.onBarCodeFound(stringValue)
         }
     }
}
