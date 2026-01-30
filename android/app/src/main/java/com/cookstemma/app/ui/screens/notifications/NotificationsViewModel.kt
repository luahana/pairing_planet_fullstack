package com.cookstemma.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.repository.Notification
import com.cookstemma.app.data.repository.NotificationRepository
import com.cookstemma.app.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val cursor: String? = null,
    val hasMore: Boolean = false,
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
        loadUnreadCount()
    }

    private fun loadNotifications(cursor: String? = null) {
        viewModelScope.launch {
            if (cursor == null) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            notificationRepository.getNotifications(cursor).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                notifications = if (cursor == null) {
                                    result.data.content
                                } else {
                                    it.notifications + result.data.content
                                },
                                cursor = result.data.nextCursor,
                                hasMore = result.data.hasMore,
                                isLoading = false,
                                isLoadingMore = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = result.exception.message
                            )
                        }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            notificationRepository.getUnreadCount().collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(unreadCount = result.data) }
                }
            }
        }
    }

    fun loadMore() {
        val cursor = _uiState.value.cursor
        if (cursor != null && !_uiState.value.isLoadingMore) {
            loadNotifications(cursor)
        }
    }

    fun refresh() {
        loadNotifications()
        loadUnreadCount()
    }

    fun markAsRead(notification: Notification) {
        if (notification.isRead) return

        // Optimistic update
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map {
                    if (it.id == notification.id) it.copy(isRead = true) else it
                },
                unreadCount = maxOf(0, state.unreadCount - 1)
            )
        }

        viewModelScope.launch {
            notificationRepository.markAsRead(notification.id).collect { result ->
                if (result is Result.Error) {
                    // Revert on failure
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == notification.id) notification else it
                            },
                            unreadCount = state.unreadCount + 1
                        )
                    }
                }
            }
        }
    }

    fun markAllAsRead() {
        // Optimistic update
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { it.copy(isRead = true) },
                unreadCount = 0
            )
        }

        viewModelScope.launch {
            notificationRepository.markAllAsRead().collect { result ->
                if (result is Result.Error) {
                    // Reload on failure
                    loadNotifications()
                    loadUnreadCount()
                }
            }
        }
    }
}

// Extension to allow copying Notification data class
private fun Notification.copy(isRead: Boolean): Notification {
    return Notification(
        id = this.id,
        type = this.type,
        message = this.message,
        actorUser = this.actorUser,
        targetRecipeId = this.targetRecipeId,
        targetLogId = this.targetLogId,
        isRead = isRead,
        createdAt = this.createdAt
    )
}
