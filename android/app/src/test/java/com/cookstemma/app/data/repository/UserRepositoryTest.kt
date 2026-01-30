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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: UserRepository

    @Before
    fun setup() {
        apiService = mockk(relaxed = true)
        repository = UserRepository(apiService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // MARK: - getMyProfile Tests

    @Test
    fun `getMyProfile emits loading then success`() = runTest {
        // Given
        val profile = createMockMyProfile()
        coEvery { apiService.getMyProfile() } returns profile

        // When / Then
        repository.getMyProfile().test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals("user-me", result.getOrNull()?.id)
            assertEquals("myuser", result.getOrNull()?.username)
            assertEquals(12, result.getOrNull()?.level)

            awaitComplete()
        }
    }

    @Test
    fun `getMyProfile unauthorized emits error`() = runTest {
        // Given
        coEvery { apiService.getMyProfile() } throws IOException("Unauthorized")

        // When / Then
        repository.getMyProfile().test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isError)

            awaitComplete()
        }
    }

    // MARK: - getUserProfile Tests

    @Test
    fun `getUserProfile emits loading then success`() = runTest {
        // Given
        val profile = createMockUserProfile()
        coEvery { apiService.getUserProfile("user-other") } returns profile

        // When / Then
        repository.getUserProfile("user-other").test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals("user-other", result.getOrNull()?.id)
            assertEquals("otheruser", result.getOrNull()?.username)
            assertFalse(result.getOrNull()?.isFollowing ?: true)

            awaitComplete()
        }
    }

    @Test
    fun `getUserProfile not found emits error`() = runTest {
        // Given
        coEvery { apiService.getUserProfile("non-existent") } throws IOException("Not found")

        // When / Then
        repository.getUserProfile("non-existent").test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isError)

            awaitComplete()
        }
    }

    // MARK: - getUserRecipes Tests

    @Test
    fun `getUserRecipes emits loading then success`() = runTest {
        // Given
        val recipes = listOf(createMockRecipeSummary(), createMockRecipeSummary())
        val response = PaginatedResponse(
            content = recipes,
            nextCursor = "cursor",
            hasMore = true
        )
        coEvery { apiService.getUserRecipes("user-123", any()) } returns response

        // When / Then
        repository.getUserRecipes("user-123", null).test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()?.content?.size)
            assertTrue(result.getOrNull()?.hasMore ?: false)

            awaitComplete()
        }
    }

    // MARK: - getUserLogs Tests

    @Test
    fun `getUserLogs emits loading then success`() = runTest {
        // Given
        val logs = listOf(createMockLogSummary(), createMockLogSummary())
        val response = PaginatedResponse(
            content = logs,
            nextCursor = "cursor",
            hasMore = true
        )
        coEvery { apiService.getUserLogs("user-123", any()) } returns response

        // When / Then
        repository.getUserLogs("user-123", null).test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()?.content?.size)

            awaitComplete()
        }
    }

    // MARK: - followUser Tests

    @Test
    fun `followUser success returns success`() = runTest {
        // Given
        coEvery { apiService.followUser("user-to-follow") } just Runs

        // When
        val result = repository.followUser("user-to-follow")

        // Then
        assertTrue(result.isSuccess)
        coVerify { apiService.followUser("user-to-follow") }
    }

    @Test
    fun `followUser already following returns error`() = runTest {
        // Given
        coEvery { apiService.followUser("user-123") } throws IOException("Already following")

        // When
        val result = repository.followUser("user-123")

        // Then
        assertTrue(result.isError)
        assertEquals("Already following", result.exceptionOrNull()?.message)
    }

    // MARK: - unfollowUser Tests

    @Test
    fun `unfollowUser success returns success`() = runTest {
        // Given
        coEvery { apiService.unfollowUser("user-to-unfollow") } just Runs

        // When
        val result = repository.unfollowUser("user-to-unfollow")

        // Then
        assertTrue(result.isSuccess)
        coVerify { apiService.unfollowUser("user-to-unfollow") }
    }

    @Test
    fun `unfollowUser error returns error`() = runTest {
        // Given
        coEvery { apiService.unfollowUser("user-123") } throws IOException("Unfollow failed")

        // When
        val result = repository.unfollowUser("user-123")

        // Then
        assertTrue(result.isError)
    }

    // MARK: - getFollowers Tests

    @Test
    fun `getFollowers emits loading then success`() = runTest {
        // Given
        val users = listOf(createMockUserSummary(), createMockUserSummary())
        val response = PaginatedResponse(
            content = users,
            nextCursor = "next",
            hasMore = true
        )
        coEvery { apiService.getFollowers("user-123", any()) } returns response

        // When / Then
        repository.getFollowers("user-123", null).test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(2, result.getOrNull()?.content?.size)
            assertTrue(result.getOrNull()?.hasMore ?: false)

            awaitComplete()
        }
    }

    @Test
    fun `getFollowers with cursor fetches next page`() = runTest {
        // Given
        val response = PaginatedResponse<UserSummary>(
            content = emptyList(),
            nextCursor = null,
            hasMore = false
        )
        coEvery { apiService.getFollowers(any(), any()) } returns response

        // When
        repository.getFollowers("user-123", "page2").test {
            awaitItem() // Loading
            awaitItem() // Result
            awaitComplete()
        }

        // Then
        coVerify { apiService.getFollowers("user-123", "page2") }
    }

    // MARK: - blockUser Tests

    @Test
    fun `blockUser success returns success`() = runTest {
        // Given
        coEvery { apiService.blockUser("user-to-block") } just Runs

        // When
        val result = repository.blockUser("user-to-block")

        // Then
        assertTrue(result.isSuccess)
        coVerify { apiService.blockUser("user-to-block") }
    }

    @Test
    fun `blockUser error returns error`() = runTest {
        // Given
        coEvery { apiService.blockUser("user-123") } throws IOException("Block failed")

        // When
        val result = repository.blockUser("user-123")

        // Then
        assertTrue(result.isError)
    }

    // MARK: - Profile Stats Tests

    @Test
    fun `user profile stats are correct`() = runTest {
        // Given
        val profile = createMockUserProfile()
        coEvery { apiService.getUserProfile("user-other") } returns profile

        // When / Then
        repository.getUserProfile("user-other").test {
            awaitItem() // Loading

            val result = awaitItem()
            assertTrue(result.isSuccess)
            val user = result.getOrNull()!!
            assertEquals(45, user.recipeCount)
            assertEquals(203, user.logCount)
            assertEquals(5200, user.followerCount)
            assertEquals(150, user.followingCount)

            awaitComplete()
        }
    }

    // MARK: - Helpers

    private fun createMockMyProfile() = MyProfile(
        id = "user-me",
        username = "myuser",
        displayName = "My User",
        email = "me@example.com",
        avatarUrl = null,
        bio = "My bio",
        level = 12,
        xp = 2450,
        levelProgress = 0.45,
        recipeCount = 15,
        logCount = 89,
        followerCount = 1200,
        followingCount = 350,
        socialLinks = null,
        createdAt = LocalDateTime.now()
    )

    private fun createMockUserProfile() = UserProfile(
        id = "user-other",
        username = "otheruser",
        displayName = "Other User",
        avatarUrl = null,
        bio = "Other bio",
        level = 24,
        recipeCount = 45,
        logCount = 203,
        followerCount = 5200,
        followingCount = 150,
        socialLinks = null,
        isFollowing = false,
        isFollowedBy = false,
        isBlocked = false,
        createdAt = LocalDateTime.now()
    )

    private fun createMockUserSummary() = UserSummary(
        id = "user-1",
        username = "testuser",
        displayName = "Test User",
        avatarUrl = null
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
        author = createMockUserSummary(),
        isSaved = false,
        category = null,
        createdAt = LocalDateTime.now()
    )

    private fun createMockLogSummary() = CookingLogSummary(
        id = "log-123",
        author = createMockUserSummary(),
        images = emptyList(),
        rating = 4,
        contentPreview = "Test log",
        recipe = null,
        likeCount = 10,
        commentCount = 3,
        isLiked = false,
        isSaved = false,
        createdAt = LocalDateTime.now()
    )
}
