package com.cookstemma.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.repository.UserRepository
import com.cookstemma.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProfileTab { RECIPES, LOGS, SAVED }

enum class VisibilityFilter(val title: String) {
    ALL("All"),
    PUBLIC("Public"),
    PRIVATE("Private")
}

enum class SavedContentFilter(val title: String) {
    ALL("All"),
    RECIPES("Recipes"),
    LOGS("Logs")
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isLoadingContent: Boolean = false,
    val error: String? = null,
    val userId: String? = null,
    val avatarUrl: String? = null,
    val username: String = "",
    val displayName: String? = null,
    val bio: String? = null,
    val level: Int? = null,
    val levelName: String? = null,
    val levelProgress: Double? = null,
    val recipeCount: Int = 0,
    val logCount: Int = 0,
    val savedCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Boolean = false,
    val isBlocked: Boolean = false,
    val socialLinks: SocialLinks? = null,
    val youtubeUrl: String? = null,
    val instagramHandle: String? = null,
    val selectedTab: ProfileTab = ProfileTab.RECIPES,
    val visibilityFilter: VisibilityFilter = VisibilityFilter.ALL,
    val savedContentFilter: SavedContentFilter = SavedContentFilter.ALL,
    val recipes: List<RecipeSummary> = emptyList(),
    val logs: List<CookingLogSummary> = emptyList(),
    val savedRecipes: List<RecipeSummary> = emptyList(),
    val savedLogs: List<CookingLog> = emptyList(),
    val blockSuccess: Boolean = false,
    val reportSuccess: Boolean = false
) {
    val localizedLevelName: String get() = LevelName.displayName(levelName)
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var isOwnProfile: Boolean = false

    fun loadProfile(userId: String?) {
        currentUserId = userId
        isOwnProfile = userId == null

        viewModelScope.launch {
            val profileFlow = if (isOwnProfile) userRepository.getMyProfile() else userRepository.getUserProfile(userId!!)
            profileFlow.collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        when (val data = result.data) {
                            is MyProfile -> _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    userId = data.id,
                                    avatarUrl = data.avatarUrl,
                                    username = data.username ?: "",
                                    displayName = data.displayName,
                                    bio = data.bio,
                                    level = data.level,
                                    levelName = data.levelName,
                                    levelProgress = data.levelProgress,
                                    recipeCount = data.recipeCount,
                                    logCount = data.logCount,
                                    savedCount = data.savedCount,
                                    followerCount = data.followerCount,
                                    followingCount = data.followingCount,
                                    socialLinks = data.socialLinks,
                                    youtubeUrl = data.youtubeUrl,
                                    instagramHandle = data.instagramHandle
                                )
                            }
                            is UserProfile -> _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    userId = data.id,
                                    avatarUrl = data.avatarUrl,
                                    username = data.username ?: "",
                                    displayName = data.displayName,
                                    bio = data.bio,
                                    level = data.level,
                                    levelName = data.levelName,
                                    recipeCount = data.recipeCount,
                                    logCount = data.logCount,
                                    followerCount = data.followerCount,
                                    followingCount = data.followingCount,
                                    isFollowing = data.isFollowing,
                                    isBlocked = data.isBlocked,
                                    socialLinks = data.socialLinks,
                                    youtubeUrl = data.youtubeUrl,
                                    instagramHandle = data.instagramHandle
                                )
                            }
                        }
                        loadContent()
                    }
                    is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
            }
        }
    }

    fun selectTab(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        loadContent()
    }

    fun toggleFollow() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            val wasFollowing = _uiState.value.isFollowing
            _uiState.update { it.copy(isFollowing = !wasFollowing, followerCount = it.followerCount + if (wasFollowing) -1 else 1) }
            val result = if (wasFollowing) userRepository.unfollowUser(userId) else userRepository.followUser(userId)
            if (result is Result.Error) {
                _uiState.update { it.copy(isFollowing = wasFollowing, followerCount = it.followerCount + if (wasFollowing) 1 else -1) }
            }
        }
    }

    fun setVisibilityFilter(filter: VisibilityFilter) {
        _uiState.update { it.copy(visibilityFilter = filter) }
        loadContent()
    }

    fun setSavedContentFilter(filter: SavedContentFilter) {
        _uiState.update { it.copy(savedContentFilter = filter) }
        loadSavedContent()
    }

    fun loadContent() {
        val userId = currentUserId ?: _uiState.value.userId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingContent = true) }
            when (_uiState.value.selectedTab) {
                ProfileTab.RECIPES -> {
                    userRepository.getUserRecipes(userId, null).collect { result ->
                        if (result is Result.Success) {
                            _uiState.update { it.copy(recipes = result.data.content, isLoadingContent = false) }
                        } else {
                            _uiState.update { it.copy(isLoadingContent = false) }
                        }
                    }
                }
                ProfileTab.LOGS -> {
                    userRepository.getUserLogs(userId, null).collect { result ->
                        if (result is Result.Success) {
                            _uiState.update { it.copy(logs = result.data.content, isLoadingContent = false) }
                        } else {
                            _uiState.update { it.copy(isLoadingContent = false) }
                        }
                    }
                }
                ProfileTab.SAVED -> {
                    loadSavedContent()
                }
            }
        }
    }

    private fun loadSavedContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingContent = true) }
            // Load saved recipes
            userRepository.getSavedRecipes(null).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(savedRecipes = result.data.content) }
                }
            }
            // Load saved logs
            userRepository.getSavedLogs(null).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(savedLogs = result.data.content, isLoadingContent = false) }
                } else {
                    _uiState.update { it.copy(isLoadingContent = false) }
                }
            }
        }
    }

    fun blockUser() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            val wasBlocked = _uiState.value.isBlocked
            // Optimistic update
            _uiState.update { it.copy(isBlocked = !wasBlocked) }
            
            val result = if (wasBlocked) {
                userRepository.unblockUser(userId)
            } else {
                userRepository.blockUser(userId)
            }
            
            when (result) {
                is Result.Success -> {
                    if (!wasBlocked) {
                        _uiState.update { it.copy(blockSuccess = true) }
                    }
                }
                is Result.Error -> {
                    // Revert on failure
                    _uiState.update { it.copy(isBlocked = wasBlocked) }
                }
                else -> {}
            }
        }
    }

    fun reportUser(reason: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            val result = userRepository.reportUser(userId, reason)
            if (result is Result.Success) {
                _uiState.update { it.copy(reportSuccess = true) }
            }
        }
    }

    fun clearBlockSuccess() {
        _uiState.update { it.copy(blockSuccess = false) }
    }

    fun clearReportSuccess() {
        _uiState.update { it.copy(reportSuccess = false) }
    }
}
