package com.cookstemma.app.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommonModelTest {

    // MARK: - PaginatedResponse Tests

    @Test
    fun `PaginatedResponse with content and more pages`() {
        // Given
        val response = PaginatedResponse(
            content = listOf("item1", "item2", "item3"),
            nextCursor = "cursor123",
            hasMore = true
        )

        // Then
        assertEquals(3, response.content.size)
        assertEquals("cursor123", response.nextCursor)
        assertTrue(response.hasMore)
    }

    @Test
    fun `PaginatedResponse last page`() {
        // Given
        val response = PaginatedResponse(
            content = listOf("item1", "item2"),
            nextCursor = null,
            hasMore = false
        )

        // Then
        assertEquals(2, response.content.size)
        assertNull(response.nextCursor)
        assertFalse(response.hasMore)
    }

    @Test
    fun `PaginatedResponse empty content`() {
        // Given
        val response = PaginatedResponse<String>(
            content = emptyList(),
            nextCursor = null,
            hasMore = false
        )

        // Then
        assertTrue(response.content.isEmpty())
        assertFalse(response.hasMore)
    }

    @Test
    fun `PaginatedResponse with typed content`() {
        // Given
        data class Item(val id: Int, val name: String)

        val items = listOf(
            Item(1, "First"),
            Item(2, "Second")
        )

        val response = PaginatedResponse(
            content = items,
            nextCursor = "next",
            hasMore = true
        )

        // Then
        assertEquals(2, response.content.size)
        assertEquals(1, response.content[0].id)
        assertEquals("Second", response.content[1].name)
    }

    // MARK: - Result Tests

    @Test
    fun `Result Success holds data`() {
        // Given
        val result: Result<String> = Result.Success("test data")

        // Then
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertFalse(result.isLoading)
        assertEquals("test data", result.getOrNull())
        assertNull(result.exceptionOrNull())
    }

    @Test
    fun `Result Error holds exception`() {
        // Given
        val exception = RuntimeException("Test error")
        val result: Result<String> = Result.Error(exception)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.isError)
        assertFalse(result.isLoading)
        assertNull(result.getOrNull())
        assertEquals(exception, result.exceptionOrNull())
        assertEquals("Test error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `Result Loading state`() {
        // Given
        val result: Result<String> = Result.Loading

        // Then
        assertFalse(result.isSuccess)
        assertFalse(result.isError)
        assertTrue(result.isLoading)
        assertNull(result.getOrNull())
        assertNull(result.exceptionOrNull())
    }

    @Test
    fun `Result map transforms success data`() {
        // Given
        val result: Result<Int> = Result.Success(5)

        // When
        val mapped = result.map { it * 2 }

        // Then
        assertTrue(mapped.isSuccess)
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun `Result map preserves error`() {
        // Given
        val exception = RuntimeException("Error")
        val result: Result<Int> = Result.Error(exception)

        // When
        val mapped = result.map { it * 2 }

        // Then
        assertTrue(mapped.isError)
        assertEquals(exception, mapped.exceptionOrNull())
    }

    @Test
    fun `Result map preserves loading`() {
        // Given
        val result: Result<Int> = Result.Loading

        // When
        val mapped = result.map { it * 2 }

        // Then
        assertTrue(mapped.isLoading)
    }

    @Test
    fun `Result onSuccess executes action on success`() {
        // Given
        var executed = false
        var capturedValue: String? = null
        val result: Result<String> = Result.Success("value")

        // When
        result.onSuccess {
            executed = true
            capturedValue = it
        }

        // Then
        assertTrue(executed)
        assertEquals("value", capturedValue)
    }

    @Test
    fun `Result onSuccess does not execute on error`() {
        // Given
        var executed = false
        val result: Result<String> = Result.Error(RuntimeException())

        // When
        result.onSuccess { executed = true }

        // Then
        assertFalse(executed)
    }

    @Test
    fun `Result onSuccess does not execute on loading`() {
        // Given
        var executed = false
        val result: Result<String> = Result.Loading

        // When
        result.onSuccess { executed = true }

        // Then
        assertFalse(executed)
    }

    @Test
    fun `Result onError executes action on error`() {
        // Given
        var executed = false
        var capturedException: Throwable? = null
        val exception = RuntimeException("Test")
        val result: Result<String> = Result.Error(exception)

        // When
        result.onError {
            executed = true
            capturedException = it
        }

        // Then
        assertTrue(executed)
        assertEquals(exception, capturedException)
    }

    @Test
    fun `Result onError does not execute on success`() {
        // Given
        var executed = false
        val result: Result<String> = Result.Success("value")

        // When
        result.onError { executed = true }

        // Then
        assertFalse(executed)
    }

    @Test
    fun `Result onError does not execute on loading`() {
        // Given
        var executed = false
        val result: Result<String> = Result.Loading

        // When
        result.onError { executed = true }

        // Then
        assertFalse(executed)
    }

    @Test
    fun `Result chaining onSuccess and onError`() {
        // Given
        var successValue: String? = null
        var errorMessage: String? = null

        val successResult: Result<String> = Result.Success("data")
        val errorResult: Result<String> = Result.Error(RuntimeException("error"))

        // When
        successResult
            .onSuccess { successValue = it }
            .onError { errorMessage = it.message }

        errorResult
            .onSuccess { successValue = "should not happen" }
            .onError { errorMessage = it.message }

        // Then
        assertEquals("data", successValue)
        assertEquals("error", errorMessage)
    }

    @Test
    fun `Result map with type transformation`() {
        // Given
        data class User(val id: Int, val name: String)
        data class UserDto(val userId: String, val displayName: String)

        val user = User(1, "John")
        val result: Result<User> = Result.Success(user)

        // When
        val dtoResult = result.map { UserDto("user_${it.id}", it.name.uppercase()) }

        // Then
        assertTrue(dtoResult.isSuccess)
        assertEquals("user_1", dtoResult.getOrNull()?.userId)
        assertEquals("JOHN", dtoResult.getOrNull()?.displayName)
    }

    // MARK: - Pattern Matching Tests

    @Test
    fun `Result when expression exhaustive matching`() {
        // Given
        val successResult: Result<Int> = Result.Success(42)
        val errorResult: Result<Int> = Result.Error(RuntimeException())
        val loadingResult: Result<Int> = Result.Loading

        // When / Then
        val successMessage = when (successResult) {
            is Result.Success -> "Got ${successResult.data}"
            is Result.Error -> "Error"
            is Result.Loading -> "Loading"
        }
        assertEquals("Got 42", successMessage)

        val errorMessage = when (errorResult) {
            is Result.Success -> "Success"
            is Result.Error -> "Error occurred"
            is Result.Loading -> "Loading"
        }
        assertEquals("Error occurred", errorMessage)

        val loadingMessage = when (loadingResult) {
            is Result.Success -> "Success"
            is Result.Error -> "Error"
            is Result.Loading -> "Loading..."
        }
        assertEquals("Loading...", loadingMessage)
    }

    @Test
    fun `Result smart cast in when expression`() {
        // Given
        val result: Result<List<String>> = Result.Success(listOf("a", "b", "c"))

        // When / Then
        when (result) {
            is Result.Success -> {
                // Smart cast to Result.Success<List<String>>
                assertEquals(3, result.data.size)
                assertEquals("a", result.data[0])
            }
            is Result.Error -> {
                // Smart cast to Result.Error
                assertNull(result.exception.message)
            }
            is Result.Loading -> {
                // No additional data
            }
        }
    }
}
