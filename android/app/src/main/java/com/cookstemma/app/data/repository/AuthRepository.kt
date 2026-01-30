package com.cookstemma.app.data.repository

import com.cookstemma.app.data.api.ApiService
import com.cookstemma.app.data.api.AuthResponse
import com.cookstemma.app.data.api.LoginRequest
import com.cookstemma.app.data.api.RefreshTokenRequest
import com.cookstemma.app.data.auth.TokenManager
import com.cookstemma.app.domain.model.MyProfile
import com.cookstemma.app.domain.model.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    data object Unknown : AuthState()
    data class Authenticated(val user: MyProfile) : AuthState()
    data object Unauthenticated : AuthState()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val isAuthenticated: Boolean
        get() = _authState.value is AuthState.Authenticated

    val currentUser: MyProfile?
        get() = (_authState.value as? AuthState.Authenticated)?.user

    init {
        // Check initial auth state
        if (tokenManager.hasValidToken()) {
            // Will need to fetch profile to confirm
            _authState.value = AuthState.Unknown
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun loginWithFirebase(firebaseToken: String, locale: String = "en"): Flow<Result<MyProfile>> = flow {
        try {
            // Exchange Firebase token for app tokens
            val authResponse = apiService.login(LoginRequest(firebaseToken, locale))

            // Save tokens
            tokenManager.saveTokens(
                accessToken = authResponse.accessToken,
                refreshToken = authResponse.refreshToken,
                expiresIn = authResponse.expiresIn
            )

            // Fetch user profile
            val profile = apiService.getMyProfile().toDomain()
            _authState.value = AuthState.Authenticated(profile)

            emit(Result.Success(profile))
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            emit(Result.Error(e))
        }
    }

    fun logout(): Flow<Result<Unit>> = flow {
        try {
            // Call server logout endpoint
            try {
                apiService.logout()
            } catch (e: Exception) {
                // Ignore server errors - still proceed with local logout
            }

            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()

            // Clear app tokens
            tokenManager.clearTokens()
            _authState.value = AuthState.Unauthenticated
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun checkAuthState(): Flow<Result<AuthState>> = flow {
        try {
            if (!tokenManager.hasValidToken()) {
                _authState.value = AuthState.Unauthenticated
                emit(Result.Success(AuthState.Unauthenticated))
                return@flow
            }

            // Try to fetch profile to verify token is still valid
            val profile = apiService.getMyProfile().toDomain()
            _authState.value = AuthState.Authenticated(profile)
            emit(Result.Success(AuthState.Authenticated(profile)))
        } catch (e: Exception) {
            // Token is invalid or expired
            tokenManager.clearTokens()
            _authState.value = AuthState.Unauthenticated
            emit(Result.Success(AuthState.Unauthenticated))
        }
    }

    fun refreshUserProfile(): Flow<Result<MyProfile>> = flow {
        try {
            val profile = apiService.getMyProfile().toDomain()
            _authState.value = AuthState.Authenticated(profile)
            emit(Result.Success(profile))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun refreshAccessToken(): Boolean {
        return try {
            val refreshToken = tokenManager.getRefreshToken() ?: return false
            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))

            tokenManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                expiresIn = response.expiresIn
            )
            true
        } catch (e: Exception) {
            tokenManager.clearTokens()
            _authState.value = AuthState.Unauthenticated
            false
        }
    }
}
