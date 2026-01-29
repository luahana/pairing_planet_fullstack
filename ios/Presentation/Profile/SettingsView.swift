import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var authManager: AuthManager
    @EnvironmentObject private var appState: AppState
    @AppStorage("appTheme") private var appTheme: AppTheme = .system
    @AppStorage("userMeasurement") private var measurementPreference: MeasurementPreference = .original
    @State private var showLogoutConfirmation = false
    @State private var showDeleteAccountConfirmation = false
    @State private var showEmailCopiedAlert = false
    @State private var showDeleteError = false
    @State private var deleteErrorMessage = ""
    @State private var isDeletingAccount = false

    private let userRepository: UserRepositoryProtocol = UserRepository()

    var body: some View {
        List {
            // Account section
            Section(String(localized: "settings.account")) {
                NavigationLink(destination: EditProfileView()) {
                    Label(String(localized: "settings.editProfile"), systemImage: "person.circle")
                }
                NavigationLink(destination: NotificationSettingsView()) {
                    Label(String(localized: "settings.notifications"), systemImage: "bell")
                }
                NavigationLink(destination: PrivacySettingsView()) {
                    Label(String(localized: "settings.privacy"), systemImage: "lock")
                }
            }

            // Preferences section
            Section(String(localized: "settings.preferences")) {
                NavigationLink(destination: ThemeSettingsView()) {
                    HStack {
                        Label(String(localized: "settings.theme"), systemImage: "circle.lefthalf.filled")
                        Spacer()
                        Text(appTheme.displayName).foregroundColor(DesignSystem.Colors.secondaryText)
                    }
                }
                NavigationLink(destination: LanguageSettingsView()) {
                    HStack {
                        Label(String(localized: "settings.language"), systemImage: "globe")
                        Spacer()
                        Text(LanguageManager.shared.currentLanguage.displayName).foregroundColor(DesignSystem.Colors.secondaryText)
                    }
                }
                NavigationLink(destination: UnitsSettingsView()) {
                    HStack {
                        Label(String(localized: "settings.units"), systemImage: "ruler")
                        Spacer()
                        Text(measurementPreference.displayName).foregroundColor(DesignSystem.Colors.secondaryText)
                    }
                }
            }

            // Support section
            Section(String(localized: "settings.support")) {
                Button {
                    sendFeedbackEmail()
                } label: {
                    Label(String(localized: "settings.sendFeedback"), systemImage: "envelope")
                }
                NavigationLink(destination: AboutView()) {
                    Label(String(localized: "settings.about"), systemImage: "info.circle")
                }
            }

            // Legal section
            Section(String(localized: "settings.legal")) {
                Link(destination: URL(string: "https://cookstemma.com/terms")!) {
                    Label(String(localized: "settings.termsOfService"), systemImage: "doc.text")
                }
                Link(destination: URL(string: "https://cookstemma.com/privacy")!) {
                    Label(String(localized: "settings.privacyPolicy"), systemImage: "hand.raised")
                }
            }

            // Account actions
            Section {
                Button(role: .destructive) {
                    showLogoutConfirmation = true
                } label: {
                    Label(String(localized: "settings.logout"), systemImage: "rectangle.portrait.and.arrow.right")
                }

                Button(role: .destructive) {
                    showDeleteAccountConfirmation = true
                } label: {
                    HStack {
                        Label(String(localized: "settings.deleteAccount"), systemImage: "trash")
                        if isDeletingAccount {
                            Spacer()
                            ProgressView()
                        }
                    }
                }
                .disabled(isDeletingAccount)
            }

            // Version info
            Section {
                HStack {
                    Text(String(localized: "settings.version"))
                    Spacer()
                    Text(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0")
                        .foregroundColor(DesignSystem.Colors.secondaryText)
                }
            }
            .listRowBackground(Color.clear)
            .listSectionSpacing(.compact)
        }
        .listStyle(.insetGrouped)
        .contentMargins(.bottom, 80, for: .scrollContent)
        .navigationTitle(String(localized: "settings.title"))
        .alert(String(localized: "settings.logout"), isPresented: $showLogoutConfirmation) {
            Button(String(localized: "common.cancel"), role: .cancel) { }
            Button(String(localized: "settings.logout"), role: .destructive) {
                Task {
                    await authManager.logout()
                    appState.navigateToHome()
                }
            }
        } message: {
            Text(String(localized: "settings.logoutConfirm"))
        }
        .alert(String(localized: "settings.deleteAccount"), isPresented: $showDeleteAccountConfirmation) {
            Button(String(localized: "common.cancel"), role: .cancel) { }
            Button(String(localized: "common.delete"), role: .destructive) {
                Task { await deleteAccount() }
            }
        } message: {
            Text(String(localized: "settings.deleteAccountConfirm"))
        }
        .alert(String(localized: "common.error"), isPresented: $showDeleteError) {
            Button(String(localized: "common.ok"), role: .cancel) { }
        } message: {
            Text(deleteErrorMessage)
        }
        .alert(String(localized: "settings.emailCopied"), isPresented: $showEmailCopiedAlert) {
            Button(String(localized: "common.ok"), role: .cancel) { }
        } message: {
            Text(String(localized: "settings.emailCopiedMessage"))
        }
    }

    private func sendFeedbackEmail() {
        let email = "contact@cookstemma.com"
        let subject = "Cookstemma Feedback"
        let encodedSubject = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? subject
        if let url = URL(string: "mailto:\(email)?subject=\(encodedSubject)"),
           UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
        } else {
            UIPasteboard.general.string = email
            showEmailCopiedAlert = true
        }
    }

    private func deleteAccount() async {
        isDeletingAccount = true
        defer { isDeletingAccount = false }

        let result = await userRepository.deleteAccount()
        switch result {
        case .success:
            await authManager.logout()
            appState.navigateToHome()
        case .failure(let error):
            deleteErrorMessage = error.localizedDescription
            showDeleteError = true
        }
    }
}

// MARK: - App Theme
enum AppTheme: String, CaseIterable {
    case system
    case light
    case dark

    var displayName: String {
        switch self {
        case .system: return String(localized: "theme.system")
        case .light: return String(localized: "theme.light")
        case .dark: return String(localized: "theme.dark")
        }
    }

    var colorScheme: ColorScheme? {
        switch self {
        case .system: return nil
        case .light: return .light
        case .dark: return .dark
        }
    }
}

struct ThemeSettingsView: View {
    @AppStorage("appTheme") private var appTheme: AppTheme = .system

    var body: some View {
        List {
            ForEach(AppTheme.allCases, id: \.self) { theme in
                Button {
                    appTheme = theme
                } label: {
                    HStack {
                        Label {
                            Text(theme.displayName)
                        } icon: {
                            Image(systemName: iconName(for: theme))
                        }
                        Spacer()
                        if appTheme == theme {
                            Image(systemName: "checkmark")
                                .foregroundColor(DesignSystem.Colors.primary)
                        }
                    }
                }
                .foregroundColor(DesignSystem.Colors.primaryText)
            }
        }
        .contentMargins(.bottom, 80, for: .scrollContent)
        .navigationTitle(String(localized: "settings.theme"))
    }

    private func iconName(for theme: AppTheme) -> String {
        switch theme {
        case .system: return "circle.lefthalf.filled"
        case .light: return "sun.max"
        case .dark: return "moon"
        }
    }
}

// MARK: - Settings Subviews
struct EditProfileView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = EditProfileViewModel()

    var body: some View {
        Form {
            profilePhotoSection
            informationSection
            socialLinksSection
        }
        .contentMargins(.bottom, 80, for: .scrollContent)
        .navigationTitle(String(localized: "editProfile.title"))
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                saveButton
            }
        }
        .task {
            await viewModel.loadProfile()
        }
        .alert(String(localized: "common.error"), isPresented: .constant(viewModel.error != nil)) {
            Button(String(localized: "common.ok")) { viewModel.resetError() }
        } message: {
            Text(viewModel.error ?? "")
        }
        .onChange(of: viewModel.saveSuccess) { _, success in
            if success { dismiss() }
        }
    }

    // MARK: - Profile Photo Section

    private var profilePhotoSection: some View {
        Section(String(localized: "editProfile.profilePhoto")) {
            HStack {
                Spacer()
                VStack {
                    Circle()
                        .fill(DesignSystem.Colors.secondaryBackground)
                        .frame(width: 100, height: 100)
                        .overlay(Image(systemName: "camera").font(.title))
                    Button(String(localized: "editProfile.changePhoto")) { }
                }
                Spacer()
            }
        }
    }

    // MARK: - Information Section

    private var informationSection: some View {
        Section {
            VStack(alignment: .leading, spacing: DesignSystem.Spacing.xs) {
                Text(String(localized: "editProfile.username"))
                    .font(DesignSystem.Typography.caption)
                    .foregroundColor(DesignSystem.Colors.secondaryText)

                HStack(spacing: DesignSystem.Spacing.sm) {
                    HStack {
                        Text("@")
                            .foregroundColor(DesignSystem.Colors.secondaryText)
                        TextField("username", text: $viewModel.username)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                    }
                    .padding(.horizontal, DesignSystem.Spacing.sm)
                    .padding(.vertical, DesignSystem.Spacing.xs)
                    .background(DesignSystem.Colors.secondaryBackground)
                    .cornerRadius(DesignSystem.CornerRadius.sm)

                    Button {
                        Task { await viewModel.checkUsernameAvailability() }
                    } label: {
                        if viewModel.isCheckingUsername {
                            ProgressView()
                                .frame(width: 60)
                        } else {
                            Text(String(localized: "editProfile.check"))
                                .frame(width: 60)
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(!viewModel.canCheckUsername)
                }

                usernameValidationMessage

                HStack {
                    Spacer()
                    Text(viewModel.usernameCharacterCount)
                        .font(DesignSystem.Typography.caption)
                        .foregroundColor(DesignSystem.Colors.secondaryText)
                }
            }

            VStack(alignment: .leading, spacing: DesignSystem.Spacing.xs) {
                Text(String(localized: "editProfile.bio"))
                    .font(DesignSystem.Typography.caption)
                    .foregroundColor(DesignSystem.Colors.secondaryText)

                TextField(String(localized: "editProfile.bioPlaceholder"), text: $viewModel.bio, axis: .vertical)
                    .lineLimit(3...6)
                    .onChange(of: viewModel.bio) { _, newValue in
                        // Limit bio to max length
                        if newValue.count > EditProfileViewModel.maxBioLength {
                            viewModel.bio = String(newValue.prefix(EditProfileViewModel.maxBioLength))
                        }
                    }

                HStack {
                    Spacer()
                    Text(viewModel.bioCharacterCount)
                        .font(DesignSystem.Typography.caption)
                        .foregroundColor(
                            viewModel.isBioOverLimit
                                ? .red
                                : DesignSystem.Colors.secondaryText
                        )
                }
            }
        } header: {
            Text(String(localized: "editProfile.information"))
        } footer: {
            Text(String(localized: "editProfile.usernameRules"))
                .font(DesignSystem.Typography.caption)
        }
    }

    // MARK: - Username Validation Message

    @ViewBuilder
    private var usernameValidationMessage: some View {
        if let formatError = viewModel.usernameFormatError {
            Label(formatError, systemImage: "exclamationmark.triangle.fill")
                .font(DesignSystem.Typography.caption)
                .foregroundColor(.orange)
        } else if viewModel.usernameAvailable == true {
            Label(String(localized: "editProfile.usernameAvailable"), systemImage: "checkmark.circle.fill")
                .font(DesignSystem.Typography.caption)
                .foregroundColor(.green)
        } else if viewModel.usernameAvailable == false {
            Label(String(localized: "editProfile.usernameTaken"), systemImage: "xmark.circle.fill")
                .font(DesignSystem.Typography.caption)
                .foregroundColor(.red)
        }
    }

    // MARK: - Social Links Section

    private var socialLinksSection: some View {
        Section(String(localized: "editProfile.socialLinks")) {
            VStack(alignment: .leading, spacing: DesignSystem.Spacing.xs) {
                Text(String(localized: "editProfile.youtubeUrl"))
                    .font(DesignSystem.Typography.caption)
                    .foregroundColor(DesignSystem.Colors.secondaryText)
                TextField("https://youtube.com/@channel", text: $viewModel.youtubeUrl)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .keyboardType(.URL)
            }

            VStack(alignment: .leading, spacing: DesignSystem.Spacing.xs) {
                Text(String(localized: "editProfile.instagramHandle"))
                    .font(DesignSystem.Typography.caption)
                    .foregroundColor(DesignSystem.Colors.secondaryText)
                TextField("@username", text: $viewModel.instagramHandle)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
            }
        }
    }

    // MARK: - Save Button

    private var saveButton: some View {
        Group {
            if viewModel.isSubmitting {
                ProgressView()
            } else {
                Button(String(localized: "common.save")) {
                    Task { await viewModel.saveProfile() }
                }
                .disabled(!viewModel.canSave)
            }
        }
    }
}

struct NotificationSettingsView: View {
    @State private var commentsEnabled = true
    @State private var followersEnabled = true
    @State private var newSavedEnabled = true
    @State private var newCookingLogsEnabled = true
    @State private var newVariantRecipesEnabled = true

    var body: some View {
        Form {
            Section(String(localized: "settings.pushNotifications")) {
                Toggle(String(localized: "settings.commentsReplies"), isOn: $commentsEnabled)
                Toggle(String(localized: "settings.newFollowers"), isOn: $followersEnabled)
                Toggle(String(localized: "settings.newSaved"), isOn: $newSavedEnabled)
                Toggle(String(localized: "settings.newCookingLogs"), isOn: $newCookingLogsEnabled)
                Toggle(String(localized: "settings.newVariantRecipes"), isOn: $newVariantRecipesEnabled)
            }
        }
        .contentMargins(.bottom, 80, for: .scrollContent)
        .navigationTitle(String(localized: "settings.notifications"))
    }
}

struct PrivacySettingsView: View {
    var body: some View {
        Form {
            Section(String(localized: "settings.blockedUsers")) {
                NavigationLink(String(localized: "settings.manageBlockedUsers")) {
                    BlockedUsersView()
                }
            }
        }
        .contentMargins(.bottom, 80, for: .scrollContent)
        .navigationTitle(String(localized: "settings.privacy"))
    }
}

struct BlockedUsersView: View {
    @StateObject private var viewModel = BlockedUsersViewModel()

    var body: some View {
        Group {
            switch viewModel.state {
            case .idle, .loading:
                LoadingView()
            case .loaded:
                if viewModel.blockedUsers.isEmpty {
                    EmptyStateView(icon: "hand.raised", title: String(localized: "settings.noBlockedUsers"), message: String(localized: "settings.blockedUsersMessage"))
                } else {
                    List {
                        ForEach(viewModel.blockedUsers) { user in
                            HStack {
                                AvatarView(url: user.avatarUrl, name: user.username, size: DesignSystem.AvatarSize.sm)
                                Text("@\(user.username)")
                                    .font(DesignSystem.Typography.subheadline)
                                Spacer()
                                Button(String(localized: "settings.unblock")) {
                                    Task { await viewModel.unblockUser(user) }
                                }
                                .font(DesignSystem.Typography.subheadline)
                                .foregroundColor(DesignSystem.Colors.primary)
                            }
                        }
                        if viewModel.hasMore && !viewModel.isLoadingMore {
                            Button(String(localized: "settings.loadMore")) {
                                Task { await viewModel.loadMore() }
                            }
                            .frame(maxWidth: .infinity)
                        }
                        if viewModel.isLoadingMore {
                            ProgressView()
                                .frame(maxWidth: .infinity)
                        }
                    }
                }
            case .error(let message):
                ErrorStateView(message: message) {
                    Task { await viewModel.loadBlockedUsers() }
                }
            }
        }
        .navigationTitle(String(localized: "settings.blockedUsers"))
        .onAppear {
            if case .idle = viewModel.state {
                Task { await viewModel.loadBlockedUsers() }
            }
        }
    }
}

// MARK: - Blocked Users ViewModel
@MainActor
final class BlockedUsersViewModel: ObservableObject {
    enum State: Equatable { case idle, loading, loaded, error(String) }

    @Published private(set) var state: State = .idle
    @Published private(set) var blockedUsers: [BlockedUser] = []
    @Published private(set) var hasMore = false
    @Published private(set) var isLoadingMore = false

    private let userRepository: UserRepositoryProtocol
    private var currentPage = 0

    init(userRepository: UserRepositoryProtocol = UserRepository()) {
        self.userRepository = userRepository
    }

    func loadBlockedUsers() async {
        state = .loading
        currentPage = 0
        let result = await userRepository.getBlockedUsers(page: 0)
        switch result {
        case .success(let response):
            blockedUsers = response.content
            hasMore = response.hasMore
            currentPage = response.page
            state = .loaded
        case .failure(let error):
            state = .error(error.localizedDescription)
        }
    }

    func loadMore() async {
        guard !isLoadingMore, hasMore else { return }
        isLoadingMore = true
        defer { isLoadingMore = false }

        let nextPage = currentPage + 1
        let result = await userRepository.getBlockedUsers(page: nextPage)
        if case .success(let response) = result {
            blockedUsers.append(contentsOf: response.content)
            hasMore = response.hasMore
            currentPage = response.page
        }
    }

    func unblockUser(_ user: BlockedUser) async {
        let result = await userRepository.unblockUser(userId: user.id)
        if case .success = result {
            blockedUsers.removeAll { $0.id == user.id }
        }
    }
}

struct LanguageSettingsView: View {
    @StateObject private var languageManager = LanguageManager.shared
    @State private var showRestartConfirmation = false
    @State private var selectedLanguage: AppLanguage?

    var body: some View {
        List {
            ForEach(AppLanguage.allCases) { language in
                Button {
                    if languageManager.currentLanguage != language {
                        selectedLanguage = language
                        showRestartConfirmation = true
                    }
                } label: {
                    HStack {
                        Text(language.displayName)
                        Spacer()
                        if languageManager.currentLanguage == language {
                            Image(systemName: "checkmark")
                                .foregroundColor(DesignSystem.Colors.primary)
                        }
                    }
                }
                .foregroundColor(DesignSystem.Colors.primaryText)
            }
        }
        .contentMargins(.bottom, 80, for: .scrollContent)
        .navigationTitle(String(localized: "settings.language"))
        .alert(String(localized: "settings.changeLanguage"), isPresented: $showRestartConfirmation) {
            Button(String(localized: "common.cancel"), role: .cancel) {
                selectedLanguage = nil
            }
            Button(String(localized: "settings.restart"), role: .destructive) {
                if let language = selectedLanguage {
                    languageManager.setLanguage(language)
                }
                selectedLanguage = nil
            }
        } message: {
            Text(String(localized: "settings.restartConfirmMessage"))
        }
    }
}

struct UnitsSettingsView: View {
    @AppStorage("userMeasurement") private var measurementPreference: MeasurementPreference = .original

    var body: some View {
        List {
            ForEach(MeasurementPreference.allCases, id: \.self) { preference in
                Button {
                    measurementPreference = preference
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(preference.displayName)
                            Text(preference.description)
                                .font(DesignSystem.Typography.caption)
                                .foregroundColor(DesignSystem.Colors.secondaryText)
                        }
                        Spacer()
                        if measurementPreference == preference {
                            Image(systemName: "checkmark")
                                .foregroundColor(DesignSystem.Colors.primary)
                        }
                    }
                }
                .foregroundColor(DesignSystem.Colors.primaryText)
            }
        }
        .contentMargins(.bottom, 80, for: .scrollContent)
        .navigationTitle(String(localized: "settings.units"))
    }
}

struct AboutView: View {
    var body: some View {
        List {
            Section {
                VStack(spacing: DesignSystem.Spacing.md) {
                    Image(systemName: "fork.knife.circle.fill")
                        .font(.system(size: 60))
                        .foregroundColor(DesignSystem.Colors.primary)
                    Text("Cookstemma")
                        .font(.title2)
                        .fontWeight(.bold)
                    Text(String(localized: "settings.shareYourCookingJourney"))
                        .foregroundColor(DesignSystem.Colors.secondaryText)
                }
                .frame(maxWidth: .infinity)
                .padding()
            }

            Section {
                Link(destination: URL(string: "https://cookstemma.com")!) {
                    Label(String(localized: "settings.website"), systemImage: "globe")
                }
            }

            Section {
                HStack {
                    Text(String(localized: "settings.version"))
                    Spacer()
                    Text(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0")
                        .foregroundColor(DesignSystem.Colors.secondaryText)
                }
                HStack {
                    Text(String(localized: "settings.build"))
                    Spacer()
                    Text(Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1")
                        .foregroundColor(DesignSystem.Colors.secondaryText)
                }
            }
        }
        .contentMargins(.bottom, 80, for: .scrollContent)
        .navigationTitle(String(localized: "settings.about"))
    }
}

#Preview { NavigationStack { SettingsView().environmentObject(AuthManager.shared).environmentObject(AppState()) } }
