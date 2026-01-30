package com.cookstemma.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cookstemma.app.data.repository.Notification
import com.cookstemma.app.data.repository.NotificationType
import com.cookstemma.app.ui.components.*
import com.cookstemma.app.ui.theme.BrandOrange
import com.cookstemma.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToLog: (String) -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = AppIcons.back,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Icon header
                    Icon(
                        imageVector = AppIcons.notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    if (uiState.unreadCount > 0) {
                        // Mark all button (icon only)
                        IconButton(onClick = viewModel::markAllAsRead) {
                            Icon(
                                imageVector = AppIcons.checkmarkAll,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.notifications.isEmpty() -> {
                EmptyNotificationsState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    val (unread, read) = uiState.notifications.partition { !it.isRead }

                    // New notifications (icon header)
                    if (unread.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                Icon(
                                    imageVector = AppIcons.newBadge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                // Unread indicator dot
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                )
                            }
                        }
                        items(unread) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    viewModel.markAsRead(notification)
                                    handleNotificationClick(
                                        notification = notification,
                                        onNavigateToProfile = onNavigateToProfile,
                                        onNavigateToLog = onNavigateToLog,
                                        onNavigateToRecipe = onNavigateToRecipe
                                    )
                                }
                            )
                        }
                    }

                    // Earlier notifications (icon header)
                    if (read.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(Spacing.sm))
                            Icon(
                                imageVector = AppIcons.history,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        items(read) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    handleNotificationClick(
                                        notification = notification,
                                        onNavigateToProfile = onNavigateToProfile,
                                        onNavigateToLog = onNavigateToLog,
                                        onNavigateToRecipe = onNavigateToRecipe
                                    )
                                }
                            )
                        }
                    }

                    // Load more (icon button)
                    if (uiState.hasMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.md),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isLoadingMore) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    IconButton(onClick = viewModel::loadMore) {
                                        Icon(
                                            imageVector = AppIcons.forward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun handleNotificationClick(
    notification: Notification,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToLog: (String) -> Unit,
    onNavigateToRecipe: (String) -> Unit
) {
    when (notification.type) {
        NotificationType.NEW_FOLLOWER -> {
            notification.actorUser?.let { onNavigateToProfile(it.id) }
        }
        NotificationType.COMMENT,
        NotificationType.COMMENT_REPLY,
        NotificationType.COMMENT_LIKE,
        NotificationType.LOG_LIKED -> {
            notification.targetLogId?.let { onNavigateToLog(it) }
        }
        NotificationType.RECIPE_COOKED,
        NotificationType.RECIPE_SAVED -> {
            notification.targetLogId?.let { onNavigateToLog(it) }
                ?: notification.targetRecipeId?.let { onNavigateToRecipe(it) }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (!notification.isRead) {
                    BrandOrange.copy(alpha = 0.05f)
                } else {
                    Color.Transparent
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = notification.actorUser?.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            NotificationTypeIcon(
                type = notification.type,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    notification.actorUser?.let { user ->
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append("@${user.username}")
                        }
                        append(" ")
                    }
                    append(getNotificationText(notification.type))
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                notification.createdAt,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BrandOrange)
            )
        }
    }
}

@Composable
private fun NotificationTypeIcon(
    type: NotificationType,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (type) {
        NotificationType.NEW_FOLLOWER -> Icons.Default.PersonAdd to Color(0xFF4CAF50)
        NotificationType.COMMENT, NotificationType.COMMENT_REPLY -> Icons.Default.ChatBubble to Color(0xFF2196F3)
        NotificationType.COMMENT_LIKE, NotificationType.LOG_LIKED -> Icons.Default.Favorite to Color(0xFFE91E63)
        NotificationType.RECIPE_COOKED -> Icons.Default.Restaurant to BrandOrange
        NotificationType.RECIPE_SAVED -> Icons.Default.Bookmark to Color(0xFF9C27B0)
    }

    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(tint),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
    }
}

private fun getNotificationText(type: NotificationType): String {
    return when (type) {
        NotificationType.NEW_FOLLOWER -> "started following you"
        NotificationType.COMMENT -> "commented on your log"
        NotificationType.COMMENT_REPLY -> "replied to your comment"
        NotificationType.COMMENT_LIKE -> "liked your comment"
        NotificationType.LOG_LIKED -> "liked your cooking log"
        NotificationType.RECIPE_COOKED -> "cooked your recipe"
        NotificationType.RECIPE_SAVED -> "saved your recipe"
    }
}

@Composable
private fun EmptyNotificationsState(modifier: Modifier = Modifier) {
    // Empty state (icon only)
    IconEmptyState(
        icon = AppIcons.notificationsOutline,
        modifier = modifier
    )
}
