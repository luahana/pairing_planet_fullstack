package com.cookstemma.app.ui.screens.create

import android.net.Uri
import app.cash.turbine.test
import com.cookstemma.app.data.repository.LogRepository
import com.cookstemma.app.data.repository.RecipeRepository
import com.cookstemma.app.domain.model.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CreateLogViewModelTest {

    private lateinit var logRepository: LogRepository
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var viewModel: CreateLogViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        logRepository = mockk()
        recipeRepository = mockk()
        viewModel = CreateLogViewModel(logRepository, recipeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // MARK: - Initial State Tests

    @Test
    fun `initial state has correct defaults`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.photos.isEmpty())
            assertEquals(0, state.rating)
            assertNull(state.linkedRecipe)
            assertTrue(state.content.isEmpty())
            assertTrue(state.hashtags.isEmpty())
            assertFalse(state.isPrivate)
            assertFalse(state.canSubmit)
            assertEquals(5, state.photosRemaining)
        }
    }

    // MARK: - Photo Management Tests

    @Test
    fun `addPhoto adds photo to list`() = runTest {
        val mockUri = mockk<Uri>()

        viewModel.addPhoto(mockUri)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.photos.size)
            assertEquals(4, state.photosRemaining)
        }
    }

    @Test
    fun `addPhoto does not exceed max photos`() = runTest {
        val uris = (0 until 6).map { mockk<Uri>() }

        uris.forEach { viewModel.addPhoto(it) }

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(5, state.photos.size) // Max is 5
            assertEquals(0, state.photosRemaining)
        }
    }

    @Test
    fun `removePhoto removes correct photo`() = runTest {
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()

        viewModel.addPhoto(uri1)
        viewModel.addPhoto(uri2)
        viewModel.removePhoto(uri1)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.photos.size)
            assertEquals(uri2, state.photos.first())
        }
    }

    @Test
    fun `reorderPhotos reorders correctly`() = runTest {
        val uri1 = mockk<Uri>()
        val uri2 = mockk<Uri>()
        val uri3 = mockk<Uri>()

        viewModel.addPhoto(uri1)
        viewModel.addPhoto(uri2)
        viewModel.addPhoto(uri3)

        viewModel.reorderPhotos(0, 2)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(uri2, state.photos[0])
            assertEquals(uri3, state.photos[1])
            assertEquals(uri1, state.photos[2])
        }
    }

    // MARK: - Rating Tests

    @Test
    fun `setRating updates rating`() = runTest {
        viewModel.setRating(4)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(4, state.rating)
        }
    }

    // MARK: - Content Tests

    @Test
    fun `setContent updates content`() = runTest {
        viewModel.setContent("Great cooking experience!")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Great cooking experience!", state.content)
        }
    }

    @Test
    fun `setHashtags updates hashtags`() = runTest {
        viewModel.setHashtags("#cooking #dinner")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("#cooking #dinner", state.hashtags)
        }
    }

    @Test
    fun `setPrivate updates privacy flag`() = runTest {
        viewModel.setPrivate(true)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isPrivate)
        }
    }

    // MARK: - Recipe Link Tests

    @Test
    fun `selectRecipe sets linked recipe`() = runTest {
        val recipe = createMockRecipeSummary()

        viewModel.selectRecipe(recipe)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(recipe.id, state.linkedRecipe?.id)
            assertTrue(state.recipeSearchQuery.isEmpty())
            assertTrue(state.recipeSearchResults.isEmpty())
        }
    }

    @Test
    fun `clearLinkedRecipe removes linked recipe`() = runTest {
        val recipe = createMockRecipeSummary()
        viewModel.selectRecipe(recipe)
        viewModel.clearLinkedRecipe()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.linkedRecipe)
        }
    }

    @Test
    fun `setRecipeSearchQuery triggers search when query is long enough`() = runTest {
        val searchResults = listOf(createMockRecipeSummary())
        coEvery { recipeRepository.searchRecipes("kim") } returns flowOf(
            Result.Success(SearchRecipesResponse(searchResults, null, false))
        )

        viewModel.setRecipeSearchQuery("kim")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("kim", state.recipeSearchQuery)
        }
    }

    @Test
    fun `setRecipeSearchQuery clears results when query is too short`() = runTest {
        viewModel.setRecipeSearchQuery("k")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.recipeSearchResults.isEmpty())
        }
    }

    // MARK: - Can Submit Tests

    @Test
    fun `canSubmit returns true when requirements met`() = runTest {
        viewModel.addPhoto(mockk<Uri>())
        viewModel.setRating(4)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.canSubmit)
        }
    }

    @Test
    fun `canSubmit returns false without photos`() = runTest {
        viewModel.setRating(4)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.canSubmit)
        }
    }

    @Test
    fun `canSubmit returns false without rating`() = runTest {
        viewModel.addPhoto(mockk<Uri>())

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.canSubmit)
        }
    }

    // MARK: - Submit Tests

    @Test
    fun `submit success updates state`() = runTest {
        val mockLog = createMockCookingLog()
        coEvery {
            logRepository.createLog(
                photos = any(),
                rating = any(),
                recipeId = any(),
                content = any(),
                hashtags = any(),
                isPrivate = any()
            )
        } returns flowOf(Result.Success(mockLog))

        viewModel.addPhoto(mockk<Uri>())
        viewModel.setRating(4)
        viewModel.setContent("Test content")

        viewModel.submit(listOf(mockk<File>()))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSubmitSuccess)
            assertFalse(state.isSubmitting)
        }
    }

    @Test
    fun `submit failure sets error`() = runTest {
        coEvery {
            logRepository.createLog(
                photos = any(),
                rating = any(),
                recipeId = any(),
                content = any(),
                hashtags = any(),
                isPrivate = any()
            )
        } returns flowOf(Result.Error(Exception("Upload failed")))

        viewModel.addPhoto(mockk<Uri>())
        viewModel.setRating(4)

        viewModel.submit(listOf(mockk<File>()))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Upload failed", state.error)
            assertFalse(state.isSubmitSuccess)
            assertFalse(state.isSubmitting)
        }
    }

    @Test
    fun `submit does nothing when canSubmit is false`() = runTest {
        // No photos or rating set
        viewModel.submit(emptyList())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isSubmitting)
            assertFalse(state.isSubmitSuccess)
        }

        coVerify(exactly = 0) {
            logRepository.createLog(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `submit parses hashtags correctly`() = runTest {
        val mockLog = createMockCookingLog()
        coEvery {
            logRepository.createLog(
                photos = any(),
                rating = 4,
                recipeId = null,
                content = null,
                hashtags = listOf("cooking", "dinner", "homemade"),
                isPrivate = false
            )
        } returns flowOf(Result.Success(mockLog))

        viewModel.addPhoto(mockk<Uri>())
        viewModel.setRating(4)
        viewModel.setHashtags("#cooking dinner, #homemade")

        viewModel.submit(listOf(mockk<File>()))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            logRepository.createLog(
                photos = any(),
                rating = 4,
                recipeId = null,
                content = null,
                hashtags = listOf("cooking", "dinner", "homemade"),
                isPrivate = false
            )
        }
    }

    // MARK: - Clear Error Tests

    @Test
    fun `clearError clears error message`() = runTest {
        coEvery {
            logRepository.createLog(any(), any(), any(), any(), any(), any())
        } returns flowOf(Result.Error(Exception("Error")))

        viewModel.addPhoto(mockk<Uri>())
        viewModel.setRating(4)
        viewModel.submit(listOf(mockk<File>()))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearError()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.error)
        }
    }

    // MARK: - Reset Tests

    @Test
    fun `reset clears all state`() = runTest {
        viewModel.addPhoto(mockk<Uri>())
        viewModel.setRating(4)
        viewModel.setContent("Test")
        viewModel.selectRecipe(createMockRecipeSummary())
        viewModel.setHashtags("#test")
        viewModel.setPrivate(true)

        viewModel.reset()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.photos.isEmpty())
            assertEquals(0, state.rating)
            assertNull(state.linkedRecipe)
            assertTrue(state.content.isEmpty())
            assertTrue(state.hashtags.isEmpty())
            assertFalse(state.isPrivate)
            assertFalse(state.isSubmitSuccess)
        }
    }

    // MARK: - Helpers

    private fun createMockRecipeSummary() = RecipeSummary(
        id = "recipe-123",
        title = "Test Recipe",
        description = null,
        coverImageUrl = null,
        cookingTimeRange = CookingTimeRange.UNDER_15_MIN,
        servings = 2,
        cookCount = 50,
        averageRating = 4.0,
        author = UserSummary("user-1", "testuser", "Test User", null),
        isSaved = false,
        category = null,
        createdAt = LocalDateTime.now()
    )

    private fun createMockCookingLog() = CookingLog(
        id = "log-123",
        author = UserSummary("user-1", "testuser", "Test User", null),
        images = emptyList(),
        rating = 4,
        content = "Test content",
        recipe = null,
        hashtags = emptyList(),
        isPrivate = false,
        likeCount = 0,
        commentCount = 0,
        isLiked = false,
        isSaved = false,
        createdAt = LocalDateTime.now()
    )
}

// Helper response class
data class SearchRecipesResponse(
    val items: List<RecipeSummary>,
    val nextCursor: String?,
    val hasMore: Boolean
)
