package com.cookstemma.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.data.api.HomeRecipeItem
import com.cookstemma.app.data.api.RecentActivityItem
import com.cookstemma.app.data.local.SearchHistoryDataStore
import com.cookstemma.app.data.repository.*
import com.cookstemma.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchTab {
    ALL, RECIPES, LOGS, USERS, HASHTAGS
}

data class SearchUiState(
    val query: String = "",
    val selectedTab: SearchTab = SearchTab.ALL,
    val recentSearches: List<String> = emptyList(),
    val trendingHashtags: List<HashtagResult> = emptyList(),
    // Home feed data for default view
    val trendingRecipes: List<HomeRecipeItem> = emptyList(),
    val recentLogs: List<RecentActivityItem> = emptyList(),
    val isLoadingHomeFeed: Boolean = false,
    // "See All" view states
    val showAllRecipes: Boolean = false,
    val showAllLogs: Boolean = false,
    // Search focus state
    val isSearchFocused: Boolean = false,
    // Search results
    val results: SearchResults? = null,
    val recipes: List<RecipeSummary> = emptyList(),
    val logs: List<FeedItem> = emptyList(),
    val users: List<UserSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val cursor: String? = null,
    val hasMore: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val searchHistoryDataStore: SearchHistoryDataStore,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadTrendingHashtags()
        loadRecentSearches()
        loadHomeFeed()
    }

    fun loadHomeFeed() {
        if (_uiState.value.isLoadingHomeFeed) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHomeFeed = true) }
            try {
                val response = apiService.getHome()
                _uiState.update { state ->
                    state.copy(
                        trendingRecipes = response.recentRecipes ?: emptyList(),
                        recentLogs = response.recentActivity ?: emptyList(),
                        isLoadingHomeFeed = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingHomeFeed = false) }
            }
        }
    }

    fun setSearchFocused(focused: Boolean) {
        _uiState.update { it.copy(isSearchFocused = focused) }
    }

    fun showAllRecipes() {
        _uiState.update { it.copy(showAllRecipes = true, showAllLogs = false) }
    }

    fun showAllLogs() {
        _uiState.update { it.copy(showAllLogs = true, showAllRecipes = false) }
    }

    fun resetSeeAllState() {
        _uiState.update { it.copy(showAllRecipes = false, showAllLogs = false) }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                query = "",
                results = null,
                recipes = emptyList(),
                logs = emptyList(),
                users = emptyList(),
                isSearchFocused = false
            )
        }
    }

    private fun loadTrendingHashtags() {
        viewModelScope.launch {
            searchRepository.getTrendingHashtags().collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(trendingHashtags = result.data) }
                }
            }
        }
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            searchHistoryDataStore.searchHistory.collect { history ->
                _uiState.update { it.copy(recentSearches = history) }
            }
        }
    }

    fun setQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        // Clear results when query is cleared
        if (query.isEmpty()) {
            _uiState.update {
                it.copy(
                    results = null,
                    recipes = emptyList(),
                    logs = emptyList(),
                    users = emptyList()
                )
            }
        }
    }

    fun selectTab(tab: SearchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        val query = _uiState.value.query
        if (query.length >= 2) {
            performSearch(query)
        }
    }

    fun submitSearch() {
        val query = _uiState.value.query.trim()
        if (query.isNotEmpty()) {
            addToRecentSearches(query)
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (_uiState.value.selectedTab) {
                SearchTab.ALL -> {
                    searchRepository.search(query).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update {
                                    it.copy(
                                        results = result.data,
                                        isLoading = false
                                    )
                                }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(isLoading = false) }
                            }
                            is Result.Loading -> {}
                        }
                    }
                }
                SearchTab.RECIPES -> {
                    searchRepository.searchRecipes(query).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update {
                                    it.copy(
                                        recipes = result.data.content,
                                        cursor = result.data.nextCursor,
                                        hasMore = result.data.hasMore,
                                        isLoading = false
                                    )
                                }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(isLoading = false) }
                            }
                            is Result.Loading -> {}
                        }
                    }
                }
                SearchTab.LOGS -> {
                    searchRepository.searchLogs(query).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update {
                                    it.copy(
                                        logs = result.data.content,
                                        cursor = result.data.nextCursor,
                                        hasMore = result.data.hasMore,
                                        isLoading = false
                                    )
                                }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(isLoading = false) }
                            }
                            is Result.Loading -> {}
                        }
                    }
                }
                SearchTab.USERS -> {
                    searchRepository.searchUsers(query).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update {
                                    it.copy(
                                        users = result.data.content,
                                        cursor = result.data.nextCursor,
                                        hasMore = result.data.hasMore,
                                        isLoading = false
                                    )
                                }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(isLoading = false) }
                            }
                            is Result.Loading -> {}
                        }
                    }
                }
                SearchTab.HASHTAGS -> {
                    // Hashtags handled in ALL tab
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun loadMore() {
        val cursor = _uiState.value.cursor ?: return
        if (_uiState.value.isLoadingMore) return

        val query = _uiState.value.query

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            when (_uiState.value.selectedTab) {
                SearchTab.RECIPES -> {
                    searchRepository.searchRecipes(query, cursor).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update {
                                    it.copy(
                                        recipes = it.recipes + result.data.content,
                                        cursor = result.data.nextCursor,
                                        hasMore = result.data.hasMore,
                                        isLoadingMore = false
                                    )
                                }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(isLoadingMore = false) }
                            }
                            is Result.Loading -> {}
                        }
                    }
                }
                SearchTab.LOGS -> {
                    searchRepository.searchLogs(query, cursor).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update {
                                    it.copy(
                                        logs = it.logs + result.data.content,
                                        cursor = result.data.nextCursor,
                                        hasMore = result.data.hasMore,
                                        isLoadingMore = false
                                    )
                                }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(isLoadingMore = false) }
                            }
                            is Result.Loading -> {}
                        }
                    }
                }
                SearchTab.USERS -> {
                    searchRepository.searchUsers(query, cursor).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _uiState.update {
                                    it.copy(
                                        users = it.users + result.data.content,
                                        cursor = result.data.nextCursor,
                                        hasMore = result.data.hasMore,
                                        isLoadingMore = false
                                    )
                                }
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(isLoadingMore = false) }
                            }
                            is Result.Loading -> {}
                        }
                    }
                }
                else -> {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            }
        }
    }

    fun clearRecentSearch(search: String) {
        viewModelScope.launch {
            searchHistoryDataStore.removeSearch(search)
        }
    }

    fun clearAllRecentSearches() {
        viewModelScope.launch {
            searchHistoryDataStore.clearAllSearches()
        }
    }

    private fun addToRecentSearches(query: String) {
        viewModelScope.launch {
            searchHistoryDataStore.addSearch(query)
        }
    }
}
