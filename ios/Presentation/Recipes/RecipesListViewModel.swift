import Foundation
import Combine

enum RecipesListState: Equatable {
    case idle, loading, loaded([RecipeSummary]), error(String), empty
}

@MainActor
final class RecipesListViewModel: ObservableObject {
    @Published private(set) var state: RecipesListState = .idle
    @Published private(set) var recipes: [RecipeSummary] = []
    @Published private(set) var isLoadingMore = false
    @Published private(set) var hasMore = true
    @Published var filters: RecipeFilters = RecipeFilters()
    @Published var searchQuery: String = ""
    @Published private(set) var savedRecipeIds: Set<String> = []

    var hasActiveFilters: Bool { !filters.isEmpty }

    private let recipeRepository: RecipeRepositoryProtocol
    private let savedContentRepository: SavedContentRepositoryProtocol
    private var nextCursor: String?
    private var loadTask: Task<Void, Never>?
    private var cancellables = Set<AnyCancellable>()
    private var hasFetchedSavedIds = false

    init(
        recipeRepository: RecipeRepositoryProtocol = RecipeRepository(),
        savedContentRepository: SavedContentRepositoryProtocol = SavedContentRepository()
    ) {
        self.recipeRepository = recipeRepository
        self.savedContentRepository = savedContentRepository
        setupSearchDebounce()
        setupSaveStateObserver()
    }

    private func setupSaveStateObserver() {
        NotificationCenter.default.publisher(for: .recipeSaveStateChanged)
            .receive(on: DispatchQueue.main)
            .sink { [weak self] notification in
                guard let recipeId = notification.userInfo?["recipeId"] as? String,
                      let isSaved = notification.userInfo?["isSaved"] as? Bool else { return }
                if isSaved {
                    self?.savedRecipeIds.insert(recipeId)
                } else {
                    self?.savedRecipeIds.remove(recipeId)
                }
            }
            .store(in: &cancellables)
    }

    func loadRecipes() {
        // Avoid cancelling if already loading
        guard state != .loading else { return }
        loadTask?.cancel()
        state = .loading
        loadTask = Task { await performLoad(isRefresh: true) }
    }

    func refresh() async { await performLoad(isRefresh: true) }

    func loadMore() {
        guard !isLoadingMore, hasMore, nextCursor != nil else { return }
        loadTask?.cancel()
        loadTask = Task { await performLoadMore() }
    }

    func saveRecipe(_ recipe: RecipeSummary) async {
        let wasSaved = savedRecipeIds.contains(recipe.id)

        // Optimistic update
        if wasSaved {
            savedRecipeIds.remove(recipe.id)
        } else {
            savedRecipeIds.insert(recipe.id)
        }

        let result = wasSaved
            ? await recipeRepository.unsaveRecipe(id: recipe.id)
            : await recipeRepository.saveRecipe(id: recipe.id)

        // Revert on failure or notify on success
        if case .failure = result {
            if wasSaved {
                savedRecipeIds.insert(recipe.id)
            } else {
                savedRecipeIds.remove(recipe.id)
            }
        } else {
            // Notify other views about save state change
            NotificationCenter.default.post(
                name: .recipeSaveStateChanged,
                object: nil,
                userInfo: ["recipeId": recipe.id, "isSaved": !wasSaved]
            )
        }
    }

    func isRecipeSaved(_ recipeId: String) -> Bool {
        savedRecipeIds.contains(recipeId)
    }

    private func setupSearchDebounce() {
        $searchQuery.debounce(for: .milliseconds(300), scheduler: DispatchQueue.main).removeDuplicates()
            .sink { [weak self] query in
                self?.filters.searchQuery = query.isEmpty ? nil : query
                self?.loadRecipes()
            }.store(in: &cancellables)
    }

    private func performLoad(isRefresh: Bool) async {
        guard !Task.isCancelled else { return }

        // Fetch recipes and saved IDs in parallel
        async let recipesTask = recipeRepository.getRecipes(cursor: nil, filters: filters)
        async let savedTask = fetchSavedRecipeIds()

        let result = await recipesTask
        await savedTask
        guard !Task.isCancelled else { return }

        switch result {
        case .success(let response):
            #if DEBUG
            print("[RecipesList] Success: \(response.content.count) recipes, hasNext: \(response.hasNext)")
            #endif
            recipes = response.content
            nextCursor = response.nextCursor
            hasMore = response.hasMore
            state = recipes.isEmpty ? .empty : .loaded(recipes)
        case .failure(let error):
            #if DEBUG
            print("[RecipesList] Error: \(error.localizedDescription)")
            #endif
            state = .error(error.localizedDescription)
        }
    }

    private func fetchSavedRecipeIds() async {
        // Only fetch once per session to avoid excessive API calls
        guard !hasFetchedSavedIds else { return }

        // Fetch all saved recipes to get their IDs
        var allSavedIds: Set<String> = []
        var cursor: String? = nil

        repeat {
            let result = await savedContentRepository.getSavedRecipes(cursor: cursor)
            guard case .success(let response) = result else { break }

            allSavedIds.formUnion(response.content.map { $0.id })
            cursor = response.hasMore ? response.nextCursor : nil
        } while cursor != nil

        savedRecipeIds = allSavedIds
        hasFetchedSavedIds = true

        #if DEBUG
        print("[RecipesList] Fetched \(allSavedIds.count) saved recipe IDs")
        #endif
    }

    private func performLoadMore() async {
        guard let cursor = nextCursor, !Task.isCancelled else { return }
        isLoadingMore = true
        defer { isLoadingMore = false }

        let result = await recipeRepository.getRecipes(cursor: cursor, filters: filters)
        guard !Task.isCancelled else { return }

        if case .success(let response) = result {
            recipes.append(contentsOf: response.content)
            nextCursor = response.nextCursor
            hasMore = response.hasMore
            state = .loaded(recipes)
        }
    }
}
