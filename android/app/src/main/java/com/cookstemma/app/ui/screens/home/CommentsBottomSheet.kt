package com.cookstemma.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cookstemma.app.R
import com.cookstemma.app.data.repository.Comment
import com.cookstemma.app.ui.theme.BrandOrange
import com.cookstemma.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    logId: String,
    onDismiss: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: CommentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(logId) {
        viewModel.loadComments(logId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.reset() }
    }

    // Load more when reaching end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= uiState.comments.size - 3) {
                    viewModel.loadMore()
                }
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Header
            CommentsHeader(
                commentCount = uiState.comments.size,
                onClose = onDismiss
            )

            HorizontalDivider()

            // Comments list
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.error != null -> {
                        Text(
                            text = uiState.error ?: stringResource(R.string.error_loading_comments),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(Spacing.md),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    uiState.comments.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.no_comments_yet),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(Spacing.md),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(vertical = Spacing.sm)
                        ) {
                            items(uiState.comments, key = { it.id }) { comment ->
                                CommentItem(
                                    comment = comment,
                                    onLike = viewModel::toggleCommentLike,
                                    onReply = { viewModel.setReplyingTo(comment) },
                                    onNavigateToProfile = onNavigateToProfile
                                )
                            }
                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spacing.md),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Comment input
            CommentInputBar(
                text = uiState.commentText,
                onTextChange = viewModel::setCommentText,
                onSubmit = viewModel::submitComment,
                isSubmitting = uiState.isSubmitting,
                replyingTo = uiState.replyingTo,
                onCancelReply = { viewModel.setReplyingTo(null) }
            )
        }
    }
}

@Composable
private fun CommentsHeader(
    commentCount: Int,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md)
    ) {
        Text(
            text = stringResource(R.string.comments),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center)
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    onLike: (Comment) -> Unit,
    onReply: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    isReply: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isReply) 48.dp else Spacing.md,
                end = Spacing.md,
                top = Spacing.xs,
                bottom = Spacing.xs
            )
    ) {
        val avatarSize = if (isReply) 28.dp else 36.dp
        val username = comment.author.username ?: ""
        val avatarColors = listOf(
            Color(0xFFE57373), Color(0xFFBA68C8), Color(0xFF64B5F6),
            Color(0xFF4DB6AC), Color(0xFF81C784), Color(0xFFFFD54F),
            Color(0xFFFF8A65), Color(0xFFA1887F), Color(0xFF90A4AE)
        )
        val avatarBgColor = avatarColors[kotlin.math.abs(username.hashCode()) % avatarColors.size]

        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(if (comment.author.avatarUrl != null) Color.Transparent else avatarBgColor)
                .clickable { onNavigateToProfile(comment.author.id) },
            contentAlignment = Alignment.Center
        ) {
            if (comment.author.avatarUrl != null) {
                AsyncImage(
                    model = comment.author.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = username.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(Spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.author.username ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToProfile(comment.author.id) }
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = comment.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comment.content ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Like button
                Row(
                    modifier = Modifier
                        .clickable(onClick = { onLike(comment) })
                        .padding(vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (comment.isLiked) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.cd_like),
                        tint = if (comment.isLiked) Color.Red
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    if (comment.likeCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${comment.likeCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                // Reply button
                Text(
                    text = stringResource(R.string.reply),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = onReply)
                        .padding(vertical = Spacing.xs)
                )
            }

            // Show replies
            comment.replies?.forEach { reply ->
                CommentItem(
                    comment = reply,
                    onLike = onLike,
                    onReply = onReply,
                    onNavigateToProfile = onNavigateToProfile,
                    isReply = true
                )
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean,
    replyingTo: Comment?,
    onCancelReply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HorizontalDivider()
        
        if (replyingTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.replying_to, replyingTo.author.username ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onCancelReply,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.write_comment)) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandOrange,
                    cursorColor = BrandOrange
                )
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            IconButton(
                onClick = onSubmit,
                enabled = text.isNotBlank() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = BrandOrange
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.cd_send),
                        tint = if (text.isNotBlank()) BrandOrange
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
