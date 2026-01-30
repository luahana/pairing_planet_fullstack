package com.cookstemma.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Splash screen displayed while authentication state is being determined.
 * 
 * Shows a simple background matching the app theme for a seamless
 * transition from the launch screen.
 */
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Empty - just shows background color
        // This matches iOS behavior of showing Color(.systemBackground)
        // for a seamless transition from LaunchScreen
    }
}
