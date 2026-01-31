package com.cookstemma.app.util

import com.cookstemma.app.domain.model.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Factory for creating test data in E2E tests.
 */
object TestDataFactory {

    // MARK: - Users

    fun userSummary(
        id: String = UUID.randomUUID().toString(),
        username: String = "testuser",
        displayName: String = "Test User",
        avatarUrl: String? = null
    ) = UserSummary(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl
    )

    fun userProfile(
        id: String = UUID.randomUUID().toString(),
        username: String = "testuser",
        isFollowing: Boolean = false,
        isBlocked: Boolean = false
    ) = UserProfile(
        id = id,
        username = username,
        displayName = "Test User",
        avatarUrl = null,
        bio = "Test bio",
        level = 10,
        recipeCount = 5,
        logCount = 20,
        followerCount = 100,
        followingCount = 50,
        socialLinks = null,
        isFollowing = isFollowing,
        isFollowedBy = false,
        isBlocked = isBlocked,
        createdAt = LocalDateTime.now()
    )

    // MARK: - Recipes

    fun recipeSummary(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Recipe",
        cookCount: Int = 10,
        isSaved: Boolean = false
    ) = RecipeSummary(
        id = id,
        title = title,
        description = "A test recipe description",
        coverImageUrl = null,
        cookingTimeRange = CookingTimeRange.BETWEEN_15_AND_30,
        servings = 2,
        cookCount = cookCount,
        averageRating = 4.5,
        author = userSummary(),
        isSaved = isSaved,
        category = null,
        createdAt = LocalDateTime.now()
    )

    fun recipeDetail(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Recipe",
        isSaved: Boolean = false
    ) = RecipeDetail(
        id = id,
        title = title,
        description = "A detailed test recipe",
        coverImageUrl = null,
        images = emptyList(),
        cookingTimeRange = CookingTimeRange.BETWEEN_30_AND_60,
        servings = 4,
        cookCount = 50,
        saveCount = 25,
        averageRating = 4.2,
        author = userSummary(),
        ingredients = listOf(
            Ingredient("Flour", "2 cups", IngredientCategory.MAIN),
            Ingredient("Sugar", "1 cup", IngredientCategory.MAIN),
            Ingredient("Salt", "1 tsp", IngredientCategory.SEASONING)
        ),
        steps = listOf(
            RecipeStep(1, "Mix dry ingredients", null, null),
            RecipeStep(2, "Add wet ingredients", null, "Mix slowly")
        ),
        hashtags = listOf("baking", "easy"),
        isSaved = isSaved,
        category = null,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    // MARK: - Cooking Logs

    fun cookingLogSummary(
        id: String = UUID.randomUUID().toString(),
        rating: Int = 4,
        isLiked: Boolean = false,
        isSaved: Boolean = false
    ) = CookingLogSummary(
        id = id,
        author = userSummary(),
        images = listOf(LogImage("https://example.com/thumb.jpg", "https://example.com/original.jpg")),
        rating = rating,
        contentPreview = "This was delicious!",
        recipe = null,
        likeCount = 10,
        commentCount = 5,
        isLiked = isLiked,
        isSaved = isSaved,
        createdAt = LocalDateTime.now()
    )

    fun feedItem(
        log: CookingLogSummary? = null,
        recipe: RecipeSummary? = null
    ): FeedItem {
        return when {
            log != null -> FeedItem.Log(log)
            recipe != null -> FeedItem.Recipe(recipe)
            else -> FeedItem.Log(cookingLogSummary())
        }
    }

    // MARK: - Feed

    fun feedItems(count: Int = 10): List<FeedItem> {
        return (0 until count).map { index ->
            if (index % 4 == 0) {
                FeedItem.Recipe(recipeSummary(id = "recipe-$index"))
            } else {
                FeedItem.Log(cookingLogSummary(id = "log-$index"))
            }
        }
    }

    // MARK: - Notifications

    fun notification(
        id: String = UUID.randomUUID().toString(),
        type: NotificationType = NotificationType.NEW_FOLLOWER,
        isRead: Boolean = false
    ) = Notification(
        id = id,
        type = type,
        actorUser = userSummary(),
        targetLogId = if (type == NotificationType.LOG_LIKED) "log-1" else null,
        targetRecipeId = if (type == NotificationType.RECIPE_COOKED) "recipe-1" else null,
        isRead = isRead,
        createdAt = LocalDateTime.now().toString()
    )

    // MARK: - Search

    fun searchResponse(
        recipes: List<RecipeSummary> = emptyList(),
        logs: List<CookingLogSummary> = emptyList(),
        users: List<UserSummary> = emptyList()
    ) = SearchResponse(
        recipes = recipes,
        logs = logs,
        users = users,
        hashtags = emptyList()
    )

    // MARK: - Paginated Responses

    fun <T> paginatedResponse(
        content: List<T>,
        nextCursor: String? = null,
        hasMore: Boolean = false
    ) = PaginatedResponse(
        content = content,
        nextCursor = nextCursor,
        hasMore = hasMore
    )
}
