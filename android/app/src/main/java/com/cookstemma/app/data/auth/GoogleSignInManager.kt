package com.cookstemma.app.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of a Google Sign-In operation.
 */
sealed class GoogleSignInResult {
    data class Success(val firebaseIdToken: String) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
}

/**
 * Manages Google Sign-In using the Credential Manager API.
 * 
 * This class handles:
 * 1. Requesting Google ID token via Credential Manager
 * 2. Exchanging the Google ID token for Firebase credential
 * 3. Signing in with Firebase and returning the Firebase ID token
 */
@Singleton
class GoogleSignInManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Web client ID from Firebase console (google-services.json)
    // This is the OAuth 2.0 client ID for the web application type
    private val webClientId: String by lazy {
        context.getString(
            context.resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName
            )
        )
    }

    /**
     * Signs in with Google and returns the Firebase ID token.
     * 
     * The flow is:
     * 1. Show Google account picker via Credential Manager
     * 2. Get Google ID token
     * 3. Exchange for Firebase credential
     * 4. Sign in to Firebase
     * 5. Return Firebase ID token to exchange with backend
     */
    suspend fun signIn(activityContext: Context): GoogleSignInResult {
        return try {
            // Build Google ID option for Credential Manager
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            // Build credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Request credential (shows account picker)
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            // Handle the result
            handleSignInResult(result)
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: GetCredentialException) {
            GoogleSignInResult.Error(e.message ?: "Failed to get credential")
        } catch (e: Exception) {
            GoogleSignInResult.Error(e.message ?: "Unknown error during sign in")
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse): GoogleSignInResult {
        val credential = result.credential

        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Extract Google ID token
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val googleIdToken = googleIdTokenCredential.idToken

                        // Exchange Google credential for Firebase credential
                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                        // Sign in to Firebase
                        val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                        
                        // Get Firebase ID token
                        val firebaseUser = authResult.user
                            ?: return GoogleSignInResult.Error("Firebase sign in failed: no user")
                        
                        val firebaseIdToken = firebaseUser.getIdToken(true).await().token
                            ?: return GoogleSignInResult.Error("Failed to get Firebase ID token")

                        GoogleSignInResult.Success(firebaseIdToken)
                    } catch (e: Exception) {
                        GoogleSignInResult.Error(e.message ?: "Failed to process Google credential")
                    }
                } else {
                    GoogleSignInResult.Error("Unexpected credential type: ${credential.type}")
                }
            }
            else -> GoogleSignInResult.Error("Unexpected credential type")
        }
    }

    /**
     * Signs out from Firebase.
     * Call this when the user logs out from the app.
     */
    fun signOut() {
        firebaseAuth.signOut()
    }
}
