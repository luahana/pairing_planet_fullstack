package com.cookstemma.app.data.auth

import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var tokenManager: TokenManager
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        tokenManager = mockk(relaxed = true)
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `interceptor adds auth header when token is present`() {
        // Given
        every { tokenManager.getAccessToken() } returns "test-access-token"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        // When
        client.newCall(
            Request.Builder()
                .url(mockWebServer.url("/api/v1/users/me"))
                .build()
        ).execute()

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("Bearer test-access-token", request.getHeader("Authorization"))
    }

    @Test
    fun `interceptor does not add auth header when token is null`() {
        // Given
        every { tokenManager.getAccessToken() } returns null

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        // When
        client.newCall(
            Request.Builder()
                .url(mockWebServer.url("/api/v1/users/me"))
                .build()
        ).execute()

        // Then
        val request = mockWebServer.takeRequest()
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun `interceptor skips auth header for auth endpoints`() {
        // Given
        every { tokenManager.getAccessToken() } returns "test-access-token"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        // When
        client.newCall(
            Request.Builder()
                .url(mockWebServer.url("/auth/login"))
                .build()
        ).execute()

        // Then
        val request = mockWebServer.takeRequest()
        assertNull(request.getHeader("Authorization"))
    }

    @Test
    fun `interceptor preserves original request headers`() {
        // Given
        every { tokenManager.getAccessToken() } returns "test-token"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        // When
        client.newCall(
            Request.Builder()
                .url(mockWebServer.url("/api/v1/test"))
                .header("Custom-Header", "custom-value")
                .build()
        ).execute()

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("custom-value", request.getHeader("Custom-Header"))
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
    }

    @Test
    fun `interceptor preserves request method and body`() {
        // Given
        every { tokenManager.getAccessToken() } returns "test-token"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
        )

        val requestBody = okhttp3.RequestBody.create(
            okhttp3.MediaType.parse("application/json"),
            """{"test":"data"}"""
        )

        // When
        client.newCall(
            Request.Builder()
                .url(mockWebServer.url("/api/v1/test"))
                .post(requestBody)
                .build()
        ).execute()

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("""{"test":"data"}""", request.body.readUtf8())
    }
}
