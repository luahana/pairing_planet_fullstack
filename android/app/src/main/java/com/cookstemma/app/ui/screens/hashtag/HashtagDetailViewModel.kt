package com.cookstemma.app.ui.screens.hashtag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.repository.SearchRepository
import com.cookstemma.app.domain.model.FeedItem
import com.cookstemma.app.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HashtagContentFilter {
    ALL,
    RECIPES,
    LOGS
}

data class HashtagDetailUiState(
    val hashtag: String = "",
    val posts: List<FeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val selectedFilter: HashtagContentFilter = HashtagContentFilter.ALL
) {
    val filteredPosts: List<FeedItem>
        get() = when (selectedFilter) {
            HashtagContentFilter.ALL -> posts
            HashtagContentFilter.RECIPES -> posts.filter { it.isRecipe }
            HashtagContentFilter.LOGS -> posts.filter { it.isLog }
        }
}

@HiltViewModel
class HashtagDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val hashtag: String = savedStateHandle.get<String>("tag") ?: ""

    private val _uiState = MutableStateFlow(HashtagDetailUiState(hashtag = hashtag))
    val uiState: StateFlow<HashtagDetailUiState> = _uiState.asStateFlow()

    private var nextCursor: String? = null

    init {
        loadPosts()
    }

    fun loadPosts() {
        nextCursor = null
        viewModelScope.launch {
            searchRepository.getHashtagPosts(hashtag).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { 
                        it.copy(isLoading = true, error = null) 
                    }
                    is Result.Success -> {
                        nextCursor = result.data.nextCursor
                        _uiState.update {
                            it.copy(
                                posts = result.data.content,
                                isLoading = false,
                                hasMore = result.data.hasMore
                            )
                        }
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.exception.message)
                    }
                }
            }
        }
    }

    fun refresh() {
        nextCursor = null
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            searchRepository.getHashtagPosts(hashtag).collect { result ->
                when (result) {
                    is Result.Loading -> { /* Already showing refresh indicator */ }
                    is Result.Success -> {
                        nextCursor = result.data.nextCursor
                        _uiState.update {
                            it.copy(
                                posts = result.data.content,
                                isRefreshing = false,
                                hasMore = result.data.hasMore
                            )
                        }
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isRefreshing = false, error = result.exception.message)
                    }
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore || nextCursor == null) return
        
        viewModelScope.launch {
            searchRepository.getHashtagPosts(hashtag, nextCursor).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoadingMore = true) }
                    is Result.Success -> {
                        nextCursor = result.data.nextCursor
                        _uiState.update {
                            it.copy(
                                posts = it.posts + result.data.content,
                                isLoadingMore = false,
                                hasMore = result.data.hasMore
                            )
                        }
                    }
                    is Result.Error -> _uiState.update { it.copy(isLoadingMore = false) }
                }
            }
        }
    }

    fun selectFilter(filter: HashtagContentFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
