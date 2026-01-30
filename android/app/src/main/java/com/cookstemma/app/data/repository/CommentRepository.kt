package com.cookstemma.app.data.repository

import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.domain.model.*
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

// Matches API response structure from backend CommentResponseDto
data class Comment(
    @SerializedName("publicId")
    val id: String,
    val content: String?,
    val creatorPublicId: String,
    val creatorUsername: String,
    val creatorProfileImageUrl: String?,
    val replyCount: Int,
    @SerializedName("likeCount")
    val apiLikeCount: Int = 0,
    @SerializedName("isLikedByCurrentUser")
    val apiIsLiked: Boolean = false,
    val isEdited: Boolean = false,
    val isDeleted: Boolean? = null,
    val isHidden: Boolean? = null,
    val createdAt: String,
    // UI state (not from API, used for optimistic updates)
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val replies: List<Comment>? = null
) {
    // Computed author for UI compatibility
    val author: UserSummary get() = UserSummary(
        id = creatorPublicId,
        username = creatorUsername,
        displayName = null,
        avatarUrl = creatorProfileImageUrl
    )

    // Initialize UI state from API values
    fun withUiState(): Comment = copy(
        likeCount = apiLikeCount,
        isLiked = apiIsLiked
    )
}

class CommentRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getComments(
        logId: String,
        cursor: String? = null
    ): Flow<Result<PaginatedResponse<Comment>>> = flow {
        try {
            val response = apiService.getLogComments(logId, cursor)
            // Initialize UI state from API values
            val mappedResponse = PaginatedResponse(
                content = response.content.map { it.withUiState() },
                nextCursor = response.nextCursor,
                hasMore = response.hasMore
            )
            emit(Result.Success(mappedResponse))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getReplies(
        commentId: String,
        cursor: String? = null
    ): Flow<Result<PaginatedResponse<Comment>>> = flow {
        try {
            val response = apiService.getCommentReplies(commentId, cursor)
            // Initialize UI state from API values
            val mappedResponse = PaginatedResponse(
                content = response.content.map { it.withUiState() },
                nextCursor = response.nextCursor,
                hasMore = response.hasMore
            )
            emit(Result.Success(mappedResponse))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun createComment(
        logId: String,
        content: String,
        parentCommentId: String? = null
    ): Flow<Result<Comment>> = flow {
        try {
            val response = apiService.createComment(logId, content, parentCommentId)
            emit(Result.Success(response.withUiState()))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun updateComment(
        commentId: String,
        content: String
    ): Flow<Result<Comment>> = flow {
        try {
            val response = apiService.updateComment(commentId, content)
            emit(Result.Success(response.withUiState()))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun deleteComment(commentId: String): Flow<Result<Unit>> = flow {
        try {
            apiService.deleteComment(commentId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun likeComment(commentId: String): Flow<Result<Unit>> = flow {
        try {
            apiService.likeComment(commentId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun unlikeComment(commentId: String): Flow<Result<Unit>> = flow {
        try {
            apiService.unlikeComment(commentId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
