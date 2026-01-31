package com.cookstemma.app.ui.screens.logdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.repository.LogRepository
import com.cookstemma.app.data.repository.SavedItemsManager
import com.cookstemma.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogDetailUiState(
    val log: CookingLogDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val logRepository: LogRepository,
    private val savedItemsManager: SavedItemsManager
) : ViewModel() {

    private val logId: String = checkNotNull(savedStateHandle["logId"])

    private val _uiState = MutableStateFlow(LogDetailUiState())
    val uiState: StateFlow<LogDetailUiState> = _uiState.asStateFlow()

    init {
        loadLog()
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

    fun refreshCommentCount(newCount: Int) {
        _uiState.value.log?.let { log ->
            _uiState.update {
                it.copy(log = log.copy(commentCount = newCount))
            }
        }
    }
}
