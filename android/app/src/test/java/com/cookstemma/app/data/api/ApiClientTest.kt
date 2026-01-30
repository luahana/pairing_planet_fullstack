package com.cookstemma.app.data.api

import com.cookstemma.app.data.auth.AuthInterceptor
import com.cookstemma.app.data.auth.TokenManager
import com.cookstemma.app.domain.model.PaginatedResponse
import com.cookstemma.app.domain.model.UserSummary
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        tokenManager = mockk(relaxed = true)
        every { tokenManager.getAccessToken() } returns "test-token"

        val authInterceptor = AuthInterceptor(tokenManager)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // MARK: - Successful Response Tests

    @Test
    fun `getRecipes returns paginated response on success`() = runTest {
        // Given
        val responseBody = """
            {
                "items": [
                    {
                        "id": "recipe-1",
                        "title": "Test Recipe",
                        "coverImageUrl": null,
                        "cookingTimeRange": "UNDER_30",
                        "cookCount": 10,
                        "averageRating": 4.5,
                        "author": {
                            "id": "user-1",
                            "username": "testuser",
                            "displayName": "Test User",
                            "avatarUrl": null,
                            "level": 5
                        }
                    }
                ],
                "nextCursor": "cursor-123",
                "hasMore": true
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseBody)
        )

        // When
        val result = apiService.getRecipes()

        // Then
        assertEquals(1, result.items.size)
        assertEquals("recipe-1", result.items[0].id)
        assertEquals("Test Recipe", result.items[0].title)
        assertEquals("cursor-123", result.nextCursor)
        assertTrue(result.hasMore)
    }

    @Test
    fun `getRecipe returns recipe detail on success`() = runTest {
        // Given
        val responseBody = """
            {
                "id": "recipe-1",
                "title": "Test Recipe",
                "description": "A test recipe",
                "images": ["https://example.com/image.jpg"],
                "ingredients": [],
                "steps": [],
                "cookingTimeRange": "UNDER_30",
                "servings": 2,
                "cookCount": 10,
                "averageRating": 4.5,
                "isSaved": false,
                "hashtags": ["test"],
                "author": {
                    "id": "user-1",
                    "username": "testuser",
                    "displayName": "Test User",
                    "avatarUrl": null,
                    "level": 5
                },
                "createdAt": "2024-01-01T00:00:00Z",
                "updatedAt": "2024-01-01T00:00:00Z"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseBody)
        )

        // When
        val result = apiService.getRecipe("recipe-1")

        // Then
        assertEquals("recipe-1", result.id)
        assertEquals("Test Recipe", result.title)
        assertEquals(2, result.servings)
    }

    // MARK: - Auth Header Tests

    @Test
    fun `request includes auth header when token is present`() = runTest {
        // Given
        every { tokenManager.getAccessToken() } returns "bearer-token-123"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("""{"items":[],"nextCursor":null,"hasMore":false}""")
        )

        // When
        apiService.getRecipes()

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("Bearer bearer-token-123", request.getHeader("Authorization"))
    }

    @Test
    fun `request excludes auth header for auth endpoints`() = runTest {
        // Given - token should not be added for auth paths
        every { tokenManager.getAccessToken() } returns "some-token"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("""{"accessToken":"new","refreshToken":"refresh","expiresIn":3600}""")
        )

        // When
        apiService.login(LoginRequest("firebase-token"))

        // Then
        val request = mockWebServer.takeRequest()
        // AuthInterceptor skips auth headers for /auth/ paths
        assertTrue(request.path?.contains("/auth/") == true)
    }

    // MARK: - Error Response Tests

    @Test
    fun `request throws on 401 unauthorized`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setBody("""{"error":"Unauthorized"}""")
        )

        // When/Then
        assertFailsWith<retrofit2.HttpException> {
            apiService.getRecipes()
        }
    }

    @Test
    fun `request throws on 404 not found`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                .setBody("""{"error":"Not found"}""")
        )

        // When/Then
        assertFailsWith<retrofit2.HttpException> {
            apiService.getRecipe("non-existent")
        }
    }

    @Test
    fun `request throws on 500 server error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody("""{"error":"Server error"}""")
        )

        // When/Then
        assertFailsWith<retrofit2.HttpException> {
            apiService.getRecipes()
        }
    }

    // MARK: - Query Parameters Tests

    @Test
    fun `request includes query parameters`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("""{"items":[],"nextCursor":null,"hasMore":false}""")
        )

        // When
        apiService.getRecipes(
            cursor = "abc123",
            cookingTimeRange = "UNDER_30",
            sort = "newest"
        )

        // Then
        val request = mockWebServer.takeRequest()
        assertTrue(request.path?.contains("cursor=abc123") == true)
        assertTrue(request.path?.contains("cookingTimeRange=UNDER_30") == true)
        assertTrue(request.path?.contains("sort=newest") == true)
    }

    // MARK: - POST Body Tests

    @Test
    fun `post request includes json body`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody("""{"accessToken":"token","refreshToken":"refresh","expiresIn":3600}""")
        )

        // When
        apiService.login(LoginRequest("firebase-token"))

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.getHeader("Content-Type")?.contains("application/json") == true)
        assertTrue(request.body.readUtf8().contains("firebase-token"))
    }

    // MARK: - User Endpoints Tests

    @Test
    fun `getMyProfile returns user profile`() = runTest {
        // Given
        val responseBody = """
            {
                "id": "user-1",
                "username": "myuser",
                "displayName": "My Name",
                "email": "test@example.com",
                "avatarUrl": null,
                "bio": "Test bio",
                "level": 5,
                "xp": 500,
                "levelProgress": 0.5,
                "recipeCount": 10,
                "logCount": 25,
                "followerCount": 100,
                "followingCount": 50,
                "socialLinks": null,
                "createdAt": "2024-01-01T00:00:00Z"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseBody)
        )

        // When
        val result = apiService.getMyProfile()

        // Then
        assertEquals("user-1", result.id)
        assertEquals("myuser", result.username)
        assertEquals(5, result.level)
    }

    @Test
    fun `followUser sends POST request`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        // When
        apiService.followUser("user-123")

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("/users/user-123/follow") == true)
    }

    @Test
    fun `unfollowUser sends DELETE request`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        // When
        apiService.unfollowUser("user-123")

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertTrue(request.path?.contains("/users/user-123/follow") == true)
    }

    // MARK: - Logs Endpoints Tests

    @Test
    fun `likeLog sends POST request`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        // When
        apiService.likeLog("log-123")

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("/logs/log-123/like") == true)
    }

    @Test
    fun `saveRecipe sends POST request`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        // When
        apiService.saveRecipe("recipe-123")

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path?.contains("/recipes/recipe-123/save") == true)
    }
}
