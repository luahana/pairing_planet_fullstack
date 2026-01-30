package com.cookstemma.app.data.repository

import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getFeed(cursor: String?): Flow<Result<PaginatedResponse<FeedItem>>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(apiService.getFeed(cursor)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getLog(id: String): Flow<Result<CookingLogDetail>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(apiService.getLog(id)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun likeLog(id: String): Result<Unit> = try {
        apiService.likeLog(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun unlikeLog(id: String): Result<Unit> = try {
        apiService.unlikeLog(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun saveLog(id: String): Result<Unit> = try {
        apiService.saveLog(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun unsaveLog(id: String): Result<Unit> = try {
        apiService.unsaveLog(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
