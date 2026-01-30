package com.cookstemma.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookstemma.app.data.auth.TokenManager
import com.cookstemma.app.data.repository.UserRepository
import com.cookstemma.app.domain.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppTheme(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val currentLanguage: String = "English",
    val appVersion: String = "1.0.0",
    val isLoggingOut: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val logoutSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        _uiState.update { it.copy(appVersion = packageInfo.versionName ?: "1.0.0") }
    }

    fun setTheme(theme: AppTheme) {
        _uiState.update { it.copy(appTheme = theme) }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, error = null) }
            try {
                tokenManager.clearTokens()
                _uiState.update { it.copy(isLoggingOut = false, logoutSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoggingOut = false, error = e.message) }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, error = null) }
            when (val result = userRepository.deleteAccount()) {
                is Result.Success -> {
                    tokenManager.clearTokens()
                    _uiState.update { it.copy(isDeletingAccount = false, deleteSuccess = true) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isDeletingAccount = false, error = result.exception.message) }
                }
                is Result.Loading -> { }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
