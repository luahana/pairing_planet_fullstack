package com.cookstemma.app.data.repository

import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.data.api.BlockedUser
import com.cookstemma.app.data.api.PagedResponse
import com.cookstemma.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getMyProfile(): Flow<Result<MyProfile>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(apiService.getMyProfile()))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getUserProfile(id: String): Flow<Result<UserProfile>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(apiService.getUserProfile(id)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getUserRecipes(userId: String, cursor: String?): Flow<Result<PaginatedResponse<RecipeSummary>>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(apiService.getUserRecipes(userId, cursor)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getUserLogs(userId: String, cursor: String?): Flow<Result<PaginatedResponse<CookingLogSummary>>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(apiService.getUserLogs(userId, cursor)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun followUser(userId: String): Result<Unit> = try {
        apiService.followUser(userId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun unfollowUser(userId: String): Result<Unit> = try {
        apiService.unfollowUser(userId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getFollowers(userId: String, cursor: String?): Flow<Result<PaginatedResponse<UserSummary>>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(apiService.getFollowers(userId, cursor)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getFollowing(userId: String, cursor: String?): Flow<Result<PaginatedResponse<UserSummary>>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(apiService.getFollowing(userId, cursor)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun blockUser(userId: String): Result<Unit> = try {
        apiService.blockUser(userId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun unblockUser(userId: String): Result<Unit> = try {
        apiService.unblockUser(userId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateProfile(
        avatar: MultipartBody.Part? = null,
        username: String? = null,
        displayName: String? = null,
        bio: String? = null,
        youtubeUrl: String? = null,
        instagramHandle: String? = null,
        tiktokHandle: String? = null,
        website: String? = null
    ): Result<MyProfile> = try {
        val profile = apiService.updateProfile(
            avatar = avatar,
            username = username,
            displayName = displayName,
            bio = bio,
            youtubeUrl = youtubeUrl,
            instagramHandle = instagramHandle,
            tiktokHandle = tiktokHandle,
            website = website
        )
        Result.Success(profile)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun checkUsernameAvailability(username: String): Result<Boolean> = try {
        val response = apiService.checkUsernameAvailability(username)
        Result.Success(response.available)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getBlockedUsers(page: Int = 0): Result<PagedResponse<BlockedUser>> = try {
        Result.Success(apiService.getBlockedUsers(page))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun reportUser(userId: String, reason: String): Result<Unit> = try {
        apiService.reportUser(userId, reason)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteAccount(): Result<Unit> = try {
        apiService.deleteAccount()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getSavedRecipes(cursor: String?): Flow<Result<PaginatedResponse<RecipeSummary>>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(apiService.getSavedRecipes(cursor)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getSavedLogs(cursor: String?): Flow<Result<PaginatedResponse<CookingLog>>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(apiService.getSavedLogs(cursor)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
