package com.cookstemma.app.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cookstemma.app.domain.model.SocialLinks
import com.cookstemma.app.ui.components.*
import com.cookstemma.app.ui.theme.AvatarSize
import com.cookstemma.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String?,
    viewModel: ProfileViewModel = hiltViewModel(),
    onRecipeClick: (String) -> Unit,
    onLogClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onFollowersClick: (String) -> Unit = {},
    onUserBlocked: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReportReason by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val isOwnProfile = userId == null

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    LaunchedEffect(uiState.blockSuccess) {
        if (uiState.blockSuccess) {
            onUserBlocked()
            viewModel.clearBlockSuccess()
        }
    }

    LaunchedEffect(uiState.reportSuccess) {
        if (uiState.reportSuccess) {
            viewModel.clearReportSuccess()
        }
    }

    // Refresh handling
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    if (!isOwnProfile) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = AppIcons.more,
                                    contentDescription = "More options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(if (uiState.isBlocked) "Unblock User" else "Block User")
                                    },
                                    onClick = {
                                        showMenu = false
                                        if (uiState.isBlocked) {
                                            viewModel.blockUser()
                                        } else {
                                            showBlockConfirmDialog = true
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(imageVector = AppIcons.block, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Report User") },
                                    onClick = {
                                        showMenu = false
                                        showReportDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(imageVector = AppIcons.report, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && !isRefreshing -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    IconEmptyState(
                        icon = AppIcons.error,
                        subtitle = uiState.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            viewModel.loadProfile(userId)
                        }
                    ) {
                        ProfileContent(
                            uiState = uiState,
                            isOwnProfile = isOwnProfile,
                            onFollowClick = { viewModel.toggleFollow() },
                            onRecipeClick = onRecipeClick,
                            onLogClick = onLogClick,
                            onTabChange = { viewModel.selectTab(it) },
                            onVisibilityFilterChange = { viewModel.setVisibilityFilter(it) },
                            onSavedContentFilterChange = { viewModel.setSavedContentFilter(it) },
                            onFollowersClick = { onFollowersClick(userId ?: uiState.userId ?: "") },
                            onFollowingClick = { onFollowersClick(userId ?: uiState.userId ?: "") },
                            onSettingsClick = onSettingsClick
                        )
                    }
                }
            }
        }
    }

    // Block Confirm Dialog
    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = AppIcons.block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Block User") },
            text = {
                Text("Are you sure you want to block @${uiState.username}? They won't be able to see your content or contact you.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBlockConfirmDialog = false
                        viewModel.blockUser()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Block")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Report Dialog
    if (showReportDialog) {
        val reportReasons = listOf(
            "Spam" to "spam",
            "Harassment or Bullying" to "harassment",
            "Inappropriate Content" to "inappropriate",
            "Impersonation" to "impersonation",
            "Other" to "other"
        )

        AlertDialog(
            onDismissRequest = {
                showReportDialog = false
                selectedReportReason = null
            },
            icon = {
                Icon(
                    imageVector = AppIcons.report,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Report User") },
            text = {
                Column {
                    Text(
                        "Why are you reporting @${uiState.username}?",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )
                    reportReasons.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReportReason = value }
                                .padding(vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReportReason == value,
                                onClick = { selectedReportReason = value }
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedReportReason?.let { reason ->
                            viewModel.reportUser(reason)
                            showReportDialog = false
                            selectedReportReason = null
                        }
                    },
                    enabled = selectedReportReason != null,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Report")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReportDialog = false
                        selectedReportReason = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    isOwnProfile: Boolean,
    onFollowClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onLogClick: (String) -> Unit,
    onTabChange: (ProfileTab) -> Unit,
    onVisibilityFilterChange: (VisibilityFilter) -> Unit,
    onSavedContentFilterChange: (SavedContentFilter) -> Unit,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // Profile Header (iOS-style horizontal layout)
        item(span = { GridItemSpan(2) }) {
            ProfileHeader(
                avatarUrl = uiState.avatarUrl,
                username = uiState.username,
                levelName = uiState.localizedLevelName,
                level = uiState.level,
                bio = uiState.bio,
                youtubeUrl = uiState.youtubeUrl,
                instagramHandle = uiState.instagramHandle,
                isOwnProfile = isOwnProfile,
                onSettingsClick = onSettingsClick,
                onSocialLinkClick = { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            )
        }

        // Stats Row
        item(span = { GridItemSpan(2) }) {
            StatsRow(
                recipeCount = uiState.recipeCount,
                logCount = uiState.logCount,
                followerCount = uiState.followerCount,
                followingCount = uiState.followingCount,
                onFollowersClick = onFollowersClick,
                onFollowingClick = onFollowingClick
            )
        }

        // Follow Button (for other profiles only)
        if (!isOwnProfile) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FollowIconButton(
                        isFollowing = uiState.isFollowing,
                        onClick = onFollowClick
                    )
                }
            }
        }

        // Divider
        item(span = { GridItemSpan(2) }) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        // Tab Bar
        item(span = { GridItemSpan(2) }) {
            ProfileTabBar(
                selectedTab = uiState.selectedTab,
                recipeCount = uiState.recipeCount,
                logCount = uiState.logCount,
                savedCount = uiState.savedCount,
                isOwnProfile = isOwnProfile,
                onTabChange = onTabChange
            )
        }

        // Visibility Filter (own profile only)
        if (isOwnProfile) {
            item(span = { GridItemSpan(2) }) {
                if (uiState.selectedTab == ProfileTab.SAVED) {
                    SavedContentFilterRow(
                        selectedFilter = uiState.savedContentFilter,
                        onFilterChange = onSavedContentFilterChange
                    )
                } else {
                    VisibilityFilterRow(
                        selectedFilter = uiState.visibilityFilter,
                        onFilterChange = onVisibilityFilterChange
                    )
                }
            }
        }

        // Loading indicator
        if (uiState.isLoadingContent) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Content Grid
        when (uiState.selectedTab) {
            ProfileTab.RECIPES -> {
                if (uiState.recipes.isEmpty() && !uiState.isLoadingContent) {
                    item(span = { GridItemSpan(2) }) {
                        EmptyState(
                            icon = AppIcons.recipe,
                            message = "No recipes yet"
                        )
                    }
                } else {
                    items(uiState.recipes, key = { it.id }) { recipe ->
                        RecipeGridCard(
                            imageUrl = recipe.coverImageUrl,
                            title = recipe.title,
                            onClick = { onRecipeClick(recipe.id) }
                        )
                    }
                }
            }
            ProfileTab.LOGS -> {
                if (uiState.logs.isEmpty() && !uiState.isLoadingContent) {
                    item(span = { GridItemSpan(2) }) {
                        EmptyState(
                            icon = AppIcons.log,
                            message = "No logs yet"
                        )
                    }
                } else {
                    items(uiState.logs, key = { it.id }) { log ->
                        LogGridCard(
                            imageUrl = log.images?.firstOrNull()?.thumbnailUrl,
                            rating = log.rating,
                            onClick = { onLogClick(log.id) }
                        )
                    }
                }
            }
            ProfileTab.SAVED -> {
                val filteredRecipes = if (uiState.savedContentFilter != SavedContentFilter.LOGS)
                    uiState.savedRecipes else emptyList()
                val filteredLogs = if (uiState.savedContentFilter != SavedContentFilter.RECIPES)
                    uiState.savedLogs else emptyList()

                if (filteredRecipes.isEmpty() && filteredLogs.isEmpty() && !uiState.isLoadingContent) {
                    item(span = { GridItemSpan(2) }) {
                        EmptyState(
                            icon = AppIcons.save,
                            message = when (uiState.savedContentFilter) {
                                SavedContentFilter.ALL -> "No saved items yet"
                                SavedContentFilter.RECIPES -> "No saved recipes yet"
                                SavedContentFilter.LOGS -> "No saved logs yet"
                            }
                        )
                    }
                } else {
                    items(filteredRecipes, key = { "recipe_${it.id}" }) { recipe ->
                        RecipeGridCard(
                            imageUrl = recipe.coverImageUrl,
                            title = recipe.title,
                            showSavedBadge = true,
                            onClick = { onRecipeClick(recipe.id) }
                        )
                    }
                    items(filteredLogs, key = { "log_${it.id}" }) { log ->
                        LogGridCard(
                            imageUrl = log.images?.firstOrNull()?.thumbnailUrl,
                            rating = log.rating,
                            showSavedBadge = true,
                            onClick = { onLogClick(log.id) }
                        )
                    }
                }
            }
        }
    }
}

// MARK: - Profile Header (iOS-style horizontal layout)
@Composable
private fun ProfileHeader(
    avatarUrl: String?,
    username: String,
    levelName: String,
    level: Int?,
    bio: String?,
    youtubeUrl: String?,
    instagramHandle: String?,
    isOwnProfile: Boolean,
    onSettingsClick: () -> Unit,
    onSocialLinkClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(AvatarSize.xl)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        // User Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            // Username
            Text(
                text = "@$username",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Level badge row
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level name badge
                Text(
                    text = levelName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(Spacing.xl)
                        )
                        .padding(horizontal = Spacing.sm, vertical = 2.dp)
                )

                // Level number
                level?.let {
                    Text(
                        text = "Lv. $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bio
            bio?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Social Links
            val hasYoutube = !youtubeUrl.isNullOrEmpty()
            val hasInstagram = !instagramHandle.isNullOrEmpty()
            if (hasYoutube || hasInstagram) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.padding(top = Spacing.xs)
                ) {
                    if (hasYoutube) {
                        SocialIconButton(
                            icon = "▶",
                            backgroundColor = Color(0xFFFF0000),
                            onClick = { onSocialLinkClick(youtubeUrl!!) }
                        )
                    }
                    if (hasInstagram) {
                        val cleanHandle = instagramHandle!!.replace("@", "")
                        val url = "https://instagram.com/$cleanHandle"
                        SocialIconButton(
                            icon = "📷",
                            gradientColors = listOf(Color(0xFFFFA500), Color(0xFFFF69B4), Color(0xFF9370DB)),
                            onClick = { onSocialLinkClick(url) }
                        )
                    }
                }
            }
        }

        // Settings button (own profile only)
        if (isOwnProfile) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = AppIcons.settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SocialIconButton(
    icon: String,
    backgroundColor: Color? = null,
    gradientColors: List<Color>? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .then(
                if (gradientColors != null) {
                    Modifier.background(
                        Brush.linearGradient(gradientColors)
                    )
                } else {
                    Modifier.background(backgroundColor ?: Color.Gray)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// MARK: - Stats Row
@Composable
private fun StatsRow(
    recipeCount: Int,
    logCount: Int,
    followerCount: Int,
    followingCount: Int,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(count = recipeCount, label = "Recipes")
        StatItem(count = logCount, label = "Logs")
        StatItem(
            count = followerCount,
            label = "Followers",
            onClick = onFollowersClick
        )
        StatItem(
            count = followingCount,
            label = "Following",
            onClick = onFollowingClick
        )
    }
}

@Composable
private fun StatItem(
    count: Int,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
    ) {
        Text(
            text = count.abbreviated(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// MARK: - Tab Bar
@Composable
private fun ProfileTabBar(
    selectedTab: ProfileTab,
    recipeCount: Int,
    logCount: Int,
    savedCount: Int,
    isOwnProfile: Boolean,
    onTabChange: (ProfileTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        ProfileTabButton(
            title = "Recipes",
            count = recipeCount,
            isSelected = selectedTab == ProfileTab.RECIPES,
            onClick = { onTabChange(ProfileTab.RECIPES) },
            modifier = Modifier.weight(1f)
        )
        ProfileTabButton(
            title = "Logs",
            count = logCount,
            isSelected = selectedTab == ProfileTab.LOGS,
            onClick = { onTabChange(ProfileTab.LOGS) },
            modifier = Modifier.weight(1f)
        )
        if (isOwnProfile) {
            ProfileTabButton(
                title = "Saved",
                count = savedCount,
                isSelected = selectedTab == ProfileTab.SAVED,
                onClick = { onTabChange(ProfileTab.SAVED) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProfileTabButton(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$title ($count)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.xs))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                )
        )
    }
}

// MARK: - Visibility Filter
@Composable
private fun VisibilityFilterRow(
    selectedFilter: VisibilityFilter,
    onFilterChange: (VisibilityFilter) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm)
    ) {
        VisibilityFilter.entries.forEachIndexed { index, filter ->
            SegmentedButton(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = VisibilityFilter.entries.size
                )
            ) {
                Text(filter.title)
            }
        }
    }
}

@Composable
private fun SavedContentFilterRow(
    selectedFilter: SavedContentFilter,
    onFilterChange: (SavedContentFilter) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm)
    ) {
        SavedContentFilter.entries.forEachIndexed { index, filter ->
            SegmentedButton(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = SavedContentFilter.entries.size
                )
            ) {
                Text(filter.title)
            }
        }
    }
}

// MARK: - Grid Cards
@Composable
private fun RecipeGridCard(
    imageUrl: String?,
    title: String,
    showSavedBadge: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Spacing.sm),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (showSavedBadge) {
                SavedBadge(Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

@Composable
private fun LogGridCard(
    imageUrl: String?,
    rating: Int?,
    showSavedBadge: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Spacing.sm),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Rating badge at bottom
            rating?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(Spacing.xs)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(Spacing.xs)
                        )
                        .padding(horizontal = Spacing.xs, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(it.coerceIn(1, 5)) {
                            Text("⭐", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            if (showSavedBadge) {
                SavedBadge(Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

@Composable
private fun SavedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(Spacing.xs)
            .background(
                Color.Black.copy(alpha = 0.5f),
                RoundedCornerShape(Spacing.xs)
            )
            .padding(Spacing.xs)
    ) {
        Icon(
            imageVector = AppIcons.save,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

// MARK: - Empty State
@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
