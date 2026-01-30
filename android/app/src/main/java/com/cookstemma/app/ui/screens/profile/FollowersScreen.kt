package com.cookstemma.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.cookstemma.app.data.repository.UserRepository
import com.cookstemma.app.domain.model.PaginatedResponse
import com.cookstemma.app.domain.model.Result
import com.cookstemma.app.domain.model.UserSummary
import com.cookstemma.app.ui.components.IconEmptyState
import com.cookstemma.app.ui.components.AppIcons
import com.cookstemma.app.ui.components.FollowIconButton
import com.cookstemma.app.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FollowersTab { FOLLOWERS, FOLLOWING }

data class FollowersUiState(
    val userId: String = "",
    val selectedTab: FollowersTab = FollowersTab.FOLLOWERS,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val followers: List<UserSummary> = emptyList(),
    val following: List<UserSummary> = emptyList(),
    val followersCursor: String? = null,
    val followingCursor: String? = null,
    val followersHasMore: Boolean = false,
    val followingHasMore: Boolean = false,
    val error: String? = null,
    val followingInProgress: Set<String> = emptySet()
)

@HiltViewModel
class FollowersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(FollowersUiState(userId = userId))
    val uiState: StateFlow<FollowersUiState> = _uiState.asStateFlow()

    init {
        loadFollowers()
        loadFollowing()
    }

    fun selectTab(tab: FollowersTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun loadFollowers(cursor: String? = null) {
        viewModelScope.launch {
            if (cursor == null) {
                _uiState.update { it.copy(isLoading = true) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true) }
            }

            userRepository.getFollowers(userId, cursor).collect { result ->
                when (result) {
                    is Result.Loading -> { }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                followers = if (cursor == null) {
                                    result.data.content
                                } else {
                                    it.followers + result.data.content
                                },
                                followersCursor = result.data.nextCursor,
                                followersHasMore = result.data.hasMore
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
                }
            }
        }
    }

    private fun loadFollowing(cursor: String? = null) {
        viewModelScope.launch {
            userRepository.getFollowing(userId, cursor).collect { result ->
                when (result) {
                    is Result.Loading -> { }
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                following = if (cursor == null) {
                                    result.data.content
                                } else {
                                    it.following + result.data.content
                                },
                                followingCursor = result.data.nextCursor,
                                followingHasMore = result.data.hasMore
                            )
                        }
                    }
                    is Result.Error -> { }
                }
            }
        }
    }

    fun loadMoreFollowers() {
        val cursor = _uiState.value.followersCursor
        if (cursor != null && !_uiState.value.isLoadingMore) {
            loadFollowers(cursor)
        }
    }

    fun loadMoreFollowing() {
        val cursor = _uiState.value.followingCursor
        if (cursor != null && !_uiState.value.isLoadingMore) {
            loadFollowing(cursor)
        }
    }

    fun toggleFollow(user: UserSummary) {
        if (_uiState.value.followingInProgress.contains(user.id)) return

        viewModelScope.launch {
            _uiState.update { it.copy(followingInProgress = it.followingInProgress + user.id) }

            // For simplicity, we'll just call follow - in a real app we'd track following state
            val result = userRepository.followUser(user.id)

            _uiState.update { it.copy(followingInProgress = it.followingInProgress - user.id) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: FollowersViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.selectedTab == FollowersTab.FOLLOWERS) "Followers" else "Following") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.selectedTab == FollowersTab.FOLLOWERS,
                    onClick = { viewModel.selectTab(FollowersTab.FOLLOWERS) },
                    text = { Text("Followers") }
                )
                Tab(
                    selected = uiState.selectedTab == FollowersTab.FOLLOWING,
                    onClick = { viewModel.selectTab(FollowersTab.FOLLOWING) },
                    text = { Text("Following") }
                )
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    val users = when (uiState.selectedTab) {
                        FollowersTab.FOLLOWERS -> uiState.followers
                        FollowersTab.FOLLOWING -> uiState.following
                    }
                    val hasMore = when (uiState.selectedTab) {
                        FollowersTab.FOLLOWERS -> uiState.followersHasMore
                        FollowersTab.FOLLOWING -> uiState.followingHasMore
                    }
                    val onLoadMore = when (uiState.selectedTab) {
                        FollowersTab.FOLLOWERS -> viewModel::loadMoreFollowers
                        FollowersTab.FOLLOWING -> viewModel::loadMoreFollowing
                    }

                    if (users.isEmpty()) {
                        IconEmptyState(
                            icon = AppIcons.followers,
                            subtitle = when (uiState.selectedTab) {
                                FollowersTab.FOLLOWERS -> "No followers yet"
                                FollowersTab.FOLLOWING -> "Not following anyone"
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            items(users, key = { it.id }) { user ->
                                UserListItem(
                                    user = user,
                                    isFollowLoading = uiState.followingInProgress.contains(user.id),
                                    onUserClick = { onNavigateToProfile(user.id) },
                                    onFollowClick = { viewModel.toggleFollow(user) }
                                )
                            }

                            if (hasMore) {
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
                                            TextButton(onClick = onLoadMore) {
                                                Text("Load More")
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
    }
}

@Composable
private fun UserListItem(
    user: UserSummary,
    isFollowLoading: Boolean,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayNameOrUsername,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Follow Button
            FollowIconButton(
                isFollowing = false, // In a real app, track following state per user
                isLoading = isFollowLoading,
                onClick = onFollowClick
            )
        }
    }
}
