package com.cookstemma.app.ui.screens.hashtag

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cookstemma.app.domain.model.FeedItem
import com.cookstemma.app.ui.components.AppIcons
import com.cookstemma.app.ui.components.IconEmptyState
import com.cookstemma.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLog: (String) -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    viewModel: HashtagDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()

    // Load more when reaching end
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= uiState.filteredPosts.size - 4) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "#${uiState.hashtag}",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.posts.isEmpty() -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                uiState.error != null && uiState.posts.isEmpty() -> {
                    IconEmptyState(
                        icon = AppIcons.error,
                        subtitle = uiState.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refresh() }
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            contentPadding = PaddingValues(Spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Post count header
                            item(span = { GridItemSpan(2) }) {
                                PostCountHeader(count = uiState.posts.size)
                            }

                            // Filter tabs
                            item(span = { GridItemSpan(2) }) {
                                HashtagContentFilterRow(
                                    selectedFilter = uiState.selectedFilter,
                                    onFilterChange = { viewModel.selectFilter(it) }
                                )
                            }

                            // Empty state for filtered content
                            if (uiState.filteredPosts.isEmpty() && !uiState.isLoading) {
                                item(span = { GridItemSpan(2) }) {
                                    FilterEmptyState(
                                        filter = uiState.selectedFilter,
                                        modifier = Modifier.padding(vertical = Spacing.xl)
                                    )
                                }
                            }

                            items(uiState.filteredPosts, key = { it.id }) { item ->
                                HashtagContentGridCard(
                                    item = item,
                                    onClick = {
                                        if (item.isRecipe) {
                                            onNavigateToRecipe(item.id)
                                        } else {
                                            onNavigateToLog(item.id)
                                        }
                                    }
                                )
                            }

                            if (uiState.isLoadingMore) {
                                item(span = { GridItemSpan(2) }) {
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
        }
    }
}

@Composable
private fun PostCountHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = AppIcons.gridView,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.xs))
        Text(
            text = "$count posts",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HashtagContentFilterRow(
    selectedFilter: HashtagContentFilter,
    onFilterChange: (HashtagContentFilter) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
    ) {
        HashtagContentFilter.entries.forEachIndexed { index, filter ->
            SegmentedButton(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = HashtagContentFilter.entries.size
                )
            ) {
                Text(filter.title)
            }
        }
    }
}

@Composable
private fun HashtagContentGridCard(
    item: FeedItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(Spacing.sm))
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Rating overlay for logs
            if (item.isLog && item.rating != null && item.rating > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(Spacing.xs)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(Spacing.xxs)
                        )
                        .padding(horizontal = Spacing.xs, vertical = 2.dp)
                ) {
                    Row {
                        repeat(item.rating) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = item.title ?: item.foodName ?: "",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "@${item.userName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FilterEmptyState(
    filter: HashtagContentFilter,
    modifier: Modifier = Modifier
) {
    val (icon, message) = when (filter) {
        HashtagContentFilter.ALL -> AppIcons.trending to "No posts with this hashtag yet"
        HashtagContentFilter.RECIPES -> AppIcons.recipe to "No recipes with this hashtag yet"
        HashtagContentFilter.LOGS -> AppIcons.log to "No cooking logs with this hashtag yet"
    }

    IconEmptyState(
        icon = icon,
        subtitle = message,
        modifier = modifier.fillMaxWidth()
    )
}
