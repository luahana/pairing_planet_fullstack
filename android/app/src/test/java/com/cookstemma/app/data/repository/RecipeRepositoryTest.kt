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

class RecipeRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: RecipeRepository

    @Before
    fun setup() {
        apiService = mockk(relaxed = true)
        repository = RecipeRepository(apiService)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // MARK: - getRecipes Tests

    @Test
    fun `getRecipes emits loading then success`() = runTest {
        // Given
        val recipes = createMockRecipes(3)
        val response = PaginatedResponse(
            content = recipes,
            nextCursor = "cursor123",
            hasMore = true
        )
        coEvery { apiService.getRecipes(any(), any(), any(), any()) } returns response

        // When / Then
        repository.getRecipes(null, null).test {
            // Loading state
            assertTrue(awaitItem().isLoading)

            // Success state
            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(3, result.getOrNull()?.content?.size)
            assertEquals("cursor123", result.getOrNull()?.nextCursor)
            assertTrue(result.getOrNull()?.hasMore ?: false)

            awaitComplete()
        }
    }

    @Test
    fun `getRecipes with cursor passes to API`() = runTest {
        // Given
        val response = PaginatedResponse<RecipeSummary>(
            content = emptyList(),
            nextCursor = null,
            hasMore = false
        )
        coEvery { apiService.getRecipes(any(), any(), any(), any()) } returns response

        // When
        repository.getRecipes("page2", null).test {
            awaitItem() // Loading
            awaitItem() // Result
            awaitComplete()
        }

        // Then
        coVerify { apiService.getRecipes("page2", any(), any(), any()) }
    }

    @Test
    fun `getRecipes with filters passes correct parameters`() = runTest {
        // Given
        val filters = RecipeFilters(
            cookingTimeRange = CookingTimeRange.UNDER_15_MIN,
            category = "korean",
            sortBy = RecipeSortBy.NEWEST
        )
        val response = PaginatedResponse<RecipeSummary>(
            content = emptyList(),
            nextCursor = null,
            hasMore = false
        )
        coEvery { apiService.getRecipes(any(), any(), any(), any()) } returns response

        // When
        repository.getRecipes(null, filters).test {
            awaitItem() // Loading
            awaitItem() // Result
            awaitComplete()
        }

        // Then
        coVerify {
            apiService.getRecipes(
                cursor = null,
                cookingTimeRange = "UNDER_15_MIN",
                category = "korean",
                sort = "newest"
            )
        }
    }

    @Test
    fun `getRecipes network error emits error`() = runTest {
        // Given
        coEvery { apiService.getRecipes(any(), any(), any(), any()) } throws IOException("Network error")

        // When / Then
        repository.getRecipes(null, null).test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isError)
            assertEquals("Network error", result.exceptionOrNull()?.message)

            awaitComplete()
        }
    }

    // MARK: - getRecipe Tests

    @Test
    fun `getRecipe emits loading then success`() = runTest {
        // Given
        val recipe = createMockRecipeDetail()
        coEvery { apiService.getRecipe("recipe-123") } returns recipe

        // When / Then
        repository.getRecipe("recipe-123").test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals("recipe-123", result.getOrNull()?.id)
            assertEquals("Test Recipe", result.getOrNull()?.title)

            awaitComplete()
        }
    }

    @Test
    fun `getRecipe not found emits error`() = runTest {
        // Given
        coEvery { apiService.getRecipe("non-existent") } throws retrofit2.HttpException(
            retrofit2.Response.error<Any>(404, okhttp3.ResponseBody.create(null, ""))
        )

        // When / Then
        repository.getRecipe("non-existent").test {
            assertTrue(awaitItem().isLoading)

            val result = awaitItem()
            assertTrue(result.isError)

            awaitComplete()
        }
    }

    // MARK: - saveRecipe Tests

    @Test
    fun `saveRecipe success returns success`() = runTest {
        // Given
        coEvery { apiService.saveRecipe("recipe-123") } just Runs

        // When
        val result = repository.saveRecipe("recipe-123")

        // Then
        assertTrue(result.isSuccess)
        coVerify { apiService.saveRecipe("recipe-123") }
    }

    @Test
    fun `saveRecipe error returns error`() = runTest {
        // Given
        coEvery { apiService.saveRecipe("recipe-123") } throws IOException("Save failed")

        // When
        val result = repository.saveRecipe("recipe-123")

        // Then
        assertTrue(result.isError)
        assertEquals("Save failed", result.exceptionOrNull()?.message)
    }

    // MARK: - unsaveRecipe Tests

    @Test
    fun `unsaveRecipe success returns success`() = runTest {
        // Given
        coEvery { apiService.unsaveRecipe("recipe-123") } just Runs

        // When
        val result = repository.unsaveRecipe("recipe-123")

        // Then
        assertTrue(result.isSuccess)
        coVerify { apiService.unsaveRecipe("recipe-123") }
    }

    @Test
    fun `unsaveRecipe error returns error`() = runTest {
        // Given
        coEvery { apiService.unsaveRecipe("recipe-123") } throws IOException("Unsave failed")

        // When
        val result = repository.unsaveRecipe("recipe-123")

        // Then
        assertTrue(result.isError)
    }

    // MARK: - Edge Cases

    @Test
    fun `getRecipes empty list returns success with empty content`() = runTest {
        // Given
        val response = PaginatedResponse<RecipeSummary>(
            content = emptyList(),
            nextCursor = null,
            hasMore = false
        )
        coEvery { apiService.getRecipes(any(), any(), any(), any()) } returns response

        // When / Then
        repository.getRecipes(null, null).test {
            awaitItem() // Loading

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()?.content?.isEmpty() ?: false)
            assertEquals(false, result.getOrNull()?.hasMore)

            awaitComplete()
        }
    }

    @Test
    fun `getRecipes last page has no cursor`() = runTest {
        // Given
        val response = PaginatedResponse(
            content = createMockRecipes(2),
            nextCursor = null,
            hasMore = false
        )
        coEvery { apiService.getRecipes(any(), any(), any(), any()) } returns response

        // When / Then
        repository.getRecipes(null, null).test {
            awaitItem() // Loading

            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(null, result.getOrNull()?.nextCursor)
            assertEquals(false, result.getOrNull()?.hasMore)

            awaitComplete()
        }
    }

    // MARK: - Helpers

    private fun createMockRecipes(count: Int): List<RecipeSummary> = (0 until count).map { i ->
        RecipeSummary(
            id = "recipe-$i",
            title = "Recipe $i",
            description = "Description $i",
            coverImageUrl = null,
            cookingTimeRange = CookingTimeRange.UNDER_15_MIN,
            servings = 2,
            cookCount = i * 10,
            averageRating = 4.0,
            author = createMockAuthor(),
            isSaved = false,
            category = null,
            createdAt = LocalDateTime.now()
        )
    }

    private fun createMockRecipeDetail() = RecipeDetail(
        id = "recipe-123",
        title = "Test Recipe",
        description = "Test description",
        coverImageUrl = null,
        images = emptyList(),
        cookingTimeRange = CookingTimeRange.BETWEEN_15_AND_30_MIN,
        servings = 4,
        cookCount = 100,
        saveCount = 50,
        averageRating = 4.5,
        author = createMockAuthor(),
        ingredients = emptyList(),
        steps = emptyList(),
        hashtags = emptyList(),
        isSaved = false,
        category = null,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private fun createMockAuthor() = UserSummary(
        id = "user-1",
        username = "testuser",
        displayName = "Test User",
        avatarUrl = null
    )
}
