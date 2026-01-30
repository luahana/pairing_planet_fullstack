package com.cookstemma.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.repository.AuthRepository
import com.cookstemma.app.data.repository.AuthState
import com.cookstemma.app.domain.model.MyProfile
import com.cookstemma.app.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: MyProfile? = null,
    val error: String? = null,
    val isInitialCheckComplete: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Observe auth state changes
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        _uiState.update {
                            it.copy(
                                isAuthenticated = true,
                                currentUser = authState.user,
                                isLoading = false,
                                isInitialCheckComplete = true
                            )
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        _uiState.update {
                            it.copy(
                                isAuthenticated = false,
                                currentUser = null,
                                isLoading = false,
                                isInitialCheckComplete = true
                            )
                        }
                    }
                    is AuthState.Unknown -> {
                        // Initial check in progress
                    }
                }
            }
        }

        // Check initial auth state
        checkAuthState()
    }

    fun checkAuthState() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.checkAuthState().collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Ensure isLoading is set to false even if authState didn't change
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isInitialCheckComplete = true
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message,
                                isInitialCheckComplete = true
                            )
                        }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun loginWithFirebase(firebaseToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.loginWithFirebase(firebaseToken).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                currentUser = result.data
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Login failed"
                            )
                        }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            authRepository.logout().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                currentUser = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.exception.message
                            )
                        }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(error = message, isLoading = false) }
    }
}
