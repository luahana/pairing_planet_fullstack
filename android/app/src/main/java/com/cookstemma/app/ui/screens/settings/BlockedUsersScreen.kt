package com.cookstemma.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.cookstemma.app.data.api.BlockedUser
import com.cookstemma.app.data.repository.UserRepository
import com.cookstemma.app.domain.model.Result
import com.cookstemma.app.ui.components.IconEmptyState
import com.cookstemma.app.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel
data class BlockedUsersUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val blockedUsers: List<BlockedUser> = emptyList(),
    val hasMore: Boolean = false,
    val currentPage: Int = 0,
    val error: String? = null
)

@HiltViewModel
class BlockedUsersViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlockedUsersUiState())
    val uiState: StateFlow<BlockedUsersUiState> = _uiState.asStateFlow()

    init {
        loadBlockedUsers()
    }

    fun loadBlockedUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = userRepository.getBlockedUsers(page = 0)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            blockedUsers = result.data.content,
                            hasMore = result.data.hasMore,
                            currentPage = result.data.page
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                is Result.Loading -> { }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = _uiState.value.currentPage + 1
            when (val result = userRepository.getBlockedUsers(page = nextPage)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            blockedUsers = it.blockedUsers + result.data.content,
                            hasMore = result.data.hasMore,
                            currentPage = result.data.page
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
                is Result.Loading -> { }
            }
        }
    }

    fun unblockUser(user: BlockedUser) {
        viewModelScope.launch {
            // Optimistic update
            _uiState.update { it.copy(blockedUsers = it.blockedUsers.filter { u -> u.id != user.id }) }
            when (val result = userRepository.unblockUser(user.id)) {
                is Result.Error -> {
                    // Revert on error
                    _uiState.update { it.copy(blockedUsers = it.blockedUsers + user) }
                }
                else -> { }
            }
        }
    }
}

// Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(
    onNavigateBack: () -> Unit,
    viewModel: BlockedUsersViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blocked Users") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Button(onClick = { viewModel.loadBlockedUsers() }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.blockedUsers.isEmpty() -> {
                    IconEmptyState(
                        icon = Icons.Default.Block,
                        subtitle = "You haven't blocked anyone",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        items(uiState.blockedUsers, key = { it.id }) { user ->
                            BlockedUserItem(
                                user = user,
                                onUnblock = { viewModel.unblockUser(user) }
                            )
                        }

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
                                        TextButton(onClick = { viewModel.loadMore() }) {
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

@Composable
private fun BlockedUserItem(
    user: BlockedUser,
    onUnblock: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
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

            // Username
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Blocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Unblock button
            OutlinedButton(
                onClick = onUnblock,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Unblock")
            }
        }
    }
}
