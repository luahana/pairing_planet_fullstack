package com.cookstemma.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.repository.FeedRepository
import com.cookstemma.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeFeedUiState(
    val items: List<FeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true
)

@HiltViewModel
class HomeFeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeFeedUiState())
    val uiState: StateFlow<HomeFeedUiState> = _uiState.asStateFlow()

    private var nextCursor: String? = null

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            feedRepository.getFeed(null).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        nextCursor = result.data.nextCursor
                        _uiState.update {
                            it.copy(
                                items = result.data.content,
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
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            feedRepository.getFeed(null).collect { result ->
                when (result) {
                    is Result.Loading -> { /* Already showing refresh indicator */ }
                    is Result.Success -> {
                        nextCursor = result.data.nextCursor
                        _uiState.update {
                            it.copy(
                                items = result.data.content,
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
            feedRepository.getFeed(nextCursor).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoadingMore = true) }
                    is Result.Success -> {
                        nextCursor = result.data.nextCursor
                        _uiState.update {
                            it.copy(
                                items = it.items + result.data.content,
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

    fun likeLog(logId: String) {
        viewModelScope.launch {
            // Like functionality - API call only, no local state tracking for now
            feedRepository.likeLog(logId)
        }
    }

    fun saveLog(logId: String) {
        viewModelScope.launch {
            // Save functionality - API call only
            feedRepository.saveLog(logId)
        }
    }
}
