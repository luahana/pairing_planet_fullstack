package com.cookstemma.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.cookstemma.app.data.auth.GoogleSignInManager
import com.cookstemma.app.data.local.ThemePreferencesDataStore
import com.cookstemma.app.ui.navigation.CookstemmaNavHost
import com.cookstemma.app.ui.screens.settings.AppTheme
import com.cookstemma.app.ui.theme.CookstemmaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var googleSignInManager: GoogleSignInManager

    @Inject
    lateinit var themePreferencesDataStore: ThemePreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreference by themePreferencesDataStore.themePreference.collectAsState(
                initial = themePreferencesDataStore.currentTheme
            )
            val isDarkTheme = when (themePreference) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            CookstemmaTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CookstemmaNavHost(googleSignInManager = googleSignInManager)
                }
            }
        }
    }
}
