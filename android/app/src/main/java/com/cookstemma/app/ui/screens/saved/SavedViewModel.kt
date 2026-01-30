package com.cookstemma.app.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.repository.SavedRepository
import com.cookstemma.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedUiState(
    val savedRecipes: List<RecipeSummary> = emptyList(),
    val savedLogs: List<CookingLog> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMoreRecipes: Boolean = false,
    val isLoadingMoreLogs: Boolean = false,
    val recipesCursor: String? = null,
    val logsCursor: String? = null,
    val hasMoreRecipes: Boolean = false,
    val hasMoreLogs: Boolean = false
)

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val savedRepository: SavedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    init {
        loadSavedRecipes()
        loadSavedLogs()
    }

    private fun loadSavedRecipes(cursor: String? = null) {
        viewModelScope.launch {
            if (cursor == null) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isLoadingMoreRecipes = true) }
            }

            savedRepository.getSavedRecipes(cursor).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                savedRecipes = if (cursor == null) {
                                    result.data.content
                                } else {
                                    it.savedRecipes + result.data.content
                                },
                                recipesCursor = result.data.nextCursor,
                                hasMoreRecipes = result.data.hasMore,
                                isLoading = false,
                                isLoadingMoreRecipes = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, isLoadingMoreRecipes = false)
                        }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private fun loadSavedLogs(cursor: String? = null) {
        viewModelScope.launch {
            if (cursor == null && _uiState.value.savedRecipes.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isLoadingMoreLogs = true) }
            }

            savedRepository.getSavedLogs(cursor).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                savedLogs = if (cursor == null) {
                                    result.data.content
                                } else {
                                    it.savedLogs + result.data.content
                                },
                                logsCursor = result.data.nextCursor,
                                hasMoreLogs = result.data.hasMore,
                                isLoading = false,
                                isLoadingMoreLogs = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, isLoadingMoreLogs = false)
                        }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun loadMoreRecipes() {
        val cursor = _uiState.value.recipesCursor
        if (cursor != null && !_uiState.value.isLoadingMoreRecipes) {
            loadSavedRecipes(cursor)
        }
    }

    fun loadMoreLogs() {
        val cursor = _uiState.value.logsCursor
        if (cursor != null && !_uiState.value.isLoadingMoreLogs) {
            loadSavedLogs(cursor)
        }
    }

    fun refresh() {
        loadSavedRecipes()
        loadSavedLogs()
    }
}
