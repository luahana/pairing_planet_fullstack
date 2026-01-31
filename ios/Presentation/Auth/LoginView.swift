import SwiftUI
import AuthenticationServices

struct LoginView: View {
    @StateObject private var viewModel: LoginViewModel
    @Environment(\.colorScheme) private var colorScheme

    let onLoginSuccess: () -> Void

    init(
        authManager: AuthManagerProtocol = AuthManager.shared,
        onLoginSuccess: @escaping () -> Void
    ) {
        _viewModel = StateObject(wrappedValue: LoginViewModel(authManager: authManager))
        self.onLoginSuccess = onLoginSuccess
    }

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            // Logo and tagline
            VStack(spacing: 8) {
                Text("Cookstemma")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundColor(.brandOrange)

                Text(String(localized: "auth.tagline"))
                    .font(.body)
                    .foregroundColor(.secondary)
            }

            Spacer()

            // Sign in buttons
            VStack(spacing: 16) {
                // Sign in with Google
                SignInButton(
                    title: String(localized: "auth.continueGoogle"),
                    icon: Image(systemName: "g.circle.fill"),
                    backgroundColor: .white,
                    foregroundColor: .black,
                    isLoading: viewModel.isLoading
                ) {
                    Task {
                        await viewModel.signInWithGoogle()
                    }
                }

                // Sign in with Apple
                SignInButton(
                    title: String(localized: "auth.continueApple"),
                    icon: Image(systemName: "apple.logo"),
                    backgroundColor: .black,
                    foregroundColor: .white,
                    isLoading: viewModel.isLoading
                ) {
                    Task {
                        await viewModel.signInWithApple()
                    }
                }
            }
            .padding(.horizontal, 32)
            .disabled(viewModel.isLoading)

            // Error message
            if let error = viewModel.error {
                HStack {
                    Text(error)
                        .font(.footnote)
                        .foregroundColor(.red)
                    Spacer()
                    Button(String(localized: "common.dismiss")) {
                        viewModel.clearError()
                    }
                    .font(.footnote)
                }
                .padding()
                .background(Color.red.opacity(0.1))
                .cornerRadius(8)
                .padding(.horizontal, 32)
                .padding(.top, 16)
            }

            Spacer()

            // Terms and privacy
            Text(String(localized: "auth.termsNotice"))
                .font(.caption)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
                .padding(.bottom, 32)
        }
        .onChange(of: viewModel.isAuthenticated) { _, isAuthenticated in
            if isAuthenticated {
                onLoginSuccess()
            }
        }
    }
}

// MARK: - Sign In Button

struct SignInButton: View {
    let title: String
    let icon: Image
    let backgroundColor: Color
    let foregroundColor: Color
    let isLoading: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: foregroundColor))
                } else {
                    icon
                        .resizable()
                        .scaledToFit()
                        .frame(width: 20, height: 20)
                }

                Text(title)
                    .font(.body.weight(.medium))
            }
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(backgroundColor)
            .foregroundColor(foregroundColor)
            .cornerRadius(12)
            .shadow(color: .black.opacity(0.1), radius: 2, y: 1)
        }
        .disabled(isLoading)
    }
}

// MARK: - Login ViewModel

@MainActor
final class LoginViewModel: ObservableObject {
    @Published private(set) var isLoading = false
    @Published private(set) var error: String?
    @Published private(set) var isAuthenticated = false

    private let authManager: AuthManagerProtocol
    private let firebaseService: FirebaseServiceProtocol

    init(
        authManager: AuthManagerProtocol,
        firebaseService: FirebaseServiceProtocol = FirebaseService.shared
    ) {
        self.authManager = authManager
        self.firebaseService = firebaseService
    }

    func signInWithGoogle() async {
        isLoading = true
        error = nil

        do {
            // 1. Sign in with Google and get Firebase ID token
            #if DEBUG
            print("[Login] Starting Google Sign-In...")
            #endif
            let firebaseToken = try await firebaseService.signInWithGoogle()
            #if DEBUG
            print("[Login] Got Firebase token, calling backend...")
            #endif

            // 2. Exchange Firebase token with backend for app tokens
            try await authManager.loginWithFirebase(token: firebaseToken)
            #if DEBUG
            print("[Login] Success!")
            #endif
            isAuthenticated = true
        } catch let apiError as APIError {
            #if DEBUG
            print("[Login] APIError: \(apiError)")
            #endif
            self.error = apiError.errorDescription ?? "Unknown API error"
        } catch {
            #if DEBUG
            print("[Login] Other error: \(type(of: error)) - \(error)")
            #endif
            self.error = Self.userFriendlyError(from: error)
        }

        isLoading = false
    }

    func signInWithApple() async {
        isLoading = true
        error = nil

        do {
            // 1. Sign in with Apple and get Firebase ID token
            let firebaseToken = try await firebaseService.signInWithApple()

            // 2. Exchange Firebase token with backend for app tokens
            try await authManager.loginWithFirebase(token: firebaseToken)
            isAuthenticated = true
        } catch {
            self.error = Self.userFriendlyError(from: error)
        }

        isLoading = false
    }

    func clearError() {
        error = nil
    }

    // MARK: - Error Handling

    private static func userFriendlyError(from error: Error) -> String? {
        let nsError = error as NSError

        // Google Sign-In errors (domain: com.google.GIDSignIn)
        if nsError.domain == "com.google.GIDSignIn" {
            switch nsError.code {
            case -5: // User canceled
                return nil
            case -4: // No current user / sign-in required
                return "Please sign in again"
            case -2: // Keychain error
                return "Authentication error. Please try again"
            case -1: // Unknown error
                return "Sign-in failed. Please try again"
            default:
                return "Sign-in failed. Please try again"
            }
        }

        // Apple Sign-In errors (domain: com.apple.AuthenticationServices.AuthorizationError)
        if nsError.domain == "com.apple.AuthenticationServices.AuthorizationError" ||
           nsError.domain == ASAuthorizationError.errorDomain {
            switch nsError.code {
            case 1001: // User canceled
                return nil
            case 1000: // Unknown
                return "Sign-in failed. Please try again"
            case 1002: // Invalid response
                return "Invalid response from Apple. Please try again"
            case 1003: // Not handled
                return "Sign-in not available"
            default:
                return "Sign-in failed. Please try again"
            }
        }

        // Firebase errors
        if nsError.domain.contains("firebase") || nsError.domain.contains("Firebase") {
            return "Authentication failed. Please try again"
        }

        // Generic fallback - use localized description if reasonable length
        let localizedDesc = error.localizedDescription
        if localizedDesc.count < 100 && !localizedDesc.contains("Domain=") {
            return localizedDesc
        }

        return "Something went wrong. Please try again"
    }
}

// MARK: - Brand Color Extension

extension Color {
    static let brandOrange = Color(red: 255/255, green: 107/255, blue: 53/255)
}

// MARK: - Preview

#Preview {
    LoginView(onLoginSuccess: {})
}
