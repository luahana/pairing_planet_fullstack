package com.cookstemma.app.data.repository

import android.util.Log
import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.data.api.CommentRequest
import com.cookstemma.app.domain.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

private const val TAG = "CommentRepository"

// Matches API response structure from backend CommentResponseDto
data class Comment(
    @SerializedName("publicId")
    val id: String = "",
    val content: String? = null,
    val creatorPublicId: String = "",
    val creatorUsername: String = "",
    val creatorProfileImageUrl: String? = null,
    val replyCount: Int = 0,
    @SerializedName("likeCount")
    val apiLikeCount: Int? = null,
    @SerializedName("isLikedByCurrentUser")
    val apiIsLiked: Boolean? = null,
    val isEdited: Boolean? = null,
    val isDeleted: Boolean? = null,
    val isHidden: Boolean? = null,
    val createdAt: String = "",
    // UI state (not from API, used for optimistic updates) - marked @Transient so Gson ignores them
    @Transient
    val likeCount: Int = 0,
    @Transient
    val isLiked: Boolean = false,
    @Transient
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
        likeCount = apiLikeCount ?: 0,
        isLiked = apiIsLiked ?: false
    )
}

class CommentRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val gson = Gson()

    fun getComments(
        logId: String,
        cursor: String? = null
    ): Flow<Result<PaginatedResponse<Comment>>> = flow {
        try {
            val jsonResponse = apiService.getLogComments(logId, cursor)
            val comments = parseCommentsFromJson(jsonResponse)
            val isLast = jsonResponse.get("last")?.asBoolean ?: true
            val mappedResponse = PaginatedResponse(
                content = comments,
                nextCursor = null, // API uses page-based pagination
                hasMore = !isLast
            )
            emit(Result.Success(mappedResponse))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    private fun parseCommentsFromJson(json: JsonObject): List<Comment> {
        Log.d(TAG, "parseCommentsFromJson: $json")
        val contentArray = json.getAsJsonArray("content")
        Log.d(TAG, "contentArray: $contentArray, size: ${contentArray?.size()}")
        if (contentArray == null) return emptyList()
        
        return contentArray.mapNotNull { element ->
            try {
                val wrapper = element.asJsonObject
                Log.d(TAG, "wrapper: $wrapper")
                val commentObj = wrapper.getAsJsonObject("comment")
                Log.d(TAG, "commentObj: $commentObj")
                val repliesArray = wrapper.getAsJsonArray("replies")
                
                val comment = gson.fromJson(commentObj, Comment::class.java)
                Log.d(TAG, "parsed comment: $comment")
                if (comment == null) return@mapNotNull null
                
                val replies = repliesArray?.mapNotNull { replyElement ->
                    val reply = gson.fromJson(replyElement, Comment::class.java)
                    reply?.withUiState()
                }
                
                comment.withUiState().copy(replies = replies)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing comment", e)
                null
            }
        }
    }

    fun getReplies(
        commentId: String,
        cursor: String? = null
    ): Flow<Result<PaginatedResponse<Comment>>> = flow {
        try {
            val jsonResponse = apiService.getCommentReplies(commentId, cursor)
            val contentArray = jsonResponse.getAsJsonArray("content")
            val comments = contentArray?.mapNotNull { element ->
                try {
                    val comment = gson.fromJson(element, Comment::class.java)
                    comment?.withUiState()
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            val isLast = jsonResponse.get("last")?.asBoolean ?: true
            val mappedResponse = PaginatedResponse(
                content = comments,
                nextCursor = null, // API uses page-based pagination
                hasMore = !isLast
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
            val request = CommentRequest(content)
            val response = if (parentCommentId != null) {
                apiService.createReply(parentCommentId, request)
            } else {
                apiService.createComment(logId, request)
            }
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
            val request = CommentRequest(content)
            val response = apiService.updateComment(commentId, request)
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
