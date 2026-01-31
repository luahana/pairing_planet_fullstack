package com.cookstemma.app.ui.screens.logdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.repository.CommentRepository
import com.cookstemma.app.data.repository.LogRepository
import com.cookstemma.app.data.repository.SavedItemsManager
import com.cookstemma.app.data.repository.Comment
import com.cookstemma.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogDetailUiState(
    val log: CookingLogDetail? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingComments: Boolean = false,
    val error: String? = null,
    val commentText: String = "",
    val replyingTo: Comment? = null,
    val isSubmittingComment: Boolean = false,
    val commentsCursor: String? = null,
    val hasMoreComments: Boolean = false
)

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val logRepository: LogRepository,
    private val commentRepository: CommentRepository,
    private val savedItemsManager: SavedItemsManager
) : ViewModel() {

    private val logId: String = checkNotNull(savedStateHandle["logId"])

    private val _uiState = MutableStateFlow(LogDetailUiState())
    val uiState: StateFlow<LogDetailUiState> = _uiState.asStateFlow()

    init {
        loadLog()
        loadComments()
        observeSavedState()
    }

    private fun observeSavedState() {
        viewModelScope.launch {
            savedItemsManager.savedLogIds.collect { savedIds ->
                _uiState.value.log?.let { log ->
                    val isSaved = savedIds.contains(log.id)
                    if (log.isSaved != isSaved) {
                        _uiState.update { it.copy(log = log.copy(isSaved = isSaved)) }
                    }
                }
            }
        }
    }

    private fun loadLog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            logRepository.getLog(logId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(log = result.data.withSavedState(), isLoading = false)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message
                            )
                        }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private fun loadComments(cursor: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingComments = true) }
            commentRepository.getComments(logId, cursor).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                comments = if (cursor == null) {
                                    result.data.content
                                } else {
                                    it.comments + result.data.content
                                },
                                commentsCursor = result.data.nextCursor,
                                hasMoreComments = result.data.hasMore,
                                isLoadingComments = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoadingComments = false) }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun loadMoreComments() {
        val cursor = _uiState.value.commentsCursor
        if (cursor != null && !_uiState.value.isLoadingComments) {
            loadComments(cursor)
        }
    }

    fun toggleLike() {
        val log = _uiState.value.log ?: return

        // Optimistic update
        val newIsLiked = !log.isLiked
        val newLikeCount = if (newIsLiked) log.likeCount + 1 else log.likeCount - 1
        _uiState.update {
            it.copy(log = log.copy(isLiked = newIsLiked, likeCount = newLikeCount))
        }

        viewModelScope.launch {
            val result = if (newIsLiked) {
                logRepository.likeLog(log.id)
            } else {
                logRepository.unlikeLog(log.id)
            }

            result.collect { apiResult ->
                if (apiResult is Result.Error) {
                    // Revert on failure
                    _uiState.update {
                        it.copy(log = log)
                    }
                }
            }
        }
    }

    fun toggleSave() {
        val log = _uiState.value.log ?: return
        savedItemsManager.toggleSaveLog(log.id)
    }

    fun setCommentText(text: String) {
        _uiState.update { it.copy(commentText = text) }
    }

    fun setReplyingTo(comment: Comment?) {
        _uiState.update { it.copy(replyingTo = comment) }
    }

    fun submitComment() {
        val text = _uiState.value.commentText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingComment = true) }

            val parentId = _uiState.value.replyingTo?.id

            commentRepository.createComment(logId, text, parentId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                comments = listOf(result.data) + it.comments,
                                commentText = "",
                                replyingTo = null,
                                isSubmittingComment = false
                            )
                        }
                        // Update comment count in log
                        _uiState.value.log?.let { log ->
                            _uiState.update {
                                it.copy(log = log.copy(commentCount = log.commentCount + 1))
                            }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isSubmittingComment = false) }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun toggleCommentLike(comment: Comment) {
        val newIsLiked = !comment.isLiked
        val newLikeCount = if (newIsLiked) comment.likeCount + 1 else comment.likeCount - 1

        // Optimistic update
        _uiState.update { state ->
            state.copy(
                comments = state.comments.map {
                    if (it.id == comment.id) {
                        it.copy(isLiked = newIsLiked, likeCount = newLikeCount)
                    } else {
                        it
                    }
                }
            )
        }

        viewModelScope.launch {
            val result = if (newIsLiked) {
                commentRepository.likeComment(comment.id)
            } else {
                commentRepository.unlikeComment(comment.id)
            }

            result.collect { apiResult ->
                if (apiResult is Result.Error) {
                    // Revert on failure
                    _uiState.update { state ->
                        state.copy(
                            comments = state.comments.map {
                                if (it.id == comment.id) comment else it
                            }
                        )
                    }
                }
            }
        }
    }
}
