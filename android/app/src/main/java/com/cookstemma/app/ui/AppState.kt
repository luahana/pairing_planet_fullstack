package com.cookstemma.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * App-level state management for authentication flows.
 * 
 * This class manages the login sheet visibility and pending auth actions,
 * following the iOS pattern for conditional sign-in.
 */
class AppState {
    /** Whether the login bottom sheet should be shown */
    var showLoginSheet by mutableStateOf(false)
        private set
    
    /** Action to perform after successful login */
    private var pendingAuthAction: (() -> Unit)? = null
    
    /**
     * Requires authentication before performing an action.
     * If user is authenticated, executes the action immediately.
     * If not, shows login sheet and executes action after successful login.
     * 
     * @param isAuthenticated Current authentication state
     * @param action Action to perform after authentication
     */
    fun requireAuth(isAuthenticated: Boolean, action: () -> Unit) {
        if (isAuthenticated) {
            action()
        } else {
            pendingAuthAction = action
            showLoginSheet = true
        }
    }
    
    /**
     * Called when login is successful. Executes pending action if any.
     */
    fun onLoginSuccess() {
        showLoginSheet = false
        pendingAuthAction?.invoke()
        pendingAuthAction = null
    }
    
    /**
     * Called when login is dismissed without success.
     */
    fun onLoginDismissed() {
        showLoginSheet = false
        pendingAuthAction = null
    }
}

/**
 * Remember and create an AppState instance.
 */
@Composable
fun rememberAppState(): AppState {
    return remember { AppState() }
}
