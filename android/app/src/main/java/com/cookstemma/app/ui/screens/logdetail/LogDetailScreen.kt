@file:OptIn(ExperimentalFoundationApi::class)

package com.cookstemma.app.ui.screens.logdetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cookstemma.app.R
import coil.compose.AsyncImage
import com.cookstemma.app.ui.AppState
import com.cookstemma.app.ui.components.StarRating
import com.cookstemma.app.ui.screens.home.CommentsBottomSheet
import com.cookstemma.app.ui.theme.BrandOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: LogDetailViewModel = hiltViewModel(),
    appState: AppState? = null,
    isAuthenticated: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCommentsSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (appState != null) {
                            appState.requireAuth(isAuthenticated) {
                                viewModel.toggleSave()
                            }
                        } else {
                            viewModel.toggleSave()
                        }
                    }) {
                        Icon(
                            if (uiState.log?.isSaved == true) Icons.Filled.Bookmark
                            else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.save),
                            tint = if (uiState.log?.isSaved == true) BrandOrange else Color.Gray
                        )
                    }
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share))
                    }
                    // Edit button - show only for own logs (simplified for now)
                    uiState.log?.let { log ->
                        IconButton(onClick = { onNavigateToEdit(log.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                        }
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_more_options))
                    }
                }
            )
        }
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
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error ?: stringResource(R.string.error_generic))
                }
            }
            uiState.log != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    item {
                        LogContent(
                            log = uiState.log!!,
                            onLike = viewModel::toggleLike,
                            onCommentClick = { showCommentsSheet = true },
                            onNavigateToRecipe = onNavigateToRecipe,
                            onNavigateToProfile = onNavigateToProfile
                        )
                    }
                }
            }
        }
    }

    // Comments bottom sheet
    if (showCommentsSheet) {
        uiState.log?.let { log ->
            CommentsBottomSheet(
                logId = log.id,
                onDismiss = { showCommentsSheet = false },
                onNavigateToProfile = onNavigateToProfile
            )
        }
    }
}

@Composable
private fun LogContent(
    log: com.cookstemma.app.domain.model.CookingLogDetail,
    onLike: () -> Unit,
    onCommentClick: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    Column {
        // Author header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToProfile(log.author.id) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = log.author.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    log.author.username ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    log.createdAt.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Photo carousel
        val logImages = log.images.orEmpty()
        if (logImages.isNotEmpty()) {
            val pagerState = rememberPagerState { logImages.size }
            Box {
                HorizontalPager(state = pagerState) { page ->
                    AsyncImage(
                        model = logImages[page].originalUrl ?: logImages[page].thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                }
                if (logImages.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(logImages.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == pagerState.currentPage) Color.White
                                        else Color.White.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            // Like button
            IconButton(onClick = onLike) {
                Icon(
                    if (log.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(R.string.cd_like),
                    tint = if (log.isLiked) Color.Red else Color.Gray
                )
            }
            Text(
                "${log.likeCount}",
                modifier = Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Comment button - clickable to open bottom sheet
            Row(
                modifier = Modifier
                    .clickable(onClick = onCommentClick)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = stringResource(R.string.comments),
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${log.commentCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Rating
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StarRating(rating = log.rating, size = 20.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "${log.rating}.0",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Content
        if (!log.content.isNullOrBlank()) {
            Text(
                log.content,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Linked recipe
        log.recipe?.let { recipe ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onNavigateToRecipe(recipe.id) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            recipe.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "by @${recipe.userName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.cd_view),
                        tint = Color.Gray
                    )
                }
            }
        }

        // Hashtags
        if (!log.hashtags.isNullOrEmpty()) {
            Text(
                log.hashtags.joinToString(" ") { "#$it" },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = BrandOrange
            )
        }
    }
}
