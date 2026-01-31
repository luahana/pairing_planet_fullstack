package com.cookstemma.app.ui.screens.log

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.repository.LogRepository
import com.cookstemma.app.domain.model.CookingLogDetail
import com.cookstemma.app.domain.model.LinkedRecipeSummary
import com.cookstemma.app.domain.model.LogImage
import com.cookstemma.app.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditLogUiState(
    val logId: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val existingImages: List<LogImage> = emptyList(),
    val rating: Int = 0,
    val content: String = "",
    val hashtags: String = "",
    val isPrivate: Boolean = false,
    val linkedRecipe: LinkedRecipeSummary? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false
) {
    val canSave: Boolean
        get() = rating > 0 && !isSaving && !isDeleting

    val hasChanges: Boolean
        get() = true // Simplified - ideally compare with original values
}

@HiltViewModel
class EditLogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val logRepository: LogRepository
) : ViewModel() {

    private val logId: String = checkNotNull(savedStateHandle["logId"])

    private val _uiState = MutableStateFlow(EditLogUiState(logId = logId))
    val uiState: StateFlow<EditLogUiState> = _uiState.asStateFlow()

    private var originalLog: CookingLogDetail? = null

    init {
        loadLog()
    }

    private fun loadLog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            logRepository.getLog(logId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        originalLog = result.data
                        val log = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                existingImages = log.images,
                                rating = log.rating,
                                content = log.content ?: "",
                                hashtags = log.hashtags.joinToString(" ") { tag -> "#$tag" },
                                isPrivate = log.isPrivate,
                                linkedRecipe = log.recipe
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.exception.message)
                        }
                    }
                    is Result.Loading -> { }
                }
            }
        }
    }

    fun setRating(rating: Int) {
        _uiState.update { it.copy(rating = rating) }
    }

    fun setContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun setHashtags(hashtags: String) {
        _uiState.update { it.copy(hashtags = hashtags) }
    }

    fun setPrivate(isPrivate: Boolean) {
        _uiState.update { it.copy(isPrivate = isPrivate) }
    }

    fun save() {
        if (!_uiState.value.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val state = _uiState.value
            val hashtagList = state.hashtags
                .split(" ", ",", "#")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            logRepository.updateLog(
                logId = logId,
                rating = state.rating,
                content = state.content.ifBlank { null },
                hashtags = hashtagList.ifEmpty { null },
                isPrivate = state.isPrivate
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isSaving = false, error = result.exception.message)
                        }
                    }
                    is Result.Loading -> { }
                }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }

            logRepository.deleteLog(logId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isDeleting = false, error = result.exception.message)
                        }
                    }
                    is Result.Loading -> { }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
