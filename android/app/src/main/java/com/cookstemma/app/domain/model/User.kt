package com.cookstemma.app.domain.model

import java.time.LocalDateTime

data class UserSummary(
    val id: String,
    val username: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null
) {
    val displayNameOrUsername: String get() = displayName ?: username ?: ""
}

data class MyProfile(
    val id: String = "",
    val username: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val level: Int = 0,
    val levelName: String? = null,
    val xp: Int = 0,
    val levelProgress: Double = 0.0,
    val recipeCount: Int = 0,
    val logCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val savedCount: Int = 0,
    val socialLinks: SocialLinks? = null,
    val youtubeUrl: String? = null,
    val instagramHandle: String? = null,
    val createdAt: LocalDateTime? = null
) {
    val localizedLevelName: String get() = LevelName.displayName(levelName)
}

data class UserProfile(
    val id: String = "",
    val username: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val level: Int = 0,
    val levelName: String? = null,
    val recipeCount: Int = 0,
    val logCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val socialLinks: SocialLinks? = null,
    val youtubeUrl: String? = null,
    val instagramHandle: String? = null,
    val isFollowing: Boolean = false,
    val isFollowedBy: Boolean = false,
    val isBlocked: Boolean = false,
    val createdAt: LocalDateTime? = null
) {
    val localizedLevelName: String get() = LevelName.displayName(levelName)
}

data class SocialLinks(
    val youtube: String?,
    val instagram: String?,
    val tiktok: String?,
    val website: String?
)

data class UpdateProfileRequest(
    val displayName: String?,
    val bio: String?,
    val avatarUrl: String?,
    val socialLinks: SocialLinks?
)

enum class ReportReason(val value: String) {
    SPAM("spam"),
    HARASSMENT("harassment"),
    INAPPROPRIATE("inappropriate"),
    OTHER("other")
}

/**
 * Maps backend level names to localized display names
 */
object LevelName {
    fun displayName(key: String?): String {
        if (key == null) return "Beginner"
        return when (key) {
            "beginner" -> "Beginner"
            "noviceCook" -> "Novice Cook"
            "homeCook" -> "Home Cook"
            "hobbyCook" -> "Hobby Cook"
            "skilledCook" -> "Skilled Cook"
            "expertCook" -> "Expert Cook"
            "juniorChef" -> "Junior Chef"
            "sousChef" -> "Sous Chef"
            "chef" -> "Chef"
            "headChef" -> "Head Chef"
            "executiveChef" -> "Executive Chef"
            "masterChef" -> "Master Chef"
            else -> key.replaceFirstChar { it.uppercase() }
        }
    }
}
