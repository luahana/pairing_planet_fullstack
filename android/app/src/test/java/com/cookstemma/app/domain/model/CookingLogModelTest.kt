package com.cookstemma.app.domain.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CookingLogModelTest {

    private lateinit var gson: Gson

    @Before
    fun setup() {
        gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            .create()
    }

    // MARK: - CookingLogSummary Tests

    @Test
    fun `CookingLogSummary parses from JSON`() {
        // Given
        val json = """
        {
            "id": "log-123",
            "author": {
                "id": "user-1",
                "username": "homecook",
                "displayName": "Home Cook",
                "avatarUrl": "https://example.com/avatar.jpg"
            },
            "images": [
                {"thumbnailUrl": "https://example.com/thumb1.jpg", "originalUrl": "https://example.com/img1.jpg"},
                {"thumbnailUrl": "https://example.com/thumb2.jpg", "originalUrl": "https://example.com/img2.jpg"}
            ],
            "rating": 4,
            "contentPreview": "Made this for Sunday dinner...",
            "recipe": {
                "id": "recipe-1",
                "title": "Kimchi Fried Rice",
                "description": "Delicious Korean dish",
                "coverImageUrl": "https://example.com/recipe.jpg",
                "cookingTimeRange": "BETWEEN_15_AND_30_MIN",
                "servings": 2,
                "cookCount": 156,
                "averageRating": 4.3,
                "author": {
                    "id": "user-2",
                    "username": "chefkim",
                    "displayName": "Chef Kim",
                    "avatarUrl": null
                },
                "isSaved": true,
                "category": null,
                "createdAt": "2024-01-01T00:00:00"
            },
            "likeCount": 42,
            "commentCount": 8,
            "isLiked": true,
            "isSaved": false,
            "createdAt": "2024-01-15T18:30:00"
        }
        """.trimIndent()

        // When
        val log = gson.fromJson(json, CookingLogSummary::class.java)

        // Then
        assertEquals("log-123", log.id)
        assertEquals("homecook", log.author.username)
        assertEquals(2, log.images.size)
        assertEquals(4, log.rating)
        assertEquals("Made this for Sunday dinner...", log.contentPreview)
        assertEquals("Kimchi Fried Rice", log.recipe?.title)
        assertEquals(42, log.likeCount)
        assertEquals(8, log.commentCount)
        assertTrue(log.isLiked)
        assertFalse(log.isSaved)
    }

    @Test
    fun `CookingLogSummary without linked recipe`() {
        // Given - standalone log
        val json = """
        {
            "id": "log-456",
            "author": {
                "id": "user-1",
                "username": "cook",
                "displayName": null,
                "avatarUrl": null
            },
            "images": [
                {"thumbnailUrl": "https://example.com/thumb.jpg", "originalUrl": "https://example.com/img.jpg"}
            ],
            "rating": 5,
            "contentPreview": "Just cooked something amazing!",
            "recipe": null,
            "likeCount": 10,
            "commentCount": 2,
            "isLiked": false,
            "isSaved": false,
            "createdAt": "2024-01-16T12:00:00"
        }
        """.trimIndent()

        // When
        val log = gson.fromJson(json, CookingLogSummary::class.java)

        // Then
        assertNull(log.recipe)
        assertEquals(5, log.rating)
    }

    @Test
    fun `CookingLogSummary with empty images`() {
        // Given
        val log = CookingLogSummary(
            id = "log-test",
            author = UserSummary("user-1", "user", null, null),
            images = emptyList(),
            rating = 3,
            contentPreview = "Test",
            recipe = null,
            likeCount = 0,
            commentCount = 0,
            isLiked = false,
            isSaved = false,
            createdAt = LocalDateTime.now()
        )

        // Then
        assertTrue(log.images.isEmpty())
    }

    @Test
    fun `CookingLogSummary with max images (5)`() {
        // Given
        val images = (1..5).map { LogImage("thumb$it.jpg", "img$it.jpg") }
        val log = CookingLogSummary(
            id = "log-test",
            author = UserSummary("user-1", "user", null, null),
            images = images,
            rating = 4,
            contentPreview = null,
            recipe = null,
            likeCount = 0,
            commentCount = 0,
            isLiked = false,
            isSaved = false,
            createdAt = LocalDateTime.now()
        )

        // Then
        assertEquals(5, log.images.size)
    }

    // MARK: - CookingLogDetail Tests

    @Test
    fun `CookingLogDetail parses from JSON`() {
        // Given
        val json = """
        {
            "id": "log-789",
            "author": {
                "id": "user-1",
                "username": "weekendchef",
                "displayName": "Weekend Chef",
                "avatarUrl": "https://example.com/avatar.jpg"
            },
            "images": [
                {"thumbnailUrl": "https://example.com/thumb.jpg", "originalUrl": "https://example.com/img.jpg"}
            ],
            "rating": 3,
            "content": "It was okay, could use more seasoning next time.",
            "recipe": null,
            "hashtags": ["homecooking", "dinner", "experiment"],
            "isPrivate": false,
            "likeCount": 5,
            "commentCount": 1,
            "isLiked": false,
            "isSaved": true,
            "createdAt": "2024-01-17T19:00:00"
        }
        """.trimIndent()

        // When
        val log = gson.fromJson(json, CookingLogDetail::class.java)

        // Then
        assertEquals("log-789", log.id)
        assertEquals(3, log.rating)
        assertEquals("It was okay, could use more seasoning next time.", log.content)
        assertEquals(listOf("homecooking", "dinner", "experiment"), log.hashtags)
        assertFalse(log.isPrivate)
        assertTrue(log.isSaved)
    }

    @Test
    fun `CookingLogDetail private log`() {
        // Given
        val log = CookingLogDetail(
            id = "log-private",
            author = UserSummary("user-1", "user", null, null),
            images = emptyList(),
            rating = 2,
            content = "Private experiment",
            recipe = null,
            hashtags = emptyList(),
            isPrivate = true,
            likeCount = 0,
            commentCount = 0,
            isLiked = false,
            isSaved = false,
            createdAt = LocalDateTime.now()
        )

        // Then
        assertTrue(log.isPrivate)
    }

    // MARK: - Rating Validation Tests

    @Test
    fun `rating values in valid range 1-5`() {
        for (rating in 1..5) {
            val log = CookingLogSummary(
                id = "log-$rating",
                author = UserSummary("user-1", "user", null, null),
                images = emptyList(),
                rating = rating,
                contentPreview = null,
                recipe = null,
                likeCount = 0,
                commentCount = 0,
                isLiked = false,
                isSaved = false,
                createdAt = LocalDateTime.now()
            )
            assertEquals(rating, log.rating)
        }
    }

    // MARK: - LogImage Tests

    @Test
    fun `LogImage data class works correctly`() {
        // Given
        val image = LogImage(
            thumbnailUrl = "https://example.com/thumb.jpg",
            originalUrl = "https://example.com/original.jpg"
        )

        // Then
        assertEquals("https://example.com/thumb.jpg", image.thumbnailUrl)
        assertEquals("https://example.com/original.jpg", image.originalUrl)
    }

    // MARK: - FeedItem Tests

    @Test
    fun `FeedItem Log has correct id prefix`() {
        // Given
        val log = CookingLogSummary(
            id = "abc123",
            author = UserSummary("user-1", "user", null, null),
            images = emptyList(),
            rating = 4,
            contentPreview = null,
            recipe = null,
            likeCount = 0,
            commentCount = 0,
            isLiked = false,
            isSaved = false,
            createdAt = LocalDateTime.now()
        )
        val feedItem = FeedItem.Log(log)

        // Then
        assertEquals("log_abc123", feedItem.id)
    }

    @Test
    fun `FeedItem Recipe has correct id prefix`() {
        // Given
        val recipe = RecipeSummary(
            id = "xyz789",
            title = "Test Recipe",
            description = null,
            coverImageUrl = null,
            cookingTimeRange = null,
            servings = null,
            cookCount = 0,
            averageRating = null,
            author = UserSummary("user-1", "user", null, null),
            isSaved = false,
            category = null,
            createdAt = LocalDateTime.now()
        )
        val feedItem = FeedItem.Recipe(recipe)

        // Then
        assertEquals("recipe_xyz789", feedItem.id)
    }

    @Test
    fun `FeedItem sealed class pattern matching`() {
        // Given
        val logItem: FeedItem = FeedItem.Log(
            CookingLogSummary(
                id = "log-1",
                author = UserSummary("user-1", "user", null, null),
                images = emptyList(),
                rating = 4,
                contentPreview = null,
                recipe = null,
                likeCount = 0,
                commentCount = 0,
                isLiked = false,
                isSaved = false,
                createdAt = LocalDateTime.now()
            )
        )

        val recipeItem: FeedItem = FeedItem.Recipe(
            RecipeSummary(
                id = "recipe-1",
                title = "Test",
                description = null,
                coverImageUrl = null,
                cookingTimeRange = null,
                servings = null,
                cookCount = 0,
                averageRating = null,
                author = UserSummary("user-1", "user", null, null),
                isSaved = false,
                category = null,
                createdAt = LocalDateTime.now()
            )
        )

        // Then - pattern matching works
        when (logItem) {
            is FeedItem.Log -> assertTrue(true)
            is FeedItem.Recipe -> assertTrue(false)
        }

        when (recipeItem) {
            is FeedItem.Log -> assertTrue(false)
            is FeedItem.Recipe -> assertTrue(true)
        }
    }

    // MARK: - CreateLogRequest Tests

    @Test
    fun `CreateLogRequest with all fields`() {
        // Given
        val request = CreateLogRequest(
            rating = 5,
            content = "Amazing dish!",
            imageIds = listOf("img-1", "img-2"),
            recipeId = "recipe-123",
            hashtags = listOf("delicious", "homemade"),
            isPrivate = false
        )

        // Then
        assertEquals(5, request.rating)
        assertEquals("Amazing dish!", request.content)
        assertEquals(listOf("img-1", "img-2"), request.imageIds)
        assertEquals("recipe-123", request.recipeId)
        assertEquals(listOf("delicious", "homemade"), request.hashtags)
        assertFalse(request.isPrivate)
    }

    @Test
    fun `CreateLogRequest with null optional fields`() {
        // Given
        val request = CreateLogRequest(
            rating = 3,
            content = null,
            imageIds = listOf("img-1"),
            recipeId = null,
            hashtags = emptyList(),
            isPrivate = true
        )

        // Then
        assertEquals(3, request.rating)
        assertNull(request.content)
        assertNull(request.recipeId)
        assertTrue(request.hashtags.isEmpty())
        assertTrue(request.isPrivate)
    }

    @Test
    fun `CreateLogRequest serializes to JSON`() {
        // Given
        val request = CreateLogRequest(
            rating = 4,
            content = "Great!",
            imageIds = listOf("img-1"),
            recipeId = "recipe-1",
            hashtags = listOf("test"),
            isPrivate = false
        )

        // When
        val json = gson.toJson(request)

        // Then
        assertTrue(json.contains("\"rating\":4"))
        assertTrue(json.contains("\"content\":\"Great!\""))
        assertTrue(json.contains("\"isPrivate\":false"))
    }

    // MARK: - Data Class Equality Tests

    @Test
    fun `CookingLogSummary equals works correctly`() {
        val now = LocalDateTime.now()
        val author = UserSummary("user-1", "user", null, null)

        val log1 = CookingLogSummary(
            id = "log-1",
            author = author,
            images = emptyList(),
            rating = 4,
            contentPreview = "Test",
            recipe = null,
            likeCount = 10,
            commentCount = 2,
            isLiked = true,
            isSaved = false,
            createdAt = now
        )

        val log2 = CookingLogSummary(
            id = "log-1",
            author = author,
            images = emptyList(),
            rating = 4,
            contentPreview = "Test",
            recipe = null,
            likeCount = 10,
            commentCount = 2,
            isLiked = true,
            isSaved = false,
            createdAt = now
        )

        val log3 = log1.copy(id = "log-2")

        assertEquals(log1, log2)
        assertTrue(log1 != log3)
    }

    @Test
    fun `CookingLogSummary copy works correctly`() {
        val now = LocalDateTime.now()
        val author = UserSummary("user-1", "user", null, null)

        val original = CookingLogSummary(
            id = "log-1",
            author = author,
            images = emptyList(),
            rating = 4,
            contentPreview = "Test",
            recipe = null,
            likeCount = 10,
            commentCount = 2,
            isLiked = false,
            isSaved = false,
            createdAt = now
        )

        // Simulate optimistic like update
        val updated = original.copy(isLiked = true, likeCount = 11)

        assertEquals(false, original.isLiked)
        assertEquals(10, original.likeCount)
        assertEquals(true, updated.isLiked)
        assertEquals(11, updated.likeCount)
    }

    // MARK: - Helper

    private class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): LocalDateTime? {
            return json?.asString?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        }
    }
}
