package com.cookstemma.app.data.repository

import android.util.Log
import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getRecipes(cursor: String?, filters: RecipeFilters?): Flow<Result<PaginatedResponse<RecipeSummary>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getRecipes(
                cursor = cursor,
                query = filters?.searchQuery,
                cookingTimeRange = filters?.cookingTimeRange?.value,
                category = filters?.category,
                servings = filters?.servingsRange?.displayName,
                sort = filters?.sortBy?.value ?: "trending"
            )
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getRecipe(id: String): Flow<Result<RecipeDetail>> = flow {
        emit(Result.Loading)
        try {
            val recipe = apiService.getRecipe(id).withSavedState()
            emit(Result.Success(recipe))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getRecipeLogs(recipeId: String, page: Int? = null): Flow<Result<PaginatedResponse<RecipeLogItem>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getRecipeLogs(recipeId, page)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun saveRecipe(id: String): Flow<Result<Unit>> = flow {
        try {
            Log.d("RecipeRepository", "Saving recipe: $id")
            apiService.saveRecipe(id)
            Log.d("RecipeRepository", "Recipe saved successfully: $id")
            emit(Result.Success(Unit))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("RecipeRepository", "Save recipe failed: ${e.code()} - $errorBody")
            emit(Result.Error(e))
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Save recipe error: ${e.message}", e)
            emit(Result.Error(e))
        }
    }

    fun unsaveRecipe(id: String): Flow<Result<Unit>> = flow {
        try {
            apiService.unsaveRecipe(id)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
