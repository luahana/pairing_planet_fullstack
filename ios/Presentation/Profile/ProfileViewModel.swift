import Foundation
import SwiftUI
import Combine

enum ProfileState: Equatable { case idle, loading, loaded, error(String) }
enum ProfileTab: String, CaseIterable { case recipes, logs, saved }
enum VisibilityFilter: String, CaseIterable {
    case all, publicOnly, privateOnly

    var title: String {
        switch self {
        case .all: return String(localized: "filter.all")
        case .publicOnly: return String(localized: "filter.public")
        case .privateOnly: return String(localized: "filter.private")
        }
    }
}

enum SavedContentFilter: String, CaseIterable {
    case all, recipes, logs

    var title: String {
        switch self {
        case .all: return String(localized: "filter.all")
        case .recipes: return String(localized: "profile.recipes")
        case .logs: return String(localized: "profile.logs")
        }
    }
}

@MainActor
final class ProfileViewModel: ObservableObject {
    @Published private(set) var state: ProfileState = .idle
    @Published private(set) var profile: UserProfile?
    @Published private(set) var myProfile: MyProfile?
    @Published private(set) var isOwnProfile: Bool
    @Published private(set) var recipes: [RecipeSummary] = []
    @Published private(set) var logs: [FeedLogItem] = []
    @Published private(set) var savedRecipes: [RecipeSummary] = []
    @Published private(set) var savedLogs: [FeedLogItem] = []
    @Published private(set) var isLoadingContent = false
    @Published private(set) var hasMoreRecipes = true
    @Published private(set) var hasMoreLogs = true
    @Published private(set) var hasMoreSavedRecipes = true
    @Published private(set) var hasMoreSavedLogs = true
    @Published private(set) var savedContentVersion: Int = 0
    @Published var selectedTab: ProfileTab = .recipes
    @Published var visibilityFilter: VisibilityFilter = .all
    @Published var savedContentFilter: SavedContentFilter = .all

    private let userId: String?
    private let userRepository: UserRepositoryProtocol
    private let logRepository: CookingLogRepositoryProtocol
    private let savedContentRepository: SavedContentRepositoryProtocol
    private var recipesNextCursor: String?
    private var logsNextCursor: String?
    private var savedRecipesNextCursor: String?
    private var savedLogsNextCursor: String?
    private var cancellables = Set<AnyCancellable>()

    init(
        userId: String? = nil,
        userRepository: UserRepositoryProtocol = UserRepository(),
        logRepository: CookingLogRepositoryProtocol = CookingLogRepository(),
        savedContentRepository: SavedContentRepositoryProtocol = SavedContentRepository()
    ) {
        self.userId = userId
        self.userRepository = userRepository
        self.logRepository = logRepository
        self.savedContentRepository = savedContentRepository
        self.isOwnProfile = userId == nil
        setupSaveStateObserver()
    }

    private func setupSaveStateObserver() {
        guard isOwnProfile else {
            #if DEBUG
            print("[ProfileVM] NOT own profile - skipping observer setup")
            #endif
            return
        }

        #if DEBUG
        print("========== [ProfileVM] SETTING UP OBSERVERS ==========")
        #endif

        // Observe SavedItemsManager for saved recipes changes
        SavedItemsManager.shared.$savedRecipes
            .receive(on: DispatchQueue.main)
            .sink { [weak self] recipes in
                guard let self = self else {
                    #if DEBUG
                    print("[ProfileVM] RECIPE OBSERVER - self is nil!")
                    #endif
                    return
                }
                #if DEBUG
                print("========== [ProfileVM] RECIPE OBSERVER FIRED ==========")
                print("[ProfileVM] Received \(recipes.count) recipes from SavedItemsManager")
                for (index, r) in recipes.prefix(5).enumerated() {
                    print("[ProfileVM]   [\(index)] \(r.id): \(r.coverImageUrl ?? "NIL")")
                }
                print("[ProfileVM] BEFORE assignment - self.savedRecipes.count: \(self.savedRecipes.count)")
                #endif
                self.savedRecipes = recipes
                self.savedContentVersion += 1
                #if DEBUG
                print("[ProfileVM] AFTER assignment - self.savedRecipes.count: \(self.savedRecipes.count)")
                print("[ProfileVM] savedContentVersion: \(self.savedContentVersion)")
                print("========== [ProfileVM] RECIPE OBSERVER END ==========")
                #endif
            }
            .store(in: &cancellables)

        // Observe SavedItemsManager for saved logs changes
        SavedItemsManager.shared.$savedLogs
            .receive(on: DispatchQueue.main)
            .sink { [weak self] logs in
                guard let self = self else {
                    #if DEBUG
                    print("[ProfileVM] LOG OBSERVER - self is nil!")
                    #endif
                    return
                }
                #if DEBUG
                print("========== [ProfileVM] LOG OBSERVER FIRED ==========")
                print("[ProfileVM] Received \(logs.count) logs from SavedItemsManager")
                for (index, log) in logs.prefix(5).enumerated() {
                    print("[ProfileVM]   [\(index)] \(log.id): \(log.thumbnailUrl ?? "NIL")")
                }
                print("[ProfileVM] BEFORE assignment - self.savedLogs.count: \(self.savedLogs.count)")
                #endif
                self.savedLogs = logs
                self.savedContentVersion += 1
                #if DEBUG
                print("[ProfileVM] AFTER assignment - self.savedLogs.count: \(self.savedLogs.count)")
                print("[ProfileVM] savedContentVersion: \(self.savedContentVersion)")
                print("========== [ProfileVM] LOG OBSERVER END ==========")
                #endif
            }
            .store(in: &cancellables)

        #if DEBUG
        print("[ProfileVM] Observers setup complete")
        #endif
    }

    func loadProfile() {
        state = .loading
        Task {
            if isOwnProfile { await loadMyProfile() }
            else if let id = userId { await loadUserProfile(id) }
        }
    }

    func loadContent() {
        #if DEBUG
        print("[Profile] loadContent called for tab: \(selectedTab)")
        #endif
        Task {
            switch selectedTab {
            case .recipes: await loadRecipes(refresh: true)
            case .logs: await loadLogs(refresh: true)
            case .saved: await loadSavedContent(refresh: true)
            }
        }
    }

    var savedCount: Int {
        // Use local published properties for proper SwiftUI updates
        savedRecipes.count + savedLogs.count
    }

    func toggleFollow() async {
        guard let profile = profile, !isOwnProfile else { return }
        let wasFollowing = profile.isFollowing
        updateFollowState(!wasFollowing)
        let result = wasFollowing ? await userRepository.unfollow(userId: profile.id) : await userRepository.follow(userId: profile.id)
        if case .failure = result { updateFollowState(wasFollowing) }
    }

    func blockUser() async {
        guard let profile = profile else { return }
        let result = await userRepository.blockUser(userId: profile.id)
        if case .success = result {
            self.profile = UserProfile(
                id: profile.id,
                username: profile.username,
                displayName: profile.displayName,
                avatarUrl: profile.avatarUrl,
                bio: profile.bio,
                level: profile.level,
                levelName: profile.levelName,
                recipeCount: profile.recipeCount,
                logCount: profile.logCount,
                followerCount: profile.followerCount,
                followingCount: profile.followingCount,
                youtubeUrl: profile.youtubeUrl,
                instagramHandle: profile.instagramHandle,
                isFollowing: false,
                isFollowedBy: profile.isFollowedBy,
                isBlocked: true,
                createdAt: profile.createdAt
            )
        }
    }

    func reportUser(reason: ReportReason) async {
        guard let profile = profile else { return }
        let result = await userRepository.reportUser(userId: profile.id, reason: reason)
        #if DEBUG
        if case .success = result {
            print("[Profile] Reported user \(profile.id) for: \(reason.rawValue)")
        }
        #endif
    }

    private func loadMyProfile() async {
        let result = await userRepository.getMyProfile()
        switch result {
        case .success(let profile):
            myProfile = profile
            state = .loaded
            loadContent()
        case .failure(let error):
            state = .error(error.localizedDescription)
        }
    }

    private func loadUserProfile(_ id: String) async {
        let result = await userRepository.getUserProfile(id: id)
        switch result {
        case .success(let profile): self.profile = profile; state = .loaded; loadContent()
        case .failure(let error): state = .error(error.localizedDescription)
        }
    }

    private func loadRecipes(refresh: Bool) async {
        guard let id = userId ?? myProfile?.id else { return }
        isLoadingContent = true
        defer { isLoadingContent = false }
        let result = await userRepository.getUserRecipes(userId: id, cursor: refresh ? nil : recipesNextCursor)
        if case .success(let response) = result {
            recipes = refresh ? response.content : recipes + response.content
            recipesNextCursor = response.nextCursor
            hasMoreRecipes = response.hasMore
        }
    }

    private func loadLogs(refresh: Bool) async {
        guard let id = userId ?? myProfile?.id else { return }
        isLoadingContent = true
        defer { isLoadingContent = false }
        let result = await logRepository.getUserLogs(userId: id, cursor: refresh ? nil : logsNextCursor)
        if case .success(let response) = result {
            logs = refresh ? response.content : logs + response.content
            logsNextCursor = response.nextCursor
            hasMoreLogs = response.hasMore
        }
    }

    private func updateFollowState(_ isFollowing: Bool) {
        guard let p = profile else { return }
        profile = UserProfile(
            id: p.id,
            username: p.username,
            displayName: p.displayName,
            avatarUrl: p.avatarUrl,
            bio: p.bio,
            level: p.level,
            levelName: p.levelName,
            recipeCount: p.recipeCount,
            logCount: p.logCount,
            followerCount: p.followerCount + (isFollowing ? 1 : -1),
            followingCount: p.followingCount,
            youtubeUrl: p.youtubeUrl,
            instagramHandle: p.instagramHandle,
            isFollowing: isFollowing,
            isFollowedBy: p.isFollowedBy,
            isBlocked: p.isBlocked,
            createdAt: p.createdAt
        )
    }

    private func loadSavedContent(refresh: Bool) async {
        guard isOwnProfile else { return }
        isLoadingContent = true
        defer { isLoadingContent = false }

        #if DEBUG
        print("[Profile] Loading saved content, refresh=\(refresh)")
        #endif

        // Delegate to SavedItemsManager which is the single source of truth
        await SavedItemsManager.shared.fetchAllSavedContent()

        #if DEBUG
        print("[Profile] Saved content loaded via SavedItemsManager")
        print("[Profile]   - Recipes: \(SavedItemsManager.shared.savedRecipes.count)")
        print("[Profile]   - Logs: \(SavedItemsManager.shared.savedLogs.count)")
        #endif
    }

    func loadMoreContent() {
        Task {
            switch selectedTab {
            case .recipes: await loadRecipes(refresh: false)
            case .logs: await loadLogs(refresh: false)
            case .saved: await loadSavedContent(refresh: false)
            }
        }
    }

    /// Refresh saved content (called when saved tab appears)
    func refreshSavedContentIfNeeded() async {
        guard isOwnProfile, !isLoadingContent else { return }

        #if DEBUG
        print("[Profile] refreshSavedContentIfNeeded: forcing refresh")
        #endif

        // Always refresh to ensure we have the latest saved content
        await loadSavedContent(refresh: true)
    }

    /// Reset all state (called when user changes)
    func reset() {
        #if DEBUG
        print("[Profile] Resetting profile state")
        #endif
        state = .idle
        profile = nil
        myProfile = nil
        recipes = []
        logs = []
        savedRecipes = []
        savedLogs = []
        savedContentVersion = 0
        recipesNextCursor = nil
        logsNextCursor = nil
        savedRecipesNextCursor = nil
        savedLogsNextCursor = nil
        hasMoreRecipes = true
        hasMoreLogs = true
        hasMoreSavedRecipes = true
        hasMoreSavedLogs = true
        selectedTab = .recipes
    }
}
