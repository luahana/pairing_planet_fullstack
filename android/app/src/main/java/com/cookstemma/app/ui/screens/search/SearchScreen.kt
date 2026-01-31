package com.cookstemma.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cookstemma.app.R
import coil.compose.AsyncImage
import com.cookstemma.app.data.api.HomeRecipeItem
import com.cookstemma.app.data.api.RecentActivityItem
import com.cookstemma.app.ui.components.*
import com.cookstemma.app.ui.theme.BrandOrange
import com.cookstemma.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToLog: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToHashtag: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Determine what content to show
    when {
        uiState.showAllRecipes -> {
            SeeAllRecipesScreen(
                recipes = uiState.trendingRecipes,
                onBack = { viewModel.resetSeeAllState() },
                onRecipeClick = onNavigateToRecipe,
                onRefresh = { viewModel.loadHomeFeed() },
                isRefreshing = uiState.isLoadingHomeFeed
            )
        }
        uiState.showAllLogs -> {
            SeeAllLogsScreen(
                logs = uiState.recentLogs,
                onBack = { viewModel.resetSeeAllState() },
                onLogClick = onNavigateToLog,
                onRefresh = { viewModel.loadHomeFeed() },
                isRefreshing = uiState.isLoadingHomeFeed
            )
        }
        else -> {
            MainSearchContent(
                uiState = uiState,
                onQueryChange = viewModel::setQuery,
                onSearch = viewModel::submitSearch,
                onFocusChange = viewModel::setSearchFocused,
                onClearSearch = viewModel::clearSearch,
                onSelectTab = viewModel::selectTab,
                onRecipeClick = onNavigateToRecipe,
                onLogClick = onNavigateToLog,
                onUserClick = onNavigateToProfile,
                onHashtagClick = onNavigateToHashtag,
                onSeeAllRecipes = { viewModel.showAllRecipes() },
                onSeeAllLogs = { viewModel.showAllLogs() },
                onRecentSearchClick = { query ->
                    viewModel.setQuery(query)
                    viewModel.submitSearch()
                },
                onClearRecentSearch = viewModel::clearRecentSearch,
                onClearAllRecentSearches = viewModel::clearAllRecentSearches,
                onRefresh = { viewModel.loadHomeFeed() },
                onLoadMore = viewModel::loadMore
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainSearchContent(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onClearSearch: () -> Unit,
    onSelectTab: (SearchTab) -> Unit,
    onRecipeClick: (String) -> Unit,
    onLogClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onSeeAllRecipes: () -> Unit,
    onSeeAllLogs: () -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onClearRecentSearch: (String) -> Unit,
    onClearAllRecentSearches: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Search bar (no back button - this is a main tab)
        SearchBar(
            query = uiState.query,
            onQueryChange = onQueryChange,
            onSearch = {
                onSearch()
                keyboardController?.hide()
            },
            onFocusChange = onFocusChange,
            onClearSearch = onClearSearch,
            isSearchFocused = uiState.isSearchFocused
        )

        // Content
        when {
            uiState.results != null -> {
                // Show search results
                SearchResultsContent(
                    uiState = uiState,
                    onSelectTab = onSelectTab,
                    onRecipeClick = onRecipeClick,
                    onLogClick = onLogClick,
                    onUserClick = onUserClick,
                    onHashtagClick = onHashtagClick,
                    onLoadMore = onLoadMore
                )
            }
            uiState.isSearchFocused -> {
                // Show search history when focused
                SearchHistoryContent(
                    recentSearches = uiState.recentSearches,
                    onRecentSearchClick = onRecentSearchClick,
                    onClearRecentSearch = onClearRecentSearch,
                    onClearAllRecentSearches = onClearAllRecentSearches
                )
            }
            else -> {
                // Show home-style default view
                HomeStyleContent(
                    uiState = uiState,
                    onRecipeClick = onRecipeClick,
                    onLogClick = onLogClick,
                    onHashtagClick = onHashtagClick,
                    onSeeAllRecipes = onSeeAllRecipes,
                    onSeeAllLogs = onSeeAllLogs,
                    onRefresh = onRefresh
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onClearSearch: () -> Unit,
    isSearchFocused: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { onFocusChange(it.isFocused) },
            placeholder = { },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            leadingIcon = {
                Icon(
                    imageVector = AppIcons.search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = AppIcons.close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        // Cancel button when focused with empty query
        if (isSearchFocused && query.isEmpty()) {
            Spacer(modifier = Modifier.width(Spacing.sm))
            TextButton(onClick = { onFocusChange(false) }) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// MARK: - Home Style Content (Default View)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeStyleContent(
    uiState: SearchUiState,
    onRecipeClick: (String) -> Unit,
    onLogClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onSeeAllRecipes: () -> Unit,
    onSeeAllLogs: () -> Unit,
    onRefresh: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = uiState.isLoadingHomeFeed,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = Spacing.md)
        ) {
            // Trending Recipes Section
            item {
                TrendingRecipesSection(
                    recipes = uiState.trendingRecipes,
                    isLoading = uiState.isLoadingHomeFeed,
                    onRecipeClick = onRecipeClick,
                    onSeeAllClick = onSeeAllRecipes
                )
            }

            // Popular Hashtags Section
            item {
                Spacer(modifier = Modifier.height(Spacing.lg))
                PopularHashtagsSection(
                    hashtags = uiState.trendingHashtags,
                    onHashtagClick = onHashtagClick
                )
            }

            // Recent Logs Section
            item {
                Spacer(modifier = Modifier.height(Spacing.lg))
                RecentLogsSection(
                    logs = uiState.recentLogs,
                    isLoading = uiState.isLoadingHomeFeed,
                    onLogClick = onLogClick,
                    onSeeAllClick = onSeeAllLogs
                )
            }

            // Bottom padding for navigation bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun TrendingRecipesSection(
    recipes: List<HomeRecipeItem>,
    isLoading: Boolean,
    onRecipeClick: (String) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AppIcons.trending,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.trending_recipes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(onClick = onSeeAllClick) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.see_all))
                    Icon(
                        imageVector = AppIcons.forward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Content
        when {
            isLoading && recipes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            recipes.isEmpty() -> {
                EmptyStateCard(
                    icon = AppIcons.recipe,
                    message = stringResource(R.string.no_trending_recipes)
                )
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    contentPadding = PaddingValues(horizontal = Spacing.md)
                ) {
                    items(recipes, key = { it.id }) { recipe ->
                        HorizontalRecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PopularHashtagsSection(
    hashtags: List<com.cookstemma.app.data.repository.HashtagResult>,
    onHashtagClick: (String) -> Unit
) {
    Column {
        // Section header
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Tag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.popular_hashtags),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        if (hashtags.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.Tag,
                message = stringResource(R.string.no_trending_hashtags)
            )
        } else {
            // Use FlowRow for wrapping hashtag chips (like iOS)
            FlowRow(
                modifier = Modifier.padding(horizontal = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                hashtags.forEach { hashtag ->
                    Surface(
                        onClick = { onHashtagClick(hashtag.tag) },
                        shape = RoundedCornerShape(Spacing.xl),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "#${hashtag.tag}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                horizontal = Spacing.sm,
                                vertical = Spacing.xs
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentLogsSection(
    logs: List<RecentActivityItem>,
    isLoading: Boolean,
    onLogClick: (String) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AppIcons.log,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.recent_logs),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(onClick = onSeeAllClick) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.see_all))
                    Icon(
                        imageVector = AppIcons.forward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Content
        when {
            isLoading && logs.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            logs.isEmpty() -> {
                EmptyStateCard(
                    icon = AppIcons.log,
                    message = stringResource(R.string.no_recent_logs)
                )
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    contentPadding = PaddingValues(horizontal = Spacing.md)
                ) {
                    items(logs, key = { it.id }) { feedItem ->
                        HorizontalLogCard(
                            feedItem = feedItem,
                            onClick = { onLogClick(feedItem.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md)
            .height(120.dp)
            .clip(RoundedCornerShape(Spacing.md))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// MARK: - Horizontal Cards

@Composable
private fun HorizontalRecipeCard(
    recipe: HomeRecipeItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp),
        shape = RoundedCornerShape(Spacing.md),
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Column {
            // Thumbnail
            AsyncImage(
                model = recipe.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xs)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${recipe.userName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HorizontalLogCard(
    feedItem: RecentActivityItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp),
        shape = RoundedCornerShape(Spacing.md),
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Column {
            // Thumbnail
            AsyncImage(
                model = feedItem.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xs)
            ) {
                // Rating stars
                Row {
                    repeat(feedItem.rating) {
                        Icon(
                            imageVector = AppIcons.star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = BrandOrange
                        )
                    }
                }
                Text(
                    text = "@${feedItem.userName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = feedItem.recipeTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// MARK: - Search History Content

@Composable
private fun SearchHistoryContent(
    recentSearches: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onClearRecentSearch: (String) -> Unit,
    onClearAllRecentSearches: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.md)
    ) {
        if (recentSearches.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = AppIcons.history,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = stringResource(R.string.no_recent_searches),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            item {
                Surface(
                    shape = RoundedCornerShape(Spacing.md),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(Modifier.padding(Spacing.md)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.recent_searches),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = onClearAllRecentSearches) {
                                Text(stringResource(R.string.clear))
                            }
                        }

                        recentSearches.forEach { search ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRecentSearchClick(search) }
                                    .padding(vertical = Spacing.sm),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = AppIcons.history,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Text(search)
                                }
                                IconButton(
                                    onClick = { onClearRecentSearch(search) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = AppIcons.close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Search Results Content

@Composable
private fun SearchResultsContent(
    uiState: SearchUiState,
    onSelectTab: (SearchTab) -> Unit,
    onRecipeClick: (String) -> Unit,
    onLogClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Icon Tab bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            shape = RoundedCornerShape(Spacing.md),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xs)
            ) {
                SearchTab.entries.forEach { tab ->
                    SearchTabIconButton(
                        icon = tab.icon,
                        isSelected = uiState.selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Search results
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            SearchResults(
                uiState = uiState,
                onRecipeClick = onRecipeClick,
                onLogClick = onLogClick,
                onUserClick = onUserClick,
                onHashtagClick = onHashtagClick,
                onLoadMore = onLoadMore
            )
        }
    }
}

@Composable
private fun SearchResults(
    uiState: SearchUiState,
    onRecipeClick: (String) -> Unit,
    onLogClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        when (uiState.selectedTab) {
            SearchTab.ALL -> {
                uiState.results?.let { results ->
                    if (results.recipes.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.recipes),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(results.recipes.take(3)) { recipe ->
                            RecipeSearchItem(recipe, onClick = { onRecipeClick(recipe.id) })
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    if (results.logs.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.cooking_logs),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(results.logs.take(3)) { log ->
                            LogSearchItem(log, onClick = { onLogClick(log.id) })
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    if (results.users.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.users),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(results.users.take(5)) { user ->
                            UserSearchItem(user, onClick = { onUserClick(user.id) })
                        }
                    }
                }
            }
            SearchTab.RECIPES -> {
                items(uiState.recipes) { recipe ->
                    RecipeSearchItem(recipe, onClick = { onRecipeClick(recipe.id) })
                }
                if (uiState.hasMore) {
                    item {
                        LoadMoreButton(
                            isLoading = uiState.isLoadingMore,
                            onClick = onLoadMore
                        )
                    }
                }
            }
            SearchTab.LOGS -> {
                items(uiState.logs) { log ->
                    LogSearchItem(log, onClick = { onLogClick(log.id) })
                }
                if (uiState.hasMore) {
                    item {
                        LoadMoreButton(
                            isLoading = uiState.isLoadingMore,
                            onClick = onLoadMore
                        )
                    }
                }
            }
            SearchTab.USERS -> {
                items(uiState.users) { user ->
                    UserSearchItem(user, onClick = { onUserClick(user.id) })
                }
                if (uiState.hasMore) {
                    item {
                        LoadMoreButton(
                            isLoading = uiState.isLoadingMore,
                            onClick = onLoadMore
                        )
                    }
                }
            }
            SearchTab.HASHTAGS -> {
                uiState.results?.hashtags?.let { hashtags ->
                    items(hashtags) { hashtag ->
                        HashtagSearchItem(hashtag, onClick = { onHashtagClick(hashtag.tag) })
                    }
                }
            }
        }
    }
}

// MARK: - See All Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeeAllRecipesScreen(
    recipes: List<HomeRecipeItem>,
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Back header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = AppIcons.back,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.trending_recipes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            // Balance spacer
            Spacer(modifier = Modifier.width(48.dp))
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(recipes, key = { it.id }) { recipe ->
                    RecipeCardCompact(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeeAllLogsScreen(
    logs: List<RecentActivityItem>,
    onBack: () -> Unit,
    onLogClick: (String) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Back header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = AppIcons.back,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.recent_logs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            // Balance spacer
            Spacer(modifier = Modifier.width(48.dp))
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(logs, key = { it.id }) { feedItem ->
                    LogCardCompact(
                        feedItem = feedItem,
                        onClick = { onLogClick(feedItem.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun RecipeCardCompact(
    recipe: HomeRecipeItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            AsyncImage(
                model = recipe.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(Spacing.sm))
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "by @${recipe.userName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = AppIcons.log,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${recipe.logCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogCardCompact(
    feedItem: RecentActivityItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            AsyncImage(
                model = feedItem.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(Spacing.sm))
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                Text(
                    text = feedItem.recipeTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${feedItem.userName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    repeat(feedItem.rating) {
                        Icon(
                            imageVector = AppIcons.star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = BrandOrange
                        )
                    }
                }
            }
        }
    }
}

// MARK: - Search Result Items

@Composable
private fun RecipeSearchItem(
    recipe: com.cookstemma.app.domain.model.RecipeSummary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = recipe.coverImageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                recipe.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "by @${recipe.userName}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                stringResource(R.string.cooked_count, recipe.cookCount),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun LogSearchItem(
    log: com.cookstemma.app.domain.model.FeedItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = log.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "@${log.userName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            log.recipeTitle?.let { title ->
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandOrange,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(log.rating ?: 0) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserSearchItem(
    user: com.cookstemma.app.domain.model.UserSummary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                user.username ?: "",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            user.displayName?.let { name ->
                Text(
                    name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun HashtagSearchItem(
    hashtag: com.cookstemma.app.data.repository.HashtagResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Tag,
            contentDescription = null,
            tint = BrandOrange,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                "#${hashtag.tag}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.posts_count, hashtag.postCount),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun LoadMoreButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = AppIcons.forward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// MARK: - Search Tab Icon Button
@Composable
private fun SearchTabIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(Spacing.xxs))
        // Selection indicator dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CircleShape
                )
        )
    }
}

// Extension for SearchTab icons
val SearchTab.icon: androidx.compose.ui.graphics.vector.ImageVector
    get() = when (this) {
        SearchTab.ALL -> Icons.Default.GridView
        SearchTab.RECIPES -> AppIcons.recipe
        SearchTab.LOGS -> AppIcons.log
        SearchTab.USERS -> AppIcons.followers
        SearchTab.HASHTAGS -> Icons.Default.Tag
    }
