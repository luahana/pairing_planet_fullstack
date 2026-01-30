package com.cookstemma.app.ui.screens.home

import app.cash.turbine.test
import com.cookstemma.app.data.repository.FeedRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeFeedViewModelTest {

    private lateinit var feedRepository: FeedRepository
    private lateinit var viewModel: HomeFeedViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        feedRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        coEvery { feedRepository.getFeed(null) } returns flowOf(Result.Loading)

        viewModel = HomeFeedViewModel(feedRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading || state.items.isEmpty())
        }
    }

    @Test
    fun `loadFeed success updates state with items`() = runTest {
        val mockItems = listOf(
            FeedItem.Log(createMockLog("log-1")),
            FeedItem.Log(createMockLog("log-2"))
        )
        val response = PaginatedResponse(mockItems, "cursor-1", true)

        coEvery { feedRepository.getFeed(null) } returns flowOf(Result.Success(response))

        viewModel = HomeFeedViewModel(feedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.items.size)
            assertFalse(state.isLoading)
            assertTrue(state.hasMore)
        }
    }

    @Test
    fun `loadFeed error updates state with error message`() = runTest {
        val error = Exception("Network error")
        coEvery { feedRepository.getFeed(null) } returns flowOf(Result.Error(error))

        viewModel = HomeFeedViewModel(feedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Network error", state.error)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `likeLog optimistically updates UI`() = runTest {
        val log = createMockLog("log-1", isLiked = false, likeCount = 10)
        val mockItems = listOf(FeedItem.Log(log))
        val response = PaginatedResponse(mockItems, null, false)

        coEvery { feedRepository.getFeed(null) } returns flowOf(Result.Success(response))
        coEvery { feedRepository.likeLog("log-1") } returns Result.Success(Unit)

        viewModel = HomeFeedViewModel(feedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.likeLog(log)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val updatedLog = (state.items.first() as FeedItem.Log).log
            assertTrue(updatedLog.isLiked)
            assertEquals(11, updatedLog.likeCount)
        }

        coVerify { feedRepository.likeLog("log-1") }
    }

    @Test
    fun `likeLog reverts on API failure`() = runTest {
        val log = createMockLog("log-1", isLiked = false, likeCount = 10)
        val mockItems = listOf(FeedItem.Log(log))
        val response = PaginatedResponse(mockItems, null, false)

        coEvery { feedRepository.getFeed(null) } returns flowOf(Result.Success(response))
        coEvery { feedRepository.likeLog("log-1") } returns Result.Error(Exception("Failed"))

        viewModel = HomeFeedViewModel(feedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.likeLog(log)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val updatedLog = (state.items.first() as FeedItem.Log).log
            assertFalse(updatedLog.isLiked)
            assertEquals(10, updatedLog.likeCount)
        }
    }

    @Test
    fun `loadMore appends items to existing list`() = runTest {
        val initialItems = listOf(FeedItem.Log(createMockLog("log-1")))
        val moreItems = listOf(FeedItem.Log(createMockLog("log-2")))

        coEvery { feedRepository.getFeed(null) } returns flowOf(
            Result.Success(PaginatedResponse(initialItems, "cursor-1", true))
        )
        coEvery { feedRepository.getFeed("cursor-1") } returns flowOf(
            Result.Success(PaginatedResponse(moreItems, null, false))
        )

        viewModel = HomeFeedViewModel(feedRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.items.size)
            assertFalse(state.hasMore)
        }
    }

    private fun createMockLog(
        id: String,
        isLiked: Boolean = false,
        isSaved: Boolean = false,
        likeCount: Int = 0
    ) = CookingLogSummary(
        id = id,
        author = UserSummary("user-1", "testuser", "Test User", null),
        images = listOf(LogImage("thumb.jpg", "original.jpg")),
        rating = 4,
        contentPreview = "Test content",
        recipe = null,
        likeCount = likeCount,
        commentCount = 0,
        isLiked = isLiked,
        isSaved = isSaved,
        createdAt = LocalDateTime.now()
    )
}
