package com.cookstemma.app.ui.screens.profile

import app.cash.turbine.test
import com.cookstemma.app.data.repository.UserRepository
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
class ProfileViewModelTest {

    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile with null userId loads own profile`() = runTest {
        val myProfile = createMockMyProfile()
        coEvery { userRepository.getMyProfile() } returns flowOf(Result.Success(myProfile))
        coEvery { userRepository.getUserRecipes(any(), any()) } returns flowOf(Result.Success(PaginatedResponse(emptyList(), null, false)))

        viewModel = ProfileViewModel(userRepository)
        viewModel.loadProfile(null)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("myuser", state.username)
            assertEquals(5, state.level)
            assertFalse(state.isLoading)
        }

        coVerify { userRepository.getMyProfile() }
    }

    @Test
    fun `loadProfile with userId loads other user profile`() = runTest {
        val userProfile = createMockUserProfile("user-123")
        coEvery { userRepository.getUserProfile("user-123") } returns flowOf(Result.Success(userProfile))
        coEvery { userRepository.getUserRecipes(any(), any()) } returns flowOf(Result.Success(PaginatedResponse(emptyList(), null, false)))

        viewModel = ProfileViewModel(userRepository)
        viewModel.loadProfile("user-123")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("testuser", state.username)
            assertFalse(state.isFollowing)
        }

        coVerify { userRepository.getUserProfile("user-123") }
    }

    @Test
    fun `toggleFollow optimistically updates UI`() = runTest {
        val userProfile = createMockUserProfile("user-123", isFollowing = false)
        coEvery { userRepository.getUserProfile("user-123") } returns flowOf(Result.Success(userProfile))
        coEvery { userRepository.getUserRecipes(any(), any()) } returns flowOf(Result.Success(PaginatedResponse(emptyList(), null, false)))
        coEvery { userRepository.followUser("user-123") } returns Result.Success(Unit)

        viewModel = ProfileViewModel(userRepository)
        viewModel.loadProfile("user-123")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFollow()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isFollowing)
            assertEquals(101, state.followerCount)
        }

        coVerify { userRepository.followUser("user-123") }
    }

    @Test
    fun `toggleFollow reverts on API failure`() = runTest {
        val userProfile = createMockUserProfile("user-123", isFollowing = false)
        coEvery { userRepository.getUserProfile("user-123") } returns flowOf(Result.Success(userProfile))
        coEvery { userRepository.getUserRecipes(any(), any()) } returns flowOf(Result.Success(PaginatedResponse(emptyList(), null, false)))
        coEvery { userRepository.followUser("user-123") } returns Result.Error(Exception("Failed"))

        viewModel = ProfileViewModel(userRepository)
        viewModel.loadProfile("user-123")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFollow()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isFollowing)
            assertEquals(100, state.followerCount)
        }
    }

    @Test
    fun `selectTab changes selected tab and loads content`() = runTest {
        val userProfile = createMockUserProfile("user-123")
        coEvery { userRepository.getUserProfile("user-123") } returns flowOf(Result.Success(userProfile))
        coEvery { userRepository.getUserRecipes(any(), any()) } returns flowOf(Result.Success(PaginatedResponse(emptyList(), null, false)))
        coEvery { userRepository.getUserLogs(any(), any()) } returns flowOf(Result.Success(PaginatedResponse(emptyList(), null, false)))

        viewModel = ProfileViewModel(userRepository)
        viewModel.loadProfile("user-123")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectTab(ProfileTab.LOGS)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(ProfileTab.LOGS, state.selectedTab)
        }

        coVerify { userRepository.getUserLogs("user-123", null) }
    }

    private fun createMockMyProfile() = MyProfile(
        id = "me-1",
        username = "myuser",
        displayName = "My Name",
        email = "test@example.com",
        avatarUrl = null,
        bio = "Test bio",
        level = 5,
        xp = 500,
        levelProgress = 0.5,
        recipeCount = 10,
        logCount = 25,
        followerCount = 100,
        followingCount = 50,
        socialLinks = null,
        createdAt = LocalDateTime.now()
    )

    private fun createMockUserProfile(id: String, isFollowing: Boolean = false) = UserProfile(
        id = id,
        username = "testuser",
        displayName = "Test User",
        avatarUrl = null,
        bio = "Test bio",
        level = 10,
        recipeCount = 5,
        logCount = 20,
        followerCount = 100,
        followingCount = 50,
        socialLinks = null,
        isFollowing = isFollowing,
        isFollowedBy = false,
        isBlocked = false,
        createdAt = LocalDateTime.now()
    )
}
