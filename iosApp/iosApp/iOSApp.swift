import SwiftUI
import StoreKit
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
            InAppPurchaseView()
        }
    }
}

class InAppPurchaseManager: NSObject {
    static let shared = InAppPurchaseManager()
    
    var products: [SKProduct] = []
    
    let productIdentifiers: Set<String> = ["test_product","premium_monthly"]

    func fetchProducts() {
        IOSKmmSetupKt.logKmm(message: "fetchProducts:")
        let request = SKProductsRequest(productIdentifiers: productIdentifiers)
        request.delegate = self
        request.start()
    }
}

extension InAppPurchaseManager: SKProductsRequestDelegate {
    func productsRequest(_ request: SKProductsRequest, didReceive response: SKProductsResponse) {
        IOSKmmSetupKt.logKmm(message: "RESPONCE: \(response)")
        self.products = response.products
        for product in products {
            IOSKmmSetupKt.logKmm(message: "Produit disponible : \(product.localizedTitle) - \(product.price) \(product.priceLocale.currencySymbol ?? "")")
        }

        if response.invalidProductIdentifiers.isEmpty == false {
            IOSKmmSetupKt.logKmm(message: "Identifiants invalides : \(response.invalidProductIdentifiers)")
        }
    }
    
    func request(_ request: SKRequest, didFailWithError error: Error) {
        IOSKmmSetupKt.logKmm(message: "Failed to fetch products: \(error.localizedDescription)")
    }
}

struct Product: Identifiable {
    let id: String
    let title: String
    let price: String
}

class StoreManager: NSObject, ObservableObject {
    @Published var products: [Product] = []
    @Published var isLoading = true

    func fetchProducts() {
        // Liste des Product IDs configurés dans App Store Connect
        let productIdentifiers: Set<String> = ["test_product", "premium_monthly", "premium_weekly"]

        let request = SKProductsRequest(productIdentifiers: productIdentifiers)
        request.delegate = self
        request.start()
    }
}

extension StoreManager: SKProductsRequestDelegate {
    func productsRequest(_ request: SKProductsRequest, didReceive response: SKProductsResponse) {
        DispatchQueue.main.async {
            self.products = response.products.map { product in
                Product(
                    id: product.productIdentifier,
                    title: product.localizedTitle,
                    price: product.localizedPrice ?? "N/A"
                )
            }
            self.isLoading = false
        }
    }
    
}

extension SKProduct {
    var localizedPrice: String? {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = priceLocale
        return formatter.string(from: price)
    }
}

struct InAppPurchaseView: View {
    @StateObject private var storeManager = StoreManager()

    var body: some View {
        NavigationView {
            Group {
                if storeManager.isLoading {
                    ProgressView("Chargement des produits...")
                } else if storeManager.products.isEmpty {
                    Text("Aucun produit disponible.")
                        .font(.headline)
                        .padding()
                } else {
                    List(storeManager.products) { product in
                        HStack {
                            VStack(alignment: .leading) {
                                Text(product.title)
                                    .font(.headline)
                                Text(product.price)
                                    .font(.subheadline)
                                    .foregroundColor(.gray)
                            }
                            Spacer()
                            Button("Acheter") {
                                // Ajouter ici la logique d'achat
                                print("Achat de \(product.id)")
                            }
                            .buttonStyle(.borderedProminent)
                        }
                    }
                }
            }
            .navigationTitle("Produits")
            .onAppear {
                storeManager.fetchProducts()
            }
        }
    }
}

