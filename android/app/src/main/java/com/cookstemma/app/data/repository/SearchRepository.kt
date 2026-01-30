package com.cookstemma.app.data.repository

import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.domain.model.*
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

// UI-friendly search results (transformed from API response)
data class SearchResults(
    val recipes: List<RecipeSummary> = emptyList(),
    val logs: List<FeedItem> = emptyList(),
    val users: List<UserSummary> = emptyList(),
    val hashtags: List<HashtagResult> = emptyList()
)

// API Response model matching backend
data class UnifiedSearchResponse(
    val content: List<SearchResultItem> = emptyList(),
    val counts: SearchCountsResponse? = null,
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
    val nextCursor: String? = null
)

data class SearchCountsResponse(
    val recipes: Int = 0,
    val logs: Int = 0,
    val hashtags: Int = 0,
    val total: Int = 0
)

data class SearchResultItem(
    val type: String,
    val relevanceScore: Double? = null,
    val data: Map<String, Any?>? = null
)

data class HashtagResult(
    @SerializedName("publicId")
    val id: String = "",
    val name: String = "",
    @SerializedName("totalCount")
    val postCount: Int = 0
) {
    val tag: String get() = name
}

data class HashtagSearchResult(
    @SerializedName("publicId")
    val id: String = "",
    val name: String = "",
    val recipeCount: Int = 0,
    val logCount: Int = 0,
    val sampleThumbnails: List<String>? = null
) {
    val totalCount: Int get() = recipeCount + logCount
}

class SearchRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun search(
        query: String,
        type: SearchType? = null,
        cursor: String? = null
    ): Flow<Result<SearchResults>> = flow {
        try {
            val response = apiService.search(query, type?.value, cursor)
            val results = transformSearchResponse(response)
            emit(Result.Success(results))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    private fun transformSearchResponse(response: UnifiedSearchResponse): SearchResults {
        val recipes = mutableListOf<RecipeSummary>()
        val logs = mutableListOf<FeedItem>()
        val hashtags = mutableListOf<HashtagResult>()

        response.content.forEach { item ->
            val data = item.data ?: return@forEach
            when (item.type) {
                "RECIPE" -> {
                    try {
                        val recipe = RecipeSummary(
                            id = (data["publicId"] as? String) ?: "",
                            title = (data["title"] as? String) ?: "",
                            description = data["description"] as? String,
                            foodName = (data["foodName"] as? String) ?: "",
                            cookingStyle = data["cookingStyle"] as? String,
                            userName = (data["userName"] as? String) ?: "",
                            thumbnail = data["thumbnail"] as? String,
                            variantCount = (data["variantCount"] as? Double)?.toInt() ?: 0,
                            logCount = (data["logCount"] as? Double)?.toInt() ?: 0,
                            servings = (data["servings"] as? Double)?.toInt(),
                            cookingTimeRange = data["cookingTimeRange"] as? String,
                            isPrivate = (data["isPrivate"] as? Boolean) ?: false
                        )
                        recipes.add(recipe)
                    } catch (e: Exception) { /* Skip malformed item */ }
                }
                "LOG" -> {
                    try {
                        val log = FeedItem(
                            id = (data["publicId"] as? String) ?: "",
                            title = data["title"] as? String,
                            content = data["content"] as? String,
                            rating = (data["rating"] as? Double)?.toInt(),
                            thumbnailUrl = data["thumbnailUrl"] as? String,
                            creatorPublicId = (data["creatorPublicId"] as? String) ?: "",
                            userName = (data["userName"] as? String) ?: "",
                            foodName = data["foodName"] as? String,
                            recipeTitle = data["recipeTitle"] as? String,
                            hashtags = (data["hashtags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            isVariant = data["isVariant"] as? Boolean,
                            isPrivate = data["isPrivate"] as? Boolean,
                            commentCount = (data["commentCount"] as? Double)?.toInt(),
                            cookingStyle = data["cookingStyle"] as? String
                        )
                        logs.add(log)
                    } catch (e: Exception) { /* Skip malformed item */ }
                }
                "HASHTAG" -> {
                    try {
                        val hashtag = HashtagResult(
                            id = (data["publicId"] as? String) ?: "",
                            name = (data["name"] as? String) ?: "",
                            postCount = ((data["recipeCount"] as? Double)?.toInt() ?: 0) +
                                       ((data["logCount"] as? Double)?.toInt() ?: 0)
                        )
                        hashtags.add(hashtag)
                    } catch (e: Exception) { /* Skip malformed item */ }
                }
            }
        }

        return SearchResults(
            recipes = recipes,
            logs = logs,
            users = emptyList(), // Users not included in unified search
            hashtags = hashtags
        )
    }

    fun searchRecipes(
        query: String,
        cursor: String? = null
    ): Flow<Result<PaginatedResponse<RecipeSummary>>> = flow {
        try {
            val response = apiService.search(query, SearchType.RECIPES.value, cursor)
            val recipes = extractRecipesFromResponse(response)
            emit(Result.Success(PaginatedResponse(
                content = recipes,
                nextCursor = response.nextCursor,
                hasMore = response.hasNext
            )))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    private fun extractRecipesFromResponse(response: UnifiedSearchResponse): List<RecipeSummary> {
        return response.content.mapNotNull { item ->
            if (item.type != "RECIPE") return@mapNotNull null
            val data = item.data ?: return@mapNotNull null
            try {
                RecipeSummary(
                    id = (data["publicId"] as? String) ?: "",
                    title = (data["title"] as? String) ?: "",
                    description = data["description"] as? String,
                    foodName = (data["foodName"] as? String) ?: "",
                    cookingStyle = data["cookingStyle"] as? String,
                    userName = (data["userName"] as? String) ?: "",
                    thumbnail = data["thumbnail"] as? String,
                    variantCount = (data["variantCount"] as? Double)?.toInt() ?: 0,
                    logCount = (data["logCount"] as? Double)?.toInt() ?: 0,
                    servings = (data["servings"] as? Double)?.toInt(),
                    cookingTimeRange = data["cookingTimeRange"] as? String,
                    isPrivate = (data["isPrivate"] as? Boolean) ?: false
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun searchLogs(
        query: String,
        cursor: String? = null
    ): Flow<Result<PaginatedResponse<FeedItem>>> = flow {
        try {
            val response = apiService.search(query, SearchType.LOGS.value, cursor)
            val logs = extractLogsFromResponse(response)
            emit(Result.Success(PaginatedResponse(
                content = logs,
                nextCursor = response.nextCursor,
                hasMore = response.hasNext
            )))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    private fun extractLogsFromResponse(response: UnifiedSearchResponse): List<FeedItem> {
        return response.content.mapNotNull { item ->
            if (item.type != "LOG") return@mapNotNull null
            val data = item.data ?: return@mapNotNull null
            try {
                FeedItem(
                    id = (data["publicId"] as? String) ?: "",
                    title = data["title"] as? String,
                    content = data["content"] as? String,
                    rating = (data["rating"] as? Double)?.toInt(),
                    thumbnailUrl = data["thumbnailUrl"] as? String,
                    creatorPublicId = (data["creatorPublicId"] as? String) ?: "",
                    userName = (data["userName"] as? String) ?: "",
                    foodName = data["foodName"] as? String,
                    recipeTitle = data["recipeTitle"] as? String,
                    hashtags = (data["hashtags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    isVariant = data["isVariant"] as? Boolean,
                    isPrivate = data["isPrivate"] as? Boolean,
                    commentCount = (data["commentCount"] as? Double)?.toInt(),
                    cookingStyle = data["cookingStyle"] as? String
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun searchUsers(
        query: String,
        cursor: String? = null
    ): Flow<Result<PaginatedResponse<UserSummary>>> = flow {
        try {
            val response = apiService.search(query, SearchType.USERS.value, cursor)
            val users = extractUsersFromResponse(response)
            emit(Result.Success(PaginatedResponse(
                content = users,
                nextCursor = response.nextCursor,
                hasMore = response.hasNext
            )))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    private fun extractUsersFromResponse(response: UnifiedSearchResponse): List<UserSummary> {
        return response.content.mapNotNull { item ->
            if (item.type != "USER") return@mapNotNull null
            val data = item.data ?: return@mapNotNull null
            try {
                UserSummary(
                    id = (data["publicId"] as? String) ?: "",
                    username = data["username"] as? String,
                    displayName = data["displayName"] as? String,
                    avatarUrl = data["profileImageUrl"] as? String
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getTrendingHashtags(): Flow<Result<List<HashtagResult>>> = flow {
        try {
            val response = apiService.getTrendingHashtags()
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getHashtagPosts(
        hashtag: String,
        cursor: String? = null
    ): Flow<Result<PaginatedResponse<FeedItem>>> = flow {
        try {
            val response = apiService.getHashtagPosts(hashtag, cursor)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}

enum class SearchType(val value: String) {
    RECIPES("recipes"),
    LOGS("logs"),
    USERS("users"),
    HASHTAGS("hashtags")
}
