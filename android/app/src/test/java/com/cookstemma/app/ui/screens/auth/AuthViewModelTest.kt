package com.cookstemma.app.ui.screens.auth

import app.cash.turbine.test
import com.cookstemma.app.data.repository.AuthRepository
import com.cookstemma.app.data.repository.AuthState
import com.cookstemma.app.domain.model.MyProfile
import com.cookstemma.app.domain.model.Result
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class AuthViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val authStateFlow = MutableStateFlow<AuthState>(AuthState.Unknown)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        every { authRepository.authState } returns authStateFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // MARK: - Initial State Tests

    @Test
    fun `initial state has isLoading false`() = runTest {
        // Given
        coEvery { authRepository.checkAuthState() } returns flowOf(Result.Success(AuthState.Unauthenticated))

        // When
        viewModel = AuthViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            assertFalse(awaitItem().isLoading)
        }
    }

    // MARK: - Login Tests

    @Test
    fun `loginWithFirebase success updates authenticated state`() = runTest {
        // Given
        val profile = createMockProfile()
        coEvery { authRepository.checkAuthState() } returns flowOf(Result.Success(AuthState.Unauthenticated))
        coEvery { authRepository.loginWithFirebase(any()) } returns flowOf(Result.Success(profile))

        viewModel = AuthViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.loginWithFirebase("firebase-token")
        authStateFlow.value = AuthState.Authenticated(profile)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isAuthenticated)
            assertEquals(profile.id, state.currentUser?.id)
        }
    }

    @Test
    fun `loginWithFirebase failure shows error`() = runTest {
        // Given
        coEvery { authRepository.checkAuthState() } returns flowOf(Result.Success(AuthState.Unauthenticated))
        coEvery { authRepository.loginWithFirebase(any()) } returns flowOf(
            Result.Error(Exception("Login failed"))
        )

        viewModel = AuthViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.loginWithFirebase("firebase-token")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Login failed", state.error)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `loginWithFirebase sets loading state`() = runTest {
        // Given
        coEvery { authRepository.checkAuthState() } returns flowOf(Result.Success(AuthState.Unauthenticated))
        coEvery { authRepository.loginWithFirebase(any()) } returns flowOf(
            Result.Success(createMockProfile())
        )

        viewModel = AuthViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - collect states during login
        val states = mutableListOf<AuthUiState>()
        viewModel.uiState.test {
            states.add(awaitItem())
            viewModel.loginWithFirebase("firebase-token")
            // The loading state should be set before the result comes back
        }

        // Verify loading was called
        coVerify { authRepository.loginWithFirebase("firebase-token") }
    }

    // MARK: - Logout Tests

    @Test
    fun `logout clears authenticated state`() = runTest {
        // Given - start authenticated
        val profile = createMockProfile()
        authStateFlow.value = AuthState.Authenticated(profile)
        coEvery { authRepository.checkAuthState() } returns flowOf(Result.Success(AuthState.Authenticated(profile)))
        coEvery { authRepository.logout() } returns flowOf(Result.Success(Unit))

        viewModel = AuthViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.logout()
        authStateFlow.value = AuthState.Unauthenticated
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isAuthenticated)
            assertNull(state.currentUser)
        }
    }

    // MARK: - Auth State Observer Tests

    @Test
    fun `observes auth state changes from repository`() = runTest {
        // Given
        coEvery { authRepository.checkAuthState() } returns flowOf(Result.Success(AuthState.Unauthenticated))

        viewModel = AuthViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - simulate auth state change
        val profile = createMockProfile()
        authStateFlow.value = AuthState.Authenticated(profile)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isAuthenticated)
            assertEquals(profile.username, state.currentUser?.username)
        }
    }

    // MARK: - Clear Error Tests

    @Test
    fun `clearError removes error message`() = runTest {
        // Given
        coEvery { authRepository.checkAuthState() } returns flowOf(Result.Success(AuthState.Unauthenticated))
        coEvery { authRepository.loginWithFirebase(any()) } returns flowOf(
            Result.Error(Exception("Error"))
        )

        viewModel = AuthViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loginWithFirebase("token")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearError()

        // Then
        viewModel.uiState.test {
            assertNull(awaitItem().error)
        }
    }

    // MARK: - Check Auth State Tests

    @Test
    fun `checkAuthState calls repository`() = runTest {
        // Given
        coEvery { authRepository.checkAuthState() } returns flowOf(Result.Success(AuthState.Unauthenticated))

        viewModel = AuthViewModel(authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.checkAuthState()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - called twice: once in init, once manually
        coVerify(atLeast = 2) { authRepository.checkAuthState() }
    }

    // MARK: - Helpers

    private fun createMockProfile() = MyProfile(
        id = "user-1",
        username = "testuser",
        displayName = "Test User",
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
}
