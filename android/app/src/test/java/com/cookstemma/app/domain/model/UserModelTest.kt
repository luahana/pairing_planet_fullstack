package com.cookstemma.app.domain.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserModelTest {

    private lateinit var gson: Gson

    @Before
    fun setup() {
        gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            .create()
    }

    // MARK: - UserSummary Tests

    @Test
    fun `UserSummary parses from JSON`() {
        // Given
        val json = """
        {
            "id": "user-123",
            "username": "chefkim",
            "displayName": "Chef Kim",
            "avatarUrl": "https://example.com/avatar.jpg"
        }
        """.trimIndent()

        // When
        val user = gson.fromJson(json, UserSummary::class.java)

        // Then
        assertEquals("user-123", user.id)
        assertEquals("chefkim", user.username)
        assertEquals("Chef Kim", user.displayName)
        assertEquals("https://example.com/avatar.jpg", user.avatarUrl)
    }

    @Test
    fun `UserSummary handles null optional fields`() {
        // Given
        val json = """
        {
            "id": "user-456",
            "username": "newuser",
            "displayName": null,
            "avatarUrl": null
        }
        """.trimIndent()

        // When
        val user = gson.fromJson(json, UserSummary::class.java)

        // Then
        assertEquals("newuser", user.username)
        assertNull(user.displayName)
        assertNull(user.avatarUrl)
    }

    @Test
    fun `UserSummary displayNameOrUsername returns displayName when present`() {
        // Given
        val user = UserSummary(
            id = "user-1",
            username = "john_doe",
            displayName = "John Doe",
            avatarUrl = null
        )

        // Then
        assertEquals("John Doe", user.displayNameOrUsername)
    }

    @Test
    fun `UserSummary displayNameOrUsername falls back to username`() {
        // Given
        val user = UserSummary(
            id = "user-1",
            username = "jane_doe",
            displayName = null,
            avatarUrl = null
        )

        // Then
        assertEquals("jane_doe", user.displayNameOrUsername)
    }

    // MARK: - MyProfile Tests

    @Test
    fun `MyProfile parses from JSON`() {
        // Given
        val json = """
        {
            "id": "user-me",
            "username": "myusername",
            "displayName": "My Display Name",
            "email": "me@example.com",
            "avatarUrl": "https://example.com/myavatar.jpg",
            "bio": "I love cooking!",
            "level": 12,
            "xp": 2450,
            "levelProgress": 0.45,
            "recipeCount": 15,
            "logCount": 89,
            "followerCount": 1200,
            "followingCount": 350,
            "socialLinks": {
                "youtube": "https://youtube.com/@mychannel",
                "instagram": "@myinsta",
                "tiktok": "@mytiktok",
                "website": "https://myblog.com"
            },
            "createdAt": "2023-06-15T08:00:00"
        }
        """.trimIndent()

        // When
        val profile = gson.fromJson(json, MyProfile::class.java)

        // Then
        assertEquals("user-me", profile.id)
        assertEquals("myusername", profile.username)
        assertEquals("My Display Name", profile.displayName)
        assertEquals("me@example.com", profile.email)
        assertEquals("I love cooking!", profile.bio)
        assertEquals(12, profile.level)
        assertEquals(2450, profile.xp)
        assertEquals(0.45, profile.levelProgress, 0.001)
        assertEquals(15, profile.recipeCount)
        assertEquals(89, profile.logCount)
        assertEquals(1200, profile.followerCount)
        assertEquals(350, profile.followingCount)
        assertEquals("https://youtube.com/@mychannel", profile.socialLinks?.youtube)
        assertEquals("@myinsta", profile.socialLinks?.instagram)
    }

    @Test
    fun `MyProfile handles null optional fields`() {
        // Given
        val json = """
        {
            "id": "user-1",
            "username": "user",
            "displayName": null,
            "email": "user@example.com",
            "avatarUrl": null,
            "bio": null,
            "level": 1,
            "xp": 0,
            "levelProgress": 0.0,
            "recipeCount": 0,
            "logCount": 0,
            "followerCount": 0,
            "followingCount": 0,
            "socialLinks": null,
            "createdAt": "2024-01-01T00:00:00"
        }
        """.trimIndent()

        // When
        val profile = gson.fromJson(json, MyProfile::class.java)

        // Then
        assertNull(profile.displayName)
        assertNull(profile.avatarUrl)
        assertNull(profile.bio)
        assertNull(profile.socialLinks)
    }

    @Test
    fun `MyProfile level progress values`() {
        // Given - various progress scenarios
        val profiles = listOf(
            MyProfile("1", "u1", null, "e@e.com", null, null, 1, 0, 0.0, 0, 0, 0, 0, null, LocalDateTime.now()),
            MyProfile("2", "u2", null, "e@e.com", null, null, 5, 500, 0.5, 0, 0, 0, 0, null, LocalDateTime.now()),
            MyProfile("3", "u3", null, "e@e.com", null, null, 10, 999, 0.999, 0, 0, 0, 0, null, LocalDateTime.now()),
        )

        // Then
        assertEquals(0.0, profiles[0].levelProgress, 0.001)
        assertEquals(0.5, profiles[1].levelProgress, 0.001)
        assertEquals(0.999, profiles[2].levelProgress, 0.001)
    }

    // MARK: - UserProfile Tests

    @Test
    fun `UserProfile parses from JSON`() {
        // Given
        val json = """
        {
            "id": "user-other",
            "username": "otherusername",
            "displayName": "Other User",
            "avatarUrl": "https://example.com/other.jpg",
            "bio": "Food blogger and recipe creator",
            "level": 24,
            "recipeCount": 45,
            "logCount": 203,
            "followerCount": 5200,
            "followingCount": 150,
            "socialLinks": {
                "youtube": "https://youtube.com/@otheruser",
                "instagram": "@otheruser",
                "tiktok": null,
                "website": null
            },
            "isFollowing": true,
            "isFollowedBy": false,
            "isBlocked": false,
            "createdAt": "2022-03-01T00:00:00"
        }
        """.trimIndent()

        // When
        val profile = gson.fromJson(json, UserProfile::class.java)

        // Then
        assertEquals("user-other", profile.id)
        assertEquals("otherusername", profile.username)
        assertEquals("Other User", profile.displayName)
        assertEquals(24, profile.level)
        assertEquals(5200, profile.followerCount)
        assertTrue(profile.isFollowing)
        assertFalse(profile.isFollowedBy)
        assertFalse(profile.isBlocked)
    }

    @Test
    fun `UserProfile blocked user`() {
        // Given
        val profile = UserProfile(
            id = "user-blocked",
            username = "blockeduser",
            displayName = "Blocked User",
            avatarUrl = null,
            bio = null,
            level = 5,
            recipeCount = 10,
            logCount = 20,
            followerCount = 100,
            followingCount = 50,
            socialLinks = null,
            isFollowing = false,
            isFollowedBy = false,
            isBlocked = true,
            createdAt = LocalDateTime.now()
        )

        // Then
        assertTrue(profile.isBlocked)
        assertFalse(profile.isFollowing)
    }

    @Test
    fun `UserProfile mutual follow`() {
        // Given
        val profile = UserProfile(
            id = "user-mutual",
            username = "mutualfriend",
            displayName = "Mutual Friend",
            avatarUrl = null,
            bio = null,
            level = 10,
            recipeCount = 20,
            logCount = 50,
            followerCount = 500,
            followingCount = 300,
            socialLinks = null,
            isFollowing = true,
            isFollowedBy = true,
            isBlocked = false,
            createdAt = LocalDateTime.now()
        )

        // Then
        assertTrue(profile.isFollowing)
        assertTrue(profile.isFollowedBy)
        assertFalse(profile.isBlocked)
    }

    // MARK: - SocialLinks Tests

    @Test
    fun `SocialLinks parses all fields`() {
        // Given
        val json = """
        {
            "youtube": "https://youtube.com/@channel",
            "instagram": "@instahandle",
            "tiktok": "@tiktokhandle",
            "website": "https://mysite.com"
        }
        """.trimIndent()

        // When
        val links = gson.fromJson(json, SocialLinks::class.java)

        // Then
        assertEquals("https://youtube.com/@channel", links.youtube)
        assertEquals("@instahandle", links.instagram)
        assertEquals("@tiktokhandle", links.tiktok)
        assertEquals("https://mysite.com", links.website)
    }

    @Test
    fun `SocialLinks handles all null`() {
        // Given
        val json = """
        {
            "youtube": null,
            "instagram": null,
            "tiktok": null,
            "website": null
        }
        """.trimIndent()

        // When
        val links = gson.fromJson(json, SocialLinks::class.java)

        // Then
        assertNull(links.youtube)
        assertNull(links.instagram)
        assertNull(links.tiktok)
        assertNull(links.website)
    }

    @Test
    fun `SocialLinks partial fields`() {
        // Given - only youtube set
        val links = SocialLinks(
            youtube = "https://youtube.com",
            instagram = null,
            tiktok = null,
            website = null
        )

        // Then
        assertEquals("https://youtube.com", links.youtube)
        assertNull(links.instagram)
        assertNull(links.tiktok)
        assertNull(links.website)
    }

    // MARK: - UpdateProfileRequest Tests

    @Test
    fun `UpdateProfileRequest with all fields`() {
        // Given
        val request = UpdateProfileRequest(
            displayName = "New Name",
            bio = "Updated bio",
            avatarUrl = "https://example.com/new-avatar.jpg",
            socialLinks = SocialLinks(
                youtube = "https://youtube.com",
                instagram = "@new",
                tiktok = null,
                website = null
            )
        )

        // Then
        assertEquals("New Name", request.displayName)
        assertEquals("Updated bio", request.bio)
        assertEquals("https://example.com/new-avatar.jpg", request.avatarUrl)
        assertEquals("https://youtube.com", request.socialLinks?.youtube)
    }

    @Test
    fun `UpdateProfileRequest partial update`() {
        // Given - only updating display name
        val request = UpdateProfileRequest(
            displayName = "Just New Name",
            bio = null,
            avatarUrl = null,
            socialLinks = null
        )

        // Then
        assertEquals("Just New Name", request.displayName)
        assertNull(request.bio)
        assertNull(request.avatarUrl)
        assertNull(request.socialLinks)
    }

    @Test
    fun `UpdateProfileRequest serializes to JSON`() {
        // Given
        val request = UpdateProfileRequest(
            displayName = "Test",
            bio = "Bio",
            avatarUrl = null,
            socialLinks = null
        )

        // When
        val json = gson.toJson(request)

        // Then
        assertTrue(json.contains("\"displayName\":\"Test\""))
        assertTrue(json.contains("\"bio\":\"Bio\""))
    }

    // MARK: - ReportReason Tests

    @Test
    fun `ReportReason all cases have values`() {
        assertEquals("spam", ReportReason.SPAM.value)
        assertEquals("harassment", ReportReason.HARASSMENT.value)
        assertEquals("inappropriate", ReportReason.INAPPROPRIATE.value)
        assertEquals("other", ReportReason.OTHER.value)
    }

    @Test
    fun `ReportReason valueOf works for all cases`() {
        assertEquals(ReportReason.SPAM, ReportReason.valueOf("SPAM"))
        assertEquals(ReportReason.HARASSMENT, ReportReason.valueOf("HARASSMENT"))
        assertEquals(ReportReason.INAPPROPRIATE, ReportReason.valueOf("INAPPROPRIATE"))
        assertEquals(ReportReason.OTHER, ReportReason.valueOf("OTHER"))
    }

    // MARK: - Data Class Equality Tests

    @Test
    fun `UserSummary equals works correctly`() {
        val user1 = UserSummary("user-1", "user", "User", null)
        val user2 = UserSummary("user-1", "user", "User", null)
        val user3 = UserSummary("user-2", "other", null, null)

        assertEquals(user1, user2)
        assertTrue(user1 != user3)
    }

    @Test
    fun `UserProfile copy works correctly`() {
        // Given
        val original = UserProfile(
            id = "user-1",
            username = "user",
            displayName = "User",
            avatarUrl = null,
            bio = null,
            level = 5,
            recipeCount = 10,
            logCount = 20,
            followerCount = 100,
            followingCount = 50,
            socialLinks = null,
            isFollowing = false,
            isFollowedBy = false,
            isBlocked = false,
            createdAt = LocalDateTime.now()
        )

        // Simulate follow action
        val updated = original.copy(
            isFollowing = true,
            followerCount = original.followerCount + 1
        )

        // Then
        assertFalse(original.isFollowing)
        assertEquals(100, original.followerCount)
        assertTrue(updated.isFollowing)
        assertEquals(101, updated.followerCount)
    }

    @Test
    fun `MyProfile copy works correctly`() {
        // Given
        val original = MyProfile(
            id = "user-1",
            username = "user",
            displayName = "Old Name",
            email = "user@example.com",
            avatarUrl = null,
            bio = "Old bio",
            level = 5,
            xp = 500,
            levelProgress = 0.5,
            recipeCount = 10,
            logCount = 20,
            followerCount = 100,
            followingCount = 50,
            socialLinks = null,
            createdAt = LocalDateTime.now()
        )

        // Update profile
        val updated = original.copy(
            displayName = "New Name",
            bio = "New bio"
        )

        // Then
        assertEquals("Old Name", original.displayName)
        assertEquals("New Name", updated.displayName)
        assertEquals("New bio", updated.bio)
        // Unchanged fields preserved
        assertEquals(original.level, updated.level)
        assertEquals(original.xp, updated.xp)
    }

    // MARK: - Helper

    private class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): LocalDateTime? {
            return json?.asString?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        }
    }
}
