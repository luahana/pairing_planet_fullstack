package com.cookstemma.app.data.repository

import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class LogRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getLog(logId: String): Flow<Result<CookingLogDetail>> = flow {
        try {
            val response = apiService.getLog(logId)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun createLog(
        photos: List<File>,
        rating: Int,
        recipeId: String?,
        content: String?,
        hashtags: List<String>?,
        isPrivate: Boolean
    ): Flow<Result<CookingLogDetail>> = flow {
        try {
            val photoParts = photos.mapIndexed { index, file ->
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("photos", file.name, requestBody)
            }

            val response = apiService.createLog(
                photos = photoParts,
                rating = rating,
                recipeId = recipeId,
                content = content,
                hashtags = hashtags,
                isPrivate = isPrivate
            )
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun updateLog(
        logId: String,
        rating: Int?,
        content: String?,
        hashtags: List<String>?,
        isPrivate: Boolean?
    ): Flow<Result<CookingLogDetail>> = flow {
        try {
            val response = apiService.updateLog(
                logId = logId,
                rating = rating,
                content = content,
                hashtags = hashtags,
                isPrivate = isPrivate
            )
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun deleteLog(logId: String): Flow<Result<Unit>> = flow {
        try {
            apiService.deleteLog(logId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun likeLog(logId: String): Flow<Result<Unit>> = flow {
        try {
            apiService.likeLog(logId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun unlikeLog(logId: String): Flow<Result<Unit>> = flow {
        try {
            apiService.unlikeLog(logId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun saveLog(logId: String): Flow<Result<Unit>> = flow {
        try {
            apiService.saveLog(logId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun unsaveLog(logId: String): Flow<Result<Unit>> = flow {
        try {
            apiService.unsaveLog(logId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
