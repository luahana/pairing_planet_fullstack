import SwiftUI

// MARK: - App Notifications
extension Notification.Name {
    /// Posted when a recipe's save state changes
    /// userInfo: ["recipeId": String, "isSaved": Bool]
    static let recipeSaveStateChanged = Notification.Name("recipeSaveStateChanged")

    /// Posted when a log's save state changes
    /// userInfo: ["logId": String, "isSaved": Bool]
    static let logSaveStateChanged = Notification.Name("logSaveStateChanged")

    /// Posted when a log is deleted
    /// userInfo: ["logId": String]
    static let logDeleted = Notification.Name("logDeleted")
}

@main
struct CookstemmaApp: App {
    @StateObject private var authManager = AuthManager.shared
    @StateObject private var appState = AppState()
    @AppStorage("appTheme") private var appTheme: AppTheme = .system

    init() {
        FirebaseService.shared.configure()
    }

    var body: some Scene {
        WindowGroup {
            Group {
                switch authManager.authState {
                case .unknown:
                    SplashView()
                case .authenticated, .unauthenticated:
                    MainTabView()
                        .sheet(isPresented: $appState.showLoginSheet) {
                            LoginView(onLoginSuccess: {
                                appState.onLoginSuccess()
                            })
                            .environmentObject(authManager)
                        }
                }
            }
            .environmentObject(authManager)
            .environmentObject(appState)
            .preferredColorScheme(appTheme.colorScheme)
            .animation(.easeInOut(duration: 0.3), value: authManager.authState)
            .onAppear {
                setupAppearance()
            }
            .onOpenURL { url in
                _ = FirebaseService.handleOpenURL(url)
            }
        }
    }

    private func setupAppearance() {
        let appearance = UITabBarAppearance()
        appearance.configureWithOpaqueBackground()
        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }
}
