import SwiftUI

struct RecipesListView: View {
    @StateObject private var viewModel = RecipesListViewModel()
    @EnvironmentObject private var appState: AppState
    @State private var showFilters = false
    @State private var navigationPath = NavigationPath()
    @State private var scrollProxy: ScrollViewProxy?

    var body: some View {
        NavigationStack(path: $navigationPath) {
            VStack(spacing: 0) {
                recipesHeader
                contentView
            }
            .frame(maxHeight: .infinity, alignment: .top)
            .background(DesignSystem.Colors.background)
            .navigationBarHidden(true)
            .navigationDestination(for: String.self) { recipeId in
                RecipeDetailView(recipeId: recipeId)
            }
            .sheet(isPresented: $showFilters) {
                RecipeFiltersView(filters: $viewModel.filters) { viewModel.loadRecipes() }
            }
        }
        .onAppear { if case .idle = viewModel.state { viewModel.loadRecipes() } }
        .onChange(of: appState.recipesScrollToTopTrigger) { _, _ in
            if !navigationPath.isEmpty {
                navigationPath = NavigationPath()
                return
            }
            // Scroll to top with animation
            withAnimation(.easeInOut(duration: 0.3)) {
                scrollProxy?.scrollTo("recipes-top", anchor: .top)
            }
            Task { await viewModel.refresh() }
        }
    }

    private var recipesHeader: some View {
        HStack {
            Image(systemName: AppIcon.recipe)
                .font(.system(size: DesignSystem.IconSize.lg))
                .foregroundColor(DesignSystem.Colors.primary)

            Text("Recipes")
                .font(DesignSystem.Typography.headline)
                .foregroundColor(DesignSystem.Colors.text)

            Spacer()

            Button { showFilters = true } label: {
                Image(systemName: AppIcon.filter)
                    .font(.system(size: DesignSystem.IconSize.md))
                    .foregroundColor(viewModel.hasActiveFilters ? DesignSystem.Colors.primary : DesignSystem.Colors.text)
            }
        }
        .padding(.horizontal, DesignSystem.Spacing.md)
        .padding(.vertical, DesignSystem.Spacing.sm)
    }

    @ViewBuilder
    private var contentView: some View {
        switch viewModel.state {
        case .idle, .loading:
            Spacer()
            ProgressView()
            Spacer()
        case .loaded:
            ScrollViewReader { proxy in
                ScrollView(.vertical, showsIndicators: false) {
                    Color.clear.frame(height: 0).id("recipes-top")
                    recipeContent
                }
                .refreshable {
                    await viewModel.refresh()
                }
                .onAppear { scrollProxy = proxy }
            }
        case .empty:
            Spacer()
            IconEmptyState(icon: AppIcon.recipe)
            Spacer()
        case .error(let msg):
            Spacer()
            ErrorStateView(message: msg) { viewModel.loadRecipes() }
            Spacer()
        }
    }

    private var recipeContent: some View {
        LazyVStack(spacing: DesignSystem.Spacing.md) {
            ForEach(viewModel.recipes) { recipe in
                NavigationLink(value: recipe.id) {
                    RecipeCard(
                        recipe: recipe,
                        isSaved: viewModel.isRecipeSaved(recipe.id),
                        onSave: { await viewModel.saveRecipe(recipe) }
                    )
                }
                .buttonStyle(.plain)
                .onAppear {
                    if recipe.id == viewModel.recipes.suffix(3).first?.id {
                        viewModel.loadMore()
                    }
                }
            }

            if viewModel.isLoadingMore {
                ProgressView()
                    .padding(.vertical, DesignSystem.Spacing.md)
            }
        }
        .padding(.bottom, DesignSystem.Spacing.sm)
    }
}

// MARK: - Recipe Card (Icon-Focused)
struct RecipeCard: View {
    let recipe: RecipeSummary
    let isSaved: Bool
    let onSave: () async -> Void
    @State private var isSaving = false

    var body: some View {
        VStack(alignment: .leading, spacing: DesignSystem.Spacing.sm) {
            // Cover image
            GeometryReader { geometry in
                AsyncImage(url: URL(string: recipe.coverImageUrl ?? "")) { img in
                    img.resizable().scaledToFill()
                } placeholder: {
                    Rectangle().fill(DesignSystem.Colors.tertiaryBackground)
                }
                .frame(width: geometry.size.width, height: 180)
                .clipped()
            }
            .frame(height: 180)
            .cornerRadius(DesignSystem.CornerRadius.md)

            // Title
            Text(recipe.title)
                .font(DesignSystem.Typography.headline)
                .foregroundColor(DesignSystem.Colors.text)
                .lineLimit(2)

            // Stats row (Icons with minimal text)
            HStack(spacing: DesignSystem.Spacing.md) {
                // Cooking time
                if let time = recipe.cookingTimeRange {
                    HStack(spacing: DesignSystem.Spacing.xxs) {
                        Image(systemName: AppIcon.timer)
                        Text(time.cookingTimeDisplayText)
                    }
                    .font(DesignSystem.Typography.caption)
                    .foregroundColor(DesignSystem.Colors.secondaryText)
                }

                // Cooking style badge
                if let style = recipe.cookingStyle, !style.isEmpty {
                    HStack(spacing: 2) {
                        Text(style.flagEmoji)
                        Text(style.cookingStyleName)
                    }
                    .font(DesignSystem.Typography.caption)
                    .foregroundColor(DesignSystem.Colors.secondaryText)
                }

                // Cook count
                HStack(spacing: DesignSystem.Spacing.xxs) {
                    Image(systemName: AppIcon.chef)
                    Text("\(recipe.cookCount.abbreviated)")
                }
                .font(DesignSystem.Typography.caption)
                .foregroundColor(DesignSystem.Colors.secondaryText)

                Spacer()

                // Save button (icon only)
                Button {
                    Task {
                        isSaving = true
                        await onSave()
                        isSaving = false
                    }
                } label: {
                    Image(systemName: isSaved ? AppIcon.save : AppIcon.saveOutline)
                        .font(.system(size: DesignSystem.IconSize.md))
                        .foregroundColor(isSaved ? DesignSystem.Colors.bookmark : DesignSystem.Colors.tertiaryText)
                }
                .disabled(isSaving)
            }
        }
        .padding(DesignSystem.Spacing.md)
        .background(DesignSystem.Colors.background)
        .contentShape(Rectangle())
    }
}

// MARK: - Recipe Filters View (Icon Headers)
struct RecipeFiltersView: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var filters: RecipeFilters
    let onApply: () -> Void

    var body: some View {
        NavigationStack {
            List {
                // Cooking Time Section
                Section {
                    ForEach(CookingTimeRange.allCases, id: \.self) { range in
                        Button {
                            filters.cookingTimeRange = filters.cookingTimeRange == range ? nil : range
                        } label: {
                            HStack {
                                Text(range.displayText)
                                Spacer()
                                if filters.cookingTimeRange == range {
                                    Image(systemName: AppIcon.checkmark)
                                        .foregroundColor(DesignSystem.Colors.primary)
                                }
                            }
                        }
                        .foregroundColor(DesignSystem.Colors.text)
                    }
                } header: {
                    HStack(spacing: DesignSystem.Spacing.xs) {
                        Image(systemName: AppIcon.timer)
                            .foregroundColor(DesignSystem.Colors.primary)
                        Text("Cooking Time")
                            .foregroundColor(DesignSystem.Colors.secondaryText)
                    }
                }

                // Servings Section
                Section {
                    ForEach(ServingsOption.allCases, id: \.self) { option in
                        Button {
                            if filters.minServings == option.minServings {
                                filters.minServings = nil
                                filters.maxServings = nil
                            } else {
                                filters.minServings = option.minServings
                                filters.maxServings = option.maxServings
                            }
                        } label: {
                            HStack {
                                Text(option.displayText)
                                Spacer()
                                if filters.minServings == option.minServings {
                                    Image(systemName: AppIcon.checkmark)
                                        .foregroundColor(DesignSystem.Colors.primary)
                                }
                            }
                        }
                        .foregroundColor(DesignSystem.Colors.text)
                    }
                } header: {
                    HStack(spacing: DesignSystem.Spacing.xs) {
                        Image(systemName: AppIcon.servings)
                            .foregroundColor(DesignSystem.Colors.primary)
                        Text("Servings")
                            .foregroundColor(DesignSystem.Colors.secondaryText)
                    }
                }

                // Cooking Style Section (searchable dropdown with all countries)
                Section {
                    CookingStylePicker(selection: $filters.cookingStyle)
                } header: {
                    HStack(spacing: DesignSystem.Spacing.xs) {
                        Image(systemName: AppIcon.chef)
                            .foregroundColor(DesignSystem.Colors.primary)
                        Text("Cooking Style")
                            .foregroundColor(DesignSystem.Colors.secondaryText)
                    }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        filters = RecipeFilters()
                    } label: {
                        Image(systemName: AppIcon.reset)
                            .foregroundColor(DesignSystem.Colors.text)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        onApply()
                        dismiss()
                    } label: {
                        Image(systemName: AppIcon.checkmark)
                            .foregroundColor(DesignSystem.Colors.primary)
                    }
                }
            }
        }
    }
}

// MARK: - Cooking Style Picker (Searchable Dropdown)
struct CookingStylePicker: View {
    @Binding var selection: String?
    @State private var searchText = ""

    private var filteredStyles: [CookingStyleOption] {
        if searchText.isEmpty {
            return CookingStyleOption.allCases
        }
        return CookingStyleOption.allCases.filter {
            $0.displayText.localizedCaseInsensitiveContains(searchText)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Search field
            HStack {
                Image(systemName: AppIcon.search)
                    .foregroundColor(DesignSystem.Colors.tertiaryText)
                TextField("Search countries...", text: $searchText)
                    .textFieldStyle(.plain)
                if !searchText.isEmpty {
                    Button { searchText = "" } label: {
                        Image(systemName: AppIcon.close)
                            .foregroundColor(DesignSystem.Colors.tertiaryText)
                    }
                }
            }
            .padding(DesignSystem.Spacing.sm)
            .background(DesignSystem.Colors.tertiaryBackground)
            .cornerRadius(DesignSystem.CornerRadius.sm)

            // Selected style indicator
            if let selected = selection, let style = CookingStyleOption(rawValue: selected) {
                HStack {
                    Text("Selected:")
                        .font(DesignSystem.Typography.caption)
                        .foregroundColor(DesignSystem.Colors.secondaryText)
                    Text("\(style.flag) \(style.displayText)")
                        .font(DesignSystem.Typography.caption)
                        .foregroundColor(DesignSystem.Colors.primary)
                    Spacer()
                    Button { selection = nil } label: {
                        Image(systemName: AppIcon.close)
                            .font(.system(size: DesignSystem.IconSize.sm))
                            .foregroundColor(DesignSystem.Colors.tertiaryText)
                    }
                }
                .padding(.top, DesignSystem.Spacing.xs)
            }

            // Country list
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(filteredStyles) { style in
                        Button {
                            selection = selection == style.rawValue ? nil : style.rawValue
                        } label: {
                            HStack {
                                Text(style.flag)
                                    .font(.system(size: 20))
                                Text(style.displayText)
                                    .font(DesignSystem.Typography.body)
                                Spacer()
                                if selection == style.rawValue {
                                    Image(systemName: AppIcon.checkmark)
                                        .foregroundColor(DesignSystem.Colors.primary)
                                }
                            }
                            .padding(.vertical, DesignSystem.Spacing.sm)
                            .contentShape(Rectangle())
                        }
                        .foregroundColor(DesignSystem.Colors.text)

                        if style != filteredStyles.last {
                            Divider()
                        }
                    }
                }
            }
            .frame(maxHeight: 250)
        }
    }
}

#Preview { RecipesListView() }
