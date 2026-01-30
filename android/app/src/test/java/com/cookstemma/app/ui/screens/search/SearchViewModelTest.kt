package com.cookstemma.app.ui.screens.search

import app.cash.turbine.test
import com.cookstemma.app.data.repository.SearchRepository
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
class SearchViewModelTest {

    private lateinit var searchRepository: SearchRepository
    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchRepository = mockk()

        // Default mock for init
        coEvery { searchRepository.getTrendingHashtags() } returns flowOf(Result.Success(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // MARK: - Initial State Tests

    @Test
    fun `initial state has correct defaults`() = runTest {
        viewModel = SearchViewModel(searchRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.query.isEmpty())
            assertEquals(SearchTab.ALL, state.selectedTab)
            assertNull(state.results)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `init loads trending hashtags`() = runTest {
        val hashtags = listOf(
            HashtagResult("trending", 100),
            HashtagResult("popular", 50)
        )
        coEvery { searchRepository.getTrendingHashtags() } returns flowOf(Result.Success(hashtags))

        viewModel = SearchViewModel(searchRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.trendingHashtags.size)
        }
    }

    // MARK: - Query Tests

    @Test
    fun `setQuery updates query state`() = runTest {
        viewModel = SearchViewModel(searchRepository)

        viewModel.setQuery("kimchi")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("kimchi", state.query)
        }
    }

    @Test
    fun `setQuery with short query clears results`() = runTest {
        viewModel = SearchViewModel(searchRepository)

        viewModel.setQuery("k")

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.results)
            assertTrue(state.recipes.isEmpty())
        }
    }

    @Test
    fun `setQuery with valid query triggers search after debounce`() = runTest {
        val mockResults = createMockSearchResults()
        coEvery { searchRepository.search("kimchi") } returns flowOf(Result.Success(mockResults))

        viewModel = SearchViewModel(searchRepository)
        viewModel.setQuery("kimchi")

        // Advance past debounce (300ms)
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { searchRepository.search("kimchi") }
    }

    // MARK: - Tab Selection Tests

    @Test
    fun `selectTab updates selected tab`() = runTest {
        viewModel = SearchViewModel(searchRepository)

        viewModel.selectTab(SearchTab.RECIPES)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(SearchTab.RECIPES, state.selectedTab)
        }
    }

    @Test
    fun `selectTab triggers search when query exists`() = runTest {
        val recipes = listOf(createMockRecipeSummary())
        coEvery { searchRepository.searchRecipes("test") } returns flowOf(
            Result.Success(PaginatedResponse(recipes, null, false))
        )

        viewModel = SearchViewModel(searchRepository)
        viewModel.setQuery("test")
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectTab(SearchTab.RECIPES)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { searchRepository.searchRecipes("test") }
    }

    // MARK: - Search Tests

    @Test
    fun `search ALL tab returns combined results`() = runTest {
        val mockResults = createMockSearchResults()
        coEvery { searchRepository.search("test") } returns flowOf(Result.Success(mockResults))

        viewModel = SearchViewModel(searchRepository)
        viewModel.setQuery("test")
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(mockResults, state.results)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `search RECIPES tab returns recipes only`() = runTest {
        val recipes = listOf(createMockRecipeSummary())
        coEvery { searchRepository.searchRecipes("test") } returns flowOf(
            Result.Success(PaginatedResponse(recipes, "cursor-1", true))
        )

        viewModel = SearchViewModel(searchRepository)
        viewModel.selectTab(SearchTab.RECIPES)
        viewModel.setQuery("test")
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.recipes.size)
            assertTrue(state.hasMore)
            assertEquals("cursor-1", state.cursor)
        }
    }

    @Test
    fun `search LOGS tab returns logs only`() = runTest {
        val logs = listOf(createMockCookingLog())
        coEvery { searchRepository.searchLogs("test") } returns flowOf(
            Result.Success(PaginatedResponse(logs, null, false))
        )

        viewModel = SearchViewModel(searchRepository)
        viewModel.selectTab(SearchTab.LOGS)
        viewModel.setQuery("test")
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.logs.size)
        }
    }

    @Test
    fun `search USERS tab returns users only`() = runTest {
        val users = listOf(createMockUserSummary())
        coEvery { searchRepository.searchUsers("test") } returns flowOf(
            Result.Success(PaginatedResponse(users, null, false))
        )

        viewModel = SearchViewModel(searchRepository)
        viewModel.selectTab(SearchTab.USERS)
        viewModel.setQuery("test")
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.users.size)
        }
    }

    @Test
    fun `search shows loading state`() = runTest {
        coEvery { searchRepository.search("test") } returns flowOf(Result.Loading)

        viewModel = SearchViewModel(searchRepository)
        viewModel.setQuery("test")
        testDispatcher.scheduler.advanceTimeBy(350)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
        }
    }

    // MARK: - Load More Tests

    @Test
    fun `loadMore appends results`() = runTest {
        val initialRecipes = listOf(createMockRecipeSummary("recipe-1"))
        val moreRecipes = listOf(createMockRecipeSummary("recipe-2"))

        coEvery { searchRepository.searchRecipes("test") } returns flowOf(
            Result.Success(PaginatedResponse(initialRecipes, "cursor-1", true))
        )
        coEvery { searchRepository.searchRecipes("test", "cursor-1") } returns flowOf(
            Result.Success(PaginatedResponse(moreRecipes, null, false))
        )

        viewModel = SearchViewModel(searchRepository)
        viewModel.selectTab(SearchTab.RECIPES)
        viewModel.setQuery("test")
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.recipes.size)
            assertFalse(state.hasMore)
        }
    }

    @Test
    fun `loadMore does nothing without cursor`() = runTest {
        val recipes = listOf(createMockRecipeSummary())
        coEvery { searchRepository.searchRecipes("test") } returns flowOf(
            Result.Success(PaginatedResponse(recipes, null, false))
        )

        viewModel = SearchViewModel(searchRepository)
        viewModel.selectTab(SearchTab.RECIPES)
        viewModel.setQuery("test")
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Should not call API again
        coVerify(exactly = 1) { searchRepository.searchRecipes("test") }
    }

    // MARK: - Recent Searches Tests

    @Test
    fun `submitSearch adds to recent searches`() = runTest {
        coEvery { searchRepository.search("new search") } returns flowOf(
            Result.Success(createMockSearchResults())
        )

        viewModel = SearchViewModel(searchRepository)
        viewModel.setQuery("new search")
        viewModel.submitSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.recentSearches.contains("new search"))
        }
    }

    @Test
    fun `clearRecentSearch removes specific search`() = runTest {
        viewModel = SearchViewModel(searchRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearRecentSearch("kimchi")

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.recentSearches.contains("kimchi"))
        }
    }

    @Test
    fun `clearAllRecentSearches clears all`() = runTest {
        viewModel = SearchViewModel(searchRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearAllRecentSearches()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.recentSearches.isEmpty())
        }
    }

    // MARK: - Helpers

    private fun createMockSearchResults() = SearchResults(
        recipes = listOf(createMockRecipeSummary()),
        logs = listOf(createMockCookingLog()),
        users = listOf(createMockUserSummary()),
        hashtags = listOf(HashtagResult("test", 10))
    )

    private fun createMockRecipeSummary(id: String = "recipe-123") = RecipeSummary(
        id = id,
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

    private fun createMockCookingLog() = CookingLog(
        id = "log-123",
        author = createMockUserSummary(),
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

    private fun createMockUserSummary() = UserSummary(
        id = "user-1",
        username = "testuser",
        displayName = "Test User",
        avatarUrl = null
    )
}

// Domain model for search results
data class SearchResults(
    val recipes: List<RecipeSummary>,
    val logs: List<CookingLog>,
    val users: List<UserSummary>,
    val hashtags: List<HashtagResult>
)

data class HashtagResult(
    val tag: String,
    val count: Int
)
