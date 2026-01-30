package com.cookstemma.app.data.repository

import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getSavedRecipes(cursor: String? = null): Flow<Result<PaginatedResponse<RecipeSummary>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getSavedRecipes(cursor)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getSavedLogs(cursor: String? = null): Flow<Result<PaginatedResponse<CookingLog>>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.getSavedLogs(cursor)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
