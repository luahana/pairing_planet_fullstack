package com.cookstemma.app.ui.screens.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.repository.RecipeRepository
import com.cookstemma.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipesListUiState(
    val recipes: List<RecipeSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val filters: RecipeFilters = RecipeFilters()
) {
    val hasActiveFilters: Boolean
        get() = filters.cookingTimeRange != null ||
                filters.category != null ||
                filters.servingsRange != null ||
                filters.sortBy != RecipeSortBy.TRENDING
}

@HiltViewModel
class RecipesListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipesListUiState())
    val uiState: StateFlow<RecipesListUiState> = _uiState.asStateFlow()

    private var nextCursor: String? = null

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            recipeRepository.getRecipes(null, _uiState.value.filters).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        nextCursor = result.data.nextCursor
                        _uiState.update {
                            it.copy(recipes = result.data.content, isLoading = false, hasMore = result.data.hasMore)
                        }
                    }
                    is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore || nextCursor == null) return
        viewModelScope.launch {
            recipeRepository.getRecipes(nextCursor, _uiState.value.filters).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoadingMore = true) }
                    is Result.Success -> {
                        nextCursor = result.data.nextCursor
                        _uiState.update {
                            it.copy(recipes = it.recipes + result.data.content, isLoadingMore = false, hasMore = result.data.hasMore)
                        }
                    }
                    is Result.Error -> _uiState.update { it.copy(isLoadingMore = false) }
                }
            }
        }
    }

    fun updateFilters(filters: RecipeFilters) {
        _uiState.update { it.copy(filters = filters) }
        nextCursor = null
        loadRecipes()
    }

    fun updateSearchQuery(query: String?) {
        val currentFilters = _uiState.value.filters
        if (currentFilters.searchQuery != query) {
            _uiState.update { it.copy(filters = currentFilters.copy(searchQuery = query)) }
            nextCursor = null
            loadRecipes()
        }
    }

    fun saveRecipe(recipe: RecipeSummary) {
        viewModelScope.launch {
            val wasSaved = recipe.isSaved
            // Optimistic update
            updateRecipeInList(recipe.id) { it.copy(savedStatus = !wasSaved) }

            val result = if (wasSaved) {
                recipeRepository.unsaveRecipe(recipe.id)
            } else {
                recipeRepository.saveRecipe(recipe.id)
            }

            result.collect { res ->
                if (res is Result.Error) {
                    // Revert on failure
                    updateRecipeInList(recipe.id) { it.copy(savedStatus = wasSaved) }
                }
            }
        }
    }

    private fun updateRecipeInList(recipeId: String, update: (RecipeSummary) -> RecipeSummary) {
        _uiState.update { state ->
            state.copy(recipes = state.recipes.map { recipe ->
                if (recipe.id == recipeId) update(recipe) else recipe
            })
        }
    }
}
