package com.cookstemma.app.data.repository

import app.cash.turbine.test
import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.data.api.AuthResponse
import com.cookstemma.app.data.api.LoginRequest
import com.cookstemma.app.data.auth.TokenManager
import com.cookstemma.app.domain.model.MyProfile
import com.cookstemma.app.domain.model.Result
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        apiService = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        every { tokenManager.hasValidToken() } returns false
        authRepository = AuthRepository(apiService, tokenManager)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // MARK: - Login Tests

    @Test
    fun `loginWithFirebase success saves tokens and fetches profile`() = runTest {
        // Given
        val authResponse = AuthResponse(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 3600
        )
        val profile = createMockProfile()

        coEvery { apiService.login(any()) } returns authResponse
        coEvery { apiService.getMyProfile() } returns profile

        // When
        authRepository.loginWithFirebase("firebase-token").test {
            val result = awaitItem()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(profile.id, (result as Result.Success).data.id)
            awaitComplete()
        }

        // Verify tokens were saved
        verify {
            tokenManager.saveTokens(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                expiresIn = 3600
            )
        }
    }

    @Test
    fun `loginWithFirebase failure emits error`() = runTest {
        // Given
        coEvery { apiService.login(any()) } throws Exception("Network error")

        // When
        authRepository.loginWithFirebase("firebase-token").test {
            val result = awaitItem()

            // Then
            assertTrue(result is Result.Error)
            assertEquals("Network error", (result as Result.Error).exception.message)
            awaitComplete()
        }
    }

    @Test
    fun `loginWithFirebase updates auth state to authenticated`() = runTest {
        // Given
        val authResponse = AuthResponse(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 3600
        )
        val profile = createMockProfile()

        coEvery { apiService.login(any()) } returns authResponse
        coEvery { apiService.getMyProfile() } returns profile

        // When
        authRepository.loginWithFirebase("firebase-token").test {
            awaitItem()
            awaitComplete()
        }

        // Then
        assertTrue(authRepository.authState.value is AuthState.Authenticated)
        assertEquals(profile.id, authRepository.currentUser?.id)
    }

    // MARK: - Logout Tests

    @Test
    fun `logout clears tokens`() = runTest {
        // When
        authRepository.logout().test {
            awaitItem()
            awaitComplete()
        }

        // Then
        verify { tokenManager.clearTokens() }
    }

    @Test
    fun `logout sets auth state to unauthenticated`() = runTest {
        // When
        authRepository.logout().test {
            awaitItem()
            awaitComplete()
        }

        // Then
        assertEquals(AuthState.Unauthenticated, authRepository.authState.value)
        assertFalse(authRepository.isAuthenticated)
    }

    // MARK: - Check Auth State Tests

    @Test
    fun `checkAuthState with no token returns unauthenticated`() = runTest {
        // Given
        every { tokenManager.hasValidToken() } returns false

        // When
        authRepository.checkAuthState().test {
            val result = awaitItem()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(AuthState.Unauthenticated, (result as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `checkAuthState with valid token fetches profile`() = runTest {
        // Given
        every { tokenManager.hasValidToken() } returns true
        val profile = createMockProfile()
        coEvery { apiService.getMyProfile() } returns profile

        // When
        authRepository.checkAuthState().test {
            val result = awaitItem()

            // Then
            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data is AuthState.Authenticated)
            awaitComplete()
        }
    }

    @Test
    fun `checkAuthState with expired token clears tokens`() = runTest {
        // Given
        every { tokenManager.hasValidToken() } returns true
        coEvery { apiService.getMyProfile() } throws Exception("Unauthorized")

        // When
        authRepository.checkAuthState().test {
            awaitItem()
            awaitComplete()
        }

        // Then
        verify { tokenManager.clearTokens() }
        assertEquals(AuthState.Unauthenticated, authRepository.authState.value)
    }

    // MARK: - Refresh Profile Tests

    @Test
    fun `refreshUserProfile updates current user`() = runTest {
        // Given
        val profile = createMockProfile()
        coEvery { apiService.getMyProfile() } returns profile

        // When
        authRepository.refreshUserProfile().test {
            val result = awaitItem()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(profile.username, (result as Result.Success).data.username)
            awaitComplete()
        }
    }

    // MARK: - Current User Tests

    @Test
    fun `currentUser returns null when unauthenticated`() {
        // Given - auth state is unauthenticated by default

        // Then
        assertNull(authRepository.currentUser)
    }

    @Test
    fun `isAuthenticated returns false when unauthenticated`() {
        // Given - auth state is unauthenticated by default

        // Then
        assertFalse(authRepository.isAuthenticated)
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
