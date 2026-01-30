package com.cookstemma.app.domain.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

// Feed log item from /log_posts endpoint - matches API response directly
data class FeedItem(
    @SerializedName("publicId")
    val id: String,
    val title: String?,
    val content: String?,
    val rating: Int?,
    val thumbnailUrl: String?,
    val creatorPublicId: String,
    val userName: String,
    val foodName: String?,
    val recipeTitle: String?,
    val hashtags: List<String> = emptyList(),
    val isVariant: Boolean?,
    val isPrivate: Boolean?,
    val commentCount: Int?,
    val cookingStyle: String?
)

// Full log summary with nested author/recipe (used in other contexts)
data class CookingLogSummary(
    @SerializedName("publicId")
    val id: String,
    val author: UserSummary? = null,
    val images: List<LogImage>? = null,
    val rating: Int = 0,
    val contentPreview: String? = null,
    val content: String? = null,
    val recipe: RecipeSummary? = null,
    val hashtags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val createdAt: LocalDateTime? = null
)

// Log detail from /log_posts/{id} endpoint - matches API response
data class CookingLogDetail(
    @SerializedName("publicId")
    val id: String,
    val title: String?,
    val rating: Int,
    val content: String?,
    @SerializedName("images")
    val logImages: List<LogImageInfo> = emptyList(),
    val linkedRecipe: LinkedRecipeSummary?,
    val commentCount: Int,
    val isSavedByCurrentUser: Boolean?,
    @SerializedName("hashtags")
    val hashtagObjects: List<HashtagInfo> = emptyList(),
    val isPrivate: Boolean,
    val creatorPublicId: String,
    val userName: String,
    val createdAt: String, // Keep as String to avoid date parsing issues
    // Mutable state for UI (not from API)
    val isLiked: Boolean = false,
    val likeCount: Int = 0,
    val isSaved: Boolean = false
) {
    // Computed properties for compatibility
    val author: UserSummary get() = UserSummary(
        id = creatorPublicId,
        username = userName,
        displayName = null,
        avatarUrl = null
    )
    val images: List<LogImage> get() = logImages.map {
        LogImage(id = it.imagePublicId, url = it.imageUrl, thumbnailUrl = it.imageUrl)
    }
    val hashtags: List<String> get() = hashtagObjects.map { it.name }
    val recipe: LinkedRecipeSummary? get() = linkedRecipe

    // Initialize isSaved from API response
    fun withSavedState(): CookingLogDetail = copy(isSaved = isSavedByCurrentUser ?: false)
}

data class LogImageInfo(
    val imagePublicId: String,
    val imageUrl: String
)

data class HashtagInfo(
    val publicId: String? = null,
    val name: String
)

data class LinkedRecipeSummary(
    @SerializedName("publicId")
    val id: String,
    val title: String,
    val description: String?,
    val foodName: String,
    val cookingStyle: String?,
    val userName: String,
    val thumbnail: String?,
    val variantCount: Int,
    val logCount: Int,
    val servings: Int?,
    val cookingTimeRange: String?,
    val hashtags: List<String> = emptyList(),
    val isPrivate: Boolean
)

// Image info for log summary (matches API ImageInfo structure)
data class LogImage(
    @SerializedName("publicId")
    val id: String? = null,
    val url: String? = null,
    val thumbnailUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null
) {
    // Computed property for compatibility
    val originalUrl: String? get() = url
}

data class CreateLogRequest(
    val rating: Int,
    val content: String?,
    val imageIds: List<String>,
    val recipeId: String?,
    val hashtags: List<String>,
    val isPrivate: Boolean
)

// Alias for paginated results - uses Summary format
typealias CookingLog = CookingLogSummary

// Recipe log item from /log_posts/recipe/{id} endpoint - matches API response
// Uses same structure as FeedItem
typealias RecipeLogItem = FeedItem
