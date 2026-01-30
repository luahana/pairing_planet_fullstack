package com.cookstemma.app.data.repository

import app.cash.turbine.test
import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.domain.model.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeedRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: FeedRepository

    @Before
    fun setup() {
        apiService = mockk(relaxed = true)
        repository = FeedRepository(apiService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // MARK: - getFeed Tests

    @Test
    fun `getFeed emits loading then success`() = runTest {
        // Given
        val feedItems = listOf<FeedItem>(
            FeedItem.Log(createMockLogSummary()),
            FeedItem.Recipe(createMockRecipeSummary())
        )
        val response = PaginatedResponse(
            content = feedItems,
            nextCursor = "cursor123",
            hasMore = true
        )
        coEvery { apiService.getFeed(any()) } returns response

        // When / Then
        repository.getFeed(null).test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()?.content?.size)
            assertTrue(result.getOrNull()?.hasMore ?: false)

            awaitComplete()
        }
    }

    @Test
    fun `getFeed with cursor fetches next page`() = runTest {
        // Given
        val response = PaginatedResponse<FeedItem>(
            content = emptyList(),
            nextCursor = null,
            hasMore = false
        )
        coEvery { apiService.getFeed(any()) } returns response

        // When
        repository.getFeed("page2").test {
            awaitItem() // Loading
            awaitItem() // Result
            awaitComplete()
        }

        // Then
        coVerify { apiService.getFeed("page2") }
    }

    @Test
    fun `getFeed network error emits error`() = runTest {
        // Given
        coEvery { apiService.getFeed(any()) } throws IOException("No internet")

        // When / Then
        repository.getFeed(null).test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isError)
            assertEquals("No internet", result.exceptionOrNull()?.message)

            awaitComplete()
        }
    }

    @Test
    fun `getFeed empty feed returns success with empty content`() = runTest {
        // Given
        val response = PaginatedResponse<FeedItem>(
            content = emptyList(),
            nextCursor = null,
            hasMore = false
        )
        coEvery { apiService.getFeed(any()) } returns response

        // When / Then
        repository.getFeed(null).test {
            awaitItem() // Loading

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()?.content?.isEmpty() ?: false)

            awaitComplete()
        }
    }

    // MARK: - getLog Tests

    @Test
    fun `getLog emits loading then success`() = runTest {
        // Given
        val log = createMockLogDetail()
        coEvery { apiService.getLog("log-123") } returns log

        // When / Then
        repository.getLog("log-123").test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals("log-123", result.getOrNull()?.id)
            assertEquals(4, result.getOrNull()?.rating)

            awaitComplete()
        }
    }

    @Test
    fun `getLog not found emits error`() = runTest {
        // Given
        coEvery { apiService.getLog("non-existent") } throws IOException("Not found")

        // When / Then
        repository.getLog("non-existent").test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isError)

            awaitComplete()
        }
    }

    // MARK: - likeLog Tests

    @Test
    fun `likeLog success returns success`() = runTest {
        // Given
        coEvery { apiService.likeLog("log-123") } just Runs

        // When
        val result = repository.likeLog("log-123")

        // Then
        assertTrue(result.isSuccess)
        coVerify { apiService.likeLog("log-123") }
    }

    @Test
    fun `likeLog error returns error`() = runTest {
        // Given
        coEvery { apiService.likeLog("log-123") } throws IOException("Like failed")

        // When
        val result = repository.likeLog("log-123")

        // Then
        assertTrue(result.isError)
        assertEquals("Like failed", result.exceptionOrNull()?.message)
    }

    // MARK: - unlikeLog Tests

    @Test
    fun `unlikeLog success returns success`() = runTest {
        // Given
        coEvery { apiService.unlikeLog("log-123") } just Runs

        // When
        val result = repository.unlikeLog("log-123")

        // Then
        assertTrue(result.isSuccess)
        coVerify { apiService.unlikeLog("log-123") }
    }

    @Test
    fun `unlikeLog error returns error`() = runTest {
        // Given
        coEvery { apiService.unlikeLog("log-123") } throws IOException("Unlike failed")

        // When
        val result = repository.unlikeLog("log-123")

        // Then
        assertTrue(result.isError)
    }

    // MARK: - saveLog Tests

    @Test
    fun `saveLog success returns success`() = runTest {
        // Given
        coEvery { apiService.saveLog("log-123") } just Runs

        // When
        val result = repository.saveLog("log-123")

        // Then
        assertTrue(result.isSuccess)
        coVerify { apiService.saveLog("log-123") }
    }

    @Test
    fun `saveLog error returns error`() = runTest {
        // Given
        coEvery { apiService.saveLog("log-123") } throws IOException("Save failed")

        // When
        val result = repository.saveLog("log-123")

        // Then
        assertTrue(result.isError)
    }

    // MARK: - unsaveLog Tests

    @Test
    fun `unsaveLog success returns success`() = runTest {
        // Given
        coEvery { apiService.unsaveLog("log-123") } just Runs

        // When
        val result = repository.unsaveLog("log-123")

        // Then
        assertTrue(result.isSuccess)
        coVerify { apiService.unsaveLog("log-123") }
    }

    // MARK: - createLog Tests

    @Test
    fun `createLog success returns created log`() = runTest {
        // Given
        val expectedLog = createMockLogDetail()
        val request = CreateLogRequest(
            rating = 5,
            content = "Great experience!",
            imageIds = listOf("img-1", "img-2"),
            recipeId = "recipe-123",
            hashtags = listOf("homecooking"),
            isPrivate = false
        )
        coEvery { apiService.createLog(request) } returns expectedLog

        // When
        val result = repository.createLog(request)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("log-123", result.getOrNull()?.id)
        coVerify { apiService.createLog(request) }
    }

    @Test
    fun `createLog validation error returns error`() = runTest {
        // Given
        val request = CreateLogRequest(
            rating = 0, // Invalid
            content = null,
            imageIds = emptyList(),
            recipeId = null,
            hashtags = emptyList(),
            isPrivate = false
        )
        coEvery { apiService.createLog(request) } throws IOException("Rating is required")

        // When
        val result = repository.createLog(request)

        // Then
        assertTrue(result.isError)
        assertEquals("Rating is required", result.exceptionOrNull()?.message)
    }

    // MARK: - FeedItem Pattern Matching Tests

    @Test
    fun `feed contains mixed log and recipe items`() = runTest {
        // Given
        val feedItems = listOf(
            FeedItem.Log(createMockLogSummary()),
            FeedItem.Recipe(createMockRecipeSummary()),
            FeedItem.Log(createMockLogSummary())
        )
        val response = PaginatedResponse(
            content = feedItems,
            nextCursor = null,
            hasMore = false
        )
        coEvery { apiService.getFeed(any()) } returns response

        // When / Then
        repository.getFeed(null).test {
            awaitItem() // Loading

            val result = awaitItem()
            assertTrue(result.isSuccess)
            val items = result.getOrNull()?.content ?: emptyList()

            // Verify pattern matching works
            val logCount = items.count { it is FeedItem.Log }
            val recipeCount = items.count { it is FeedItem.Recipe }
            assertEquals(2, logCount)
            assertEquals(1, recipeCount)

            awaitComplete()
        }
    }

    // MARK: - Helpers

    private fun createMockLogSummary() = CookingLogSummary(
        id = "log-123",
        author = createMockAuthor(),
        images = emptyList(),
        rating = 4,
        contentPreview = "Test log content",
        recipe = null,
        likeCount = 10,
        commentCount = 3,
        isLiked = false,
        isSaved = false,
        createdAt = LocalDateTime.now()
    )

    private fun createMockLogDetail() = CookingLogDetail(
        id = "log-123",
        author = createMockAuthor(),
        images = emptyList(),
        rating = 4,
        content = "Test log content",
        recipe = null,
        hashtags = listOf("test"),
        isPrivate = false,
        likeCount = 10,
        commentCount = 3,
        isLiked = false,
        isSaved = false,
        createdAt = LocalDateTime.now()
    )

    private fun createMockRecipeSummary() = RecipeSummary(
        id = "recipe-123",
        title = "Test Recipe",
        description = null,
        coverImageUrl = null,
        cookingTimeRange = CookingTimeRange.UNDER_15_MIN,
        servings = 2,
        cookCount = 50,
        averageRating = 4.0,
        author = createMockAuthor(),
        isSaved = false,
        category = null,
        createdAt = LocalDateTime.now()
    )

    private fun createMockAuthor() = UserSummary(
        id = "user-1",
        username = "testuser",
        displayName = "Test User",
        avatarUrl = null
    )
}
