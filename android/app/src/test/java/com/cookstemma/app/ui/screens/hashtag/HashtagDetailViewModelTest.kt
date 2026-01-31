package com.cookstemma.app.ui.screens.hashtag

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cookstemma.app.data.repository.SearchRepository
import com.cookstemma.app.domain.model.FeedItem
import com.cookstemma.app.domain.model.PaginatedResponse
import com.cookstemma.app.domain.model.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HashtagDetailViewModelTest {

    private lateinit var searchRepository: SearchRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: HashtagDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testRecipe = FeedItem(
        id = "recipe-1",
        title = "Test Recipe",
        content = null,
        rating = null,
        thumbnailUrl = "https://example.com/recipe.jpg",
        creatorPublicId = "user-1",
        userName = "testuser",
        foodName = "Pizza",
        recipeTitle = null,
        hashtags = listOf("italian"),
        isVariant = false,
        isPrivate = false,
        commentCount = 5,
        cookingStyle = "Italian",
        type = "recipe"
    )

    private val testLog = FeedItem(
        id = "log-1",
        title = "My Cooking Log",
        content = "Great experience!",
        rating = 4,
        thumbnailUrl = "https://example.com/log.jpg",
        creatorPublicId = "user-2",
        userName = "chef",
        foodName = "Pasta",
        recipeTitle = "Carbonara",
        hashtags = listOf("italian"),
        isVariant = false,
        isPrivate = false,
        commentCount = 3,
        cookingStyle = "Italian",
        type = "log"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchRepository = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("tag" to "italian"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(posts: List<FeedItem> = emptyList()) {
        coEvery { searchRepository.getHashtagPosts("italian", null) } returns flowOf(
            Result.Success(
                PaginatedResponse(
                    content = posts,
                    hasMore = false,
                    nextCursor = null
                )
            )
        )
        viewModel = HashtagDetailViewModel(savedStateHandle, searchRepository)
    }

    // MARK: - Initial State Tests

    @Test
    fun `initial state has correct defaults`() = runTest {
        createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("italian", state.hashtag)
            assertEquals(HashtagContentFilter.ALL, state.selectedFilter)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `init loads posts from repository`() = runTest {
        createViewModel(listOf(testRecipe, testLog))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.posts.size)
        }
    }

    // MARK: - Filter Selection Tests

    @Test
    fun `selectFilter updates selected filter to RECIPES`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectFilter(HashtagContentFilter.RECIPES)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(HashtagContentFilter.RECIPES, state.selectedFilter)
        }
    }

    @Test
    fun `selectFilter updates selected filter to LOGS`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectFilter(HashtagContentFilter.LOGS)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(HashtagContentFilter.LOGS, state.selectedFilter)
        }
    }

    @Test
    fun `selectFilter updates selected filter back to ALL`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectFilter(HashtagContentFilter.RECIPES)
        viewModel.selectFilter(HashtagContentFilter.ALL)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(HashtagContentFilter.ALL, state.selectedFilter)
        }
    }

    // MARK: - Filtered Posts Tests

    @Test
    fun `filteredPosts returns all posts when filter is ALL`() = runTest {
        createViewModel(listOf(testRecipe, testLog))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(HashtagContentFilter.ALL, state.selectedFilter)
            assertEquals(2, state.filteredPosts.size)
        }
    }

    @Test
    fun `filteredPosts returns only recipes when filter is RECIPES`() = runTest {
        createViewModel(listOf(testRecipe, testLog))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectFilter(HashtagContentFilter.RECIPES)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.filteredPosts.size)
            assertTrue(state.filteredPosts.all { it.isRecipe })
            assertEquals("recipe-1", state.filteredPosts.first().id)
        }
    }

    @Test
    fun `filteredPosts returns only logs when filter is LOGS`() = runTest {
        createViewModel(listOf(testRecipe, testLog))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectFilter(HashtagContentFilter.LOGS)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.filteredPosts.size)
            assertTrue(state.filteredPosts.all { it.isLog })
            assertEquals("log-1", state.filteredPosts.first().id)
        }
    }

    @Test
    fun `filteredPosts returns empty list when no matching items`() = runTest {
        // Only create a recipe, no logs
        createViewModel(listOf(testRecipe))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectFilter(HashtagContentFilter.LOGS)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.filteredPosts.isEmpty())
        }
    }

    // MARK: - FeedItem Type Helper Tests

    @Test
    fun `FeedItem isRecipe returns true for recipe type`() {
        assertTrue(testRecipe.isRecipe)
        assertFalse(testRecipe.isLog)
    }

    @Test
    fun `FeedItem isLog returns true for log type`() {
        assertTrue(testLog.isLog)
        assertFalse(testLog.isRecipe)
    }

    @Test
    fun `FeedItem with null type returns false for both helpers`() {
        val nullTypeItem = testRecipe.copy(type = null)
        assertFalse(nullTypeItem.isRecipe)
        assertFalse(nullTypeItem.isLog)
    }
}
