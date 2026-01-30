package com.cookstemma.app.data.repository

import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class Notification(
    val id: String,
    val type: NotificationType,
    val message: String,
    val actorUser: UserSummary?,
    val targetRecipeId: String?,
    val targetLogId: String?,
    val isRead: Boolean,
    val createdAt: String
)

enum class NotificationType {
    NEW_FOLLOWER,
    COMMENT,
    COMMENT_REPLY,
    COMMENT_LIKE,
    RECIPE_COOKED,
    RECIPE_SAVED,
    LOG_LIKED
}

class NotificationRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getNotifications(cursor: String? = null): Flow<Result<PaginatedResponse<Notification>>> = flow {
        try {
            val response = apiService.getNotifications(cursor)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getUnreadCount(): Flow<Result<Int>> = flow {
        try {
            val response = apiService.getUnreadNotificationCount()
            emit(Result.Success(response.count))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun markAsRead(notificationId: String): Flow<Result<Unit>> = flow {
        try {
            apiService.markNotificationRead(notificationId)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun markAllAsRead(): Flow<Result<Unit>> = flow {
        try {
            apiService.markAllNotificationsRead()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun registerFcmToken(token: String): Flow<Result<Unit>> = flow {
        try {
            apiService.registerFcmToken(token)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun unregisterFcmToken(token: String): Flow<Result<Unit>> = flow {
        try {
            apiService.unregisterFcmToken(token)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun deleteNotification(notificationId: String): Result<Unit> = try {
        apiService.deleteNotification(notificationId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteAllNotifications(): Result<Unit> = try {
        apiService.deleteAllNotifications()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
