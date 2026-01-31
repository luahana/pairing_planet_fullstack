package com.cookstemma.app.data.repository

import android.util.Log
import com.cookstemma.app.domain.model.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SavedItemsManager"

/**
 * Centralized manager for saved items state.
 * Ensures save state is synchronized across all ViewModels.
 */
@Singleton
class SavedItemsManager @Inject constructor(
    private val feedRepository: FeedRepository,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Saved logs state
    private val _savedLogIds = MutableStateFlow<Set<String>>(emptySet())
    val savedLogIds: StateFlow<Set<String>> = _savedLogIds.asStateFlow()

    // Saved recipes state
    private val _savedRecipeIds = MutableStateFlow<Set<String>>(emptySet())
    val savedRecipeIds: StateFlow<Set<String>> = _savedRecipeIds.asStateFlow()

    // Track if initial fetch is complete
    private var hasFetchedSavedLogs = false
    private var hasFetchedSavedRecipes = false

    // Track pending changes during initial fetch
    private val pendingLogSaves = mutableSetOf<String>()
    private val pendingLogUnsaves = mutableSetOf<String>()
    private val pendingRecipeSaves = mutableSetOf<String>()
    private val pendingRecipeUnsaves = mutableSetOf<String>()

    fun isLogSaved(logId: String): Boolean = _savedLogIds.value.contains(logId)
    fun isRecipeSaved(recipeId: String): Boolean = _savedRecipeIds.value.contains(recipeId)

    /**
     * Initialize saved logs from API. Call this when user is authenticated.
     */
    fun fetchSavedLogIds() {
        if (hasFetchedSavedLogs) return

        scope.launch {
            val allSavedIds = mutableSetOf<String>()
            var cursor: String? = null

            do {
                userRepository.getSavedLogs(cursor).collect { result ->
                    if (result is Result.Success) {
                        allSavedIds.addAll(result.data.content.map { it.id })
                        cursor = if (result.data.hasMore) result.data.nextCursor else null
                    } else {
                        cursor = null
                    }
                }
            } while (cursor != null)

            // Apply fetched IDs while respecting pending changes
            val mergedIds = (allSavedIds + pendingLogSaves) - pendingLogUnsaves
            Log.d(TAG, "Fetched saved logs: ${allSavedIds.size}, merged: ${mergedIds.size}")
            _savedLogIds.value = mergedIds

            pendingLogSaves.clear()
            pendingLogUnsaves.clear()
            hasFetchedSavedLogs = true
        }
    }

    /**
     * Initialize saved recipes from API. Call this when user is authenticated.
     */
    fun fetchSavedRecipeIds() {
        if (hasFetchedSavedRecipes) return

        scope.launch {
            val allSavedIds = mutableSetOf<String>()
            var cursor: String? = null

            do {
                userRepository.getSavedRecipes(cursor).collect { result ->
                    if (result is Result.Success) {
                        allSavedIds.addAll(result.data.content.map { it.id })
                        cursor = if (result.data.hasMore) result.data.nextCursor else null
                    } else {
                        cursor = null
                    }
                }
            } while (cursor != null)

            // Apply fetched IDs while respecting pending changes
            val mergedIds = (allSavedIds + pendingRecipeSaves) - pendingRecipeUnsaves
            Log.d(TAG, "Fetched saved recipes: ${allSavedIds.size}, merged: ${mergedIds.size}")
            _savedRecipeIds.value = mergedIds

            pendingRecipeSaves.clear()
            pendingRecipeUnsaves.clear()
            hasFetchedSavedRecipes = true
        }
    }

    /**
     * Toggle save state for a log. Handles optimistic update and API call.
     */
    fun toggleSaveLog(logId: String, onError: (() -> Unit)? = null) {
        val wasSaved = isLogSaved(logId)
        Log.d(TAG, "toggleSaveLog: id=$logId, wasSaved=$wasSaved")

        // Track pending change if initial fetch is still in progress
        if (!hasFetchedSavedLogs) {
            if (wasSaved) {
                pendingLogUnsaves.add(logId)
                pendingLogSaves.remove(logId)
            } else {
                pendingLogSaves.add(logId)
                pendingLogUnsaves.remove(logId)
            }
        }

        // Optimistic update
        _savedLogIds.value = if (wasSaved) {
            _savedLogIds.value - logId
        } else {
            _savedLogIds.value + logId
        }

        scope.launch {
            val result = if (wasSaved) {
                feedRepository.unsaveLog(logId)
            } else {
                feedRepository.saveLog(logId)
            }

            if (result is Result.Error) {
                Log.e(TAG, "Save log failed: ${result.exception.message}")
                pendingLogSaves.remove(logId)
                pendingLogUnsaves.remove(logId)
                // Revert on failure
                _savedLogIds.value = if (wasSaved) {
                    _savedLogIds.value + logId
                } else {
                    _savedLogIds.value - logId
                }
                onError?.invoke()
            }
        }
    }

    /**
     * Toggle save state for a recipe. Handles optimistic update and API call.
     */
    fun toggleSaveRecipe(recipeId: String, onError: (() -> Unit)? = null) {
        val wasSaved = isRecipeSaved(recipeId)
        Log.d(TAG, "toggleSaveRecipe: id=$recipeId, wasSaved=$wasSaved")

        // Track pending change if initial fetch is still in progress
        if (!hasFetchedSavedRecipes) {
            if (wasSaved) {
                pendingRecipeUnsaves.add(recipeId)
                pendingRecipeSaves.remove(recipeId)
            } else {
                pendingRecipeSaves.add(recipeId)
                pendingRecipeUnsaves.remove(recipeId)
            }
        }

        // Optimistic update
        _savedRecipeIds.value = if (wasSaved) {
            _savedRecipeIds.value - recipeId
        } else {
            _savedRecipeIds.value + recipeId
        }

        scope.launch {
            val flow = if (wasSaved) {
                recipeRepository.unsaveRecipe(recipeId)
            } else {
                recipeRepository.saveRecipe(recipeId)
            }

            flow.collect { result ->
                if (result is Result.Error) {
                    Log.e(TAG, "Save recipe failed: ${result.exception.message}")
                    pendingRecipeSaves.remove(recipeId)
                    pendingRecipeUnsaves.remove(recipeId)
                    // Revert on failure
                    _savedRecipeIds.value = if (wasSaved) {
                        _savedRecipeIds.value + recipeId
                    } else {
                        _savedRecipeIds.value - recipeId
                    }
                    onError?.invoke()
                }
            }
        }
    }

    /**
     * Clear all saved state. Call this on logout.
     */
    fun clear() {
        _savedLogIds.value = emptySet()
        _savedRecipeIds.value = emptySet()
        hasFetchedSavedLogs = false
        hasFetchedSavedRecipes = false
        pendingLogSaves.clear()
        pendingLogUnsaves.clear()
        pendingRecipeSaves.clear()
        pendingRecipeUnsaves.clear()
    }
}
