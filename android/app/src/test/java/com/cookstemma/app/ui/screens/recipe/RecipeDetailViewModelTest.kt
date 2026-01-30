package com.cookstemma.app.ui.screens.recipe

import app.cash.turbine.test
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
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDetailViewModelTest {

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var savedStateHandle: androidx.lifecycle.SavedStateHandle
    private lateinit var viewModel: RecipeDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        recipeRepository = mockk()
        savedStateHandle = mockk()
        every { savedStateHandle.get<String>("recipeId") } returns "recipe-123"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // MARK: - Initial Load Tests

    @Test
    fun `initial state is loading`() = runTest {
        coEvery { recipeRepository.getRecipe("recipe-123") } returns flowOf(Result.Loading)
        coEvery { recipeRepository.getRecipeLogs("recipe-123", null) } returns flowOf(Result.Loading)

        viewModel = RecipeDetailViewModel(savedStateHandle, recipeRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
        }
    }

    @Test
    fun `loadRecipe success updates state with recipe`() = runTest {
        val mockRecipe = createMockRecipeDetail()
        coEvery { recipeRepository.getRecipe("recipe-123") } returns flowOf(Result.Success(mockRecipe))
        coEvery { recipeRepository.getRecipeLogs("recipe-123", any()) } returns flowOf(
            Result.Success(CookingLogsResponse(emptyList(), null, false))
        )

        viewModel = RecipeDetailViewModel(savedStateHandle, recipeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("recipe-123", state.recipe?.id)
            assertEquals("Test Recipe", state.recipe?.title)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `loadRecipe error updates state with error message`() = runTest {
        val error = Exception("Recipe not found")
        coEvery { recipeRepository.getRecipe("recipe-123") } returns flowOf(Result.Error(error))
        coEvery { recipeRepository.getRecipeLogs("recipe-123", any()) } returns flowOf(Result.Loading)

        viewModel = RecipeDetailViewModel(savedStateHandle, recipeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Recipe not found", state.error)
            assertFalse(state.isLoading)
        }
    }

    // MARK: - Cooking Logs Tests

    @Test
    fun `loadCookingLogs success updates state with logs`() = runTest {
        val mockRecipe = createMockRecipeDetail()
        val mockLogs = listOf(createMockCookingLog("log-1"), createMockCookingLog("log-2"))
        coEvery { recipeRepository.getRecipe("recipe-123") } returns flowOf(Result.Success(mockRecipe))
        coEvery { recipeRepository.getRecipeLogs("recipe-123", null) } returns flowOf(
            Result.Success(CookingLogsResponse(mockLogs, "cursor-1", true))
        )

        viewModel = RecipeDetailViewModel(savedStateHandle, recipeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.cookingLogs.size)
            assertTrue(state.hasMoreLogs)
            assertEquals("cursor-1", state.logsCursor)
        }
    }

    @Test
    fun `loadMoreLogs appends to existing logs`() = runTest {
        val mockRecipe = createMockRecipeDetail()
        val initialLogs = listOf(createMockCookingLog("log-1"))
        val moreLogs = listOf(createMockCookingLog("log-2"))

        coEvery { recipeRepository.getRecipe("recipe-123") } returns flowOf(Result.Success(mockRecipe))
        coEvery { recipeRepository.getRecipeLogs("recipe-123", null) } returns flowOf(
            Result.Success(CookingLogsResponse(initialLogs, "cursor-1", true))
        )
        coEvery { recipeRepository.getRecipeLogs("recipe-123", "cursor-1") } returns flowOf(
            Result.Success(CookingLogsResponse(moreLogs, null, false))
        )

        viewModel = RecipeDetailViewModel(savedStateHandle, recipeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreLogs()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.cookingLogs.size)
            assertFalse(state.hasMoreLogs)
            assertNull(state.logsCursor)
        }
    }

    @Test
    fun `loadMoreLogs does nothing when no cursor`() = runTest {
        val mockRecipe = createMockRecipeDetail()
        coEvery { recipeRepository.getRecipe("recipe-123") } returns flowOf(Result.Success(mockRecipe))
        coEvery { recipeRepository.getRecipeLogs("recipe-123", null) } returns flowOf(
            Result.Success(CookingLogsResponse(emptyList(), null, false))
        )

        viewModel = RecipeDetailViewModel(savedStateHandle, recipeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreLogs()
        testDispatcher.scheduler.advanceUntilIdle()

        // Should not throw or cause issues
        coVerify(exactly = 1) { recipeRepository.getRecipeLogs("recipe-123", null) }
    }

    // MARK: - Toggle Save Tests

    @Test
    fun `toggleSave optimistically updates UI when saving`() = runTest {
        val mockRecipe = createMockRecipeDetail(isSaved = false)
        coEvery { recipeRepository.getRecipe("recipe-123") } returns flowOf(Result.Success(mockRecipe))
        coEvery { recipeRepository.getRecipeLogs("recipe-123", any()) } returns flowOf(
            Result.Success(CookingLogsResponse(emptyList(), null, false))
        )
        coEvery { recipeRepository.saveRecipe("recipe-123") } returns flowOf(Result.Success(Unit))

        viewModel = RecipeDetailViewModel(savedStateHandle, recipeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSave()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.recipe?.isSaved ?: false)
        }

        coVerify { recipeRepository.saveRecipe("recipe-123") }
    }

    @Test
    fun `toggleSave optimistically updates UI when unsaving`() = runTest {
        val mockRecipe = createMockRecipeDetail(isSaved = true)
        coEvery { recipeRepository.getRecipe("recipe-123") } returns flowOf(Result.Success(mockRecipe))
        coEvery { recipeRepository.getRecipeLogs("recipe-123", any()) } returns flowOf(
            Result.Success(CookingLogsResponse(emptyList(), null, false))
        )
        coEvery { recipeRepository.unsaveRecipe("recipe-123") } returns flowOf(Result.Success(Unit))

        viewModel = RecipeDetailViewModel(savedStateHandle, recipeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSave()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.recipe?.isSaved ?: true)
        }

        coVerify { recipeRepository.unsaveRecipe("recipe-123") }
    }

    @Test
    fun `toggleSave reverts on API failure`() = runTest {
        val mockRecipe = createMockRecipeDetail(isSaved = false)
        coEvery { recipeRepository.getRecipe("recipe-123") } returns flowOf(Result.Success(mockRecipe))
        coEvery { recipeRepository.getRecipeLogs("recipe-123", any()) } returns flowOf(
            Result.Success(CookingLogsResponse(emptyList(), null, false))
        )
        coEvery { recipeRepository.saveRecipe("recipe-123") } returns flowOf(Result.Error(Exception("Failed")))

        viewModel = RecipeDetailViewModel(savedStateHandle, recipeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSave()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.recipe?.isSaved ?: true) // Should be reverted
        }
    }

    // MARK: - Refresh Tests

    @Test
    fun `refresh reloads recipe and logs`() = runTest {
        val mockRecipe = createMockRecipeDetail()
        coEvery { recipeRepository.getRecipe("recipe-123") } returns flowOf(Result.Success(mockRecipe))
        coEvery { recipeRepository.getRecipeLogs("recipe-123", null) } returns flowOf(
            Result.Success(CookingLogsResponse(emptyList(), null, false))
        )

        viewModel = RecipeDetailViewModel(savedStateHandle, recipeRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 2) { recipeRepository.getRecipe("recipe-123") }
        coVerify(exactly = 2) { recipeRepository.getRecipeLogs("recipe-123", null) }
    }

    // MARK: - Helpers

    private fun createMockRecipeDetail(isSaved: Boolean = false) = RecipeDetail(
        id = "recipe-123",
        title = "Test Recipe",
        description = "Test description",
        coverImageUrl = null,
        images = emptyList(),
        cookingTimeRange = CookingTimeRange.UNDER_15_MIN,
        servings = 2,
        cookCount = 100,
        saveCount = 50,
        averageRating = 4.5,
        author = createMockAuthor(),
        ingredients = emptyList(),
        steps = emptyList(),
        hashtags = emptyList(),
        isSaved = isSaved,
        category = null,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private fun createMockCookingLog(id: String) = CookingLog(
        id = id,
        author = createMockAuthor(),
        images = emptyList(),
        rating = 4,
        content = "Test content",
        recipe = null,
        hashtags = emptyList(),
        isPrivate = false,
        likeCount = 10,
        commentCount = 2,
        isLiked = false,
        isSaved = false,
        createdAt = LocalDateTime.now()
    )

    private fun createMockAuthor() = UserSummary(
        id = "user-1",
        username = "testuser",
        displayName = "Test User",
        avatarUrl = null
    )
}

// Response class for cooking logs
data class CookingLogsResponse(
    val items: List<CookingLog>,
    val nextCursor: String?,
    val hasMore: Boolean
)
