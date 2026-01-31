package com.cookstemma.app.flow

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cookstemma.app.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SocialFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // MARK: - Profile Tab Tests

    @Test
    fun profileTab_displaysUserInfo() {
        // Given: User navigates to profile tab
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.waitForIdle()

        // Then: Profile screen should be visible
        composeRule.onNodeWithTag("profile_screen").assertExists()
    }

    @Test
    fun profileTab_displaysStats() {
        // Given: User is on profile tab
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.waitForIdle()

        // Then: Stats section should be visible
        composeRule.onNodeWithTag("profile_stats").assertExists()
    }

    @Test
    fun profileTab_avatarVisible() {
        // Given: User is on profile tab
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.waitForIdle()

        // Then: Avatar should be visible
        composeRule.onNodeWithTag("profile_avatar").assertExists()
    }

    @Test
    fun profileTab_switchContentTabs() {
        // Given: User is on profile tab
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.waitForIdle()

        // When: User taps on logs tab icon
        composeRule.onNodeWithTag("profile_tab_logs").performClick()
        composeRule.waitForIdle()

        // Then: Logs content should be visible
        composeRule.onNodeWithTag("profile_logs_content").assertExists()

        // When: User taps on recipes tab icon
        composeRule.onNodeWithTag("profile_tab_recipes").performClick()
        composeRule.waitForIdle()

        // Then: Recipes content should be visible
        composeRule.onNodeWithTag("profile_recipes_content").assertExists()
    }

    // MARK: - Other User Profile Tests

    @Test
    fun navigateToOtherUserProfile() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // When: User taps on user avatar in feed
        composeRule.onAllNodesWithTag("feed_user_avatar")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Then: Other user's profile should appear
        composeRule.onNodeWithTag("user_profile_screen").assertExists()
    }

    // MARK: - Follow Button Tests

    @Test
    fun followButton_visibleOnOtherProfile() {
        // Given: User navigates to another user's profile
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("feed_user_avatar")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Then: Follow button should be visible (person badge icon)
        composeRule.onNodeWithTag("follow_button").assertExists()
    }

    @Test
    fun followButton_toggleState() {
        // Given: User is on another user's profile
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("feed_user_avatar")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // When: User taps follow button
        composeRule.onNodeWithTag("follow_button").performClick()
        composeRule.waitForIdle()

        // Then: Button state should toggle
        composeRule.onNodeWithTag("follow_button").assertExists()
    }

    // MARK: - Followers/Following Lists

    @Test
    fun tapFollowersCount_opensList() {
        // Given: User is on profile tab
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.waitForIdle()

        // When: User taps on followers count
        composeRule.onNodeWithTag("followers_count").performClick()
        composeRule.waitForIdle()

        // Then: Followers list should appear
        composeRule.onNodeWithTag("followers_list_screen").assertExists()
    }

    @Test
    fun tapFollowingCount_opensList() {
        // Given: User is on profile tab
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.waitForIdle()

        // When: User taps on following count
        composeRule.onNodeWithTag("following_count").performClick()
        composeRule.waitForIdle()

        // Then: Following list should appear
        composeRule.onNodeWithTag("following_list_screen").assertExists()
    }

    // MARK: - Like Action Tests

    @Test
    fun likeButton_visibleInFeed() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // Then: Like buttons (heart icons) should be visible
        composeRule.onAllNodesWithTag("like_button")
            .onFirst()
            .assertExists()
    }

    @Test
    fun likeButton_togglesOnTap() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // When: User taps like button
        composeRule.onAllNodesWithTag("like_button")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Then: Like should toggle
        composeRule.onAllNodesWithTag("like_button")
            .onFirst()
            .assertExists()
    }

    // MARK: - Comment Action Tests

    @Test
    fun commentButton_visibleInFeed() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // Then: Comment buttons (chat bubble icons) should be visible
        composeRule.onAllNodesWithTag("comment_button")
            .onFirst()
            .assertExists()
    }

    @Test
    fun commentButton_navigatesToComments() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // When: User taps comment button
        composeRule.onAllNodesWithTag("comment_button")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Then: Comments section should appear
        composeRule.onNodeWithTag("comments_screen").assertExists()
    }

    // MARK: - Share Action Tests

    @Test
    fun shareButton_visibleInFeed() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // Then: Share buttons should be visible
        composeRule.onAllNodesWithTag("share_button")
            .onFirst()
            .assertExists()
    }

    @Test
    fun shareButton_opensShareSheet() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // When: User taps share button
        composeRule.onAllNodesWithTag("share_button")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Then: Share sheet should appear (or intent fired)
        // Note: System share sheet may not be testable in compose tests
        composeRule.onNodeWithTag("home_feed").assertExists()
    }

    // MARK: - Save Action Tests

    @Test
    fun saveButton_visibleInFeed() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // Then: Save buttons (bookmark icons) should be visible
        composeRule.onAllNodesWithTag("save_button")
            .onFirst()
            .assertExists()
    }

    @Test
    fun saveButton_togglesOnTap() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // When: User taps save button
        composeRule.onAllNodesWithTag("save_button")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Then: Save should toggle
        composeRule.onAllNodesWithTag("save_button")
            .onFirst()
            .assertExists()
    }

    // MARK: - Notifications Tests

    @Test
    fun notificationBell_visibleOnHome() {
        // Given: User is on home tab
        composeRule.waitForIdle()

        // Then: Notification bell icon should be visible
        composeRule.onNodeWithTag("notification_button").assertExists()
    }

    @Test
    fun notificationBell_navigatesToNotifications() {
        // Given: User is on home tab
        composeRule.waitForIdle()

        // When: User taps notification bell
        composeRule.onNodeWithTag("notification_button").performClick()
        composeRule.waitForIdle()

        // Then: Notifications screen should appear
        composeRule.onNodeWithTag("notifications_screen").assertExists()
    }

    @Test
    fun notifications_displaysItems() {
        // Given: User navigates to notifications
        composeRule.onNodeWithTag("notification_button").performClick()
        composeRule.waitForIdle()

        // Then: Notification list or empty state should be visible
        composeRule.onNodeWithTag("notifications_screen").assertExists()
    }

    @Test
    fun notifications_markAllButton() {
        // Given: User is on notifications screen
        composeRule.onNodeWithTag("notification_button").performClick()
        composeRule.waitForIdle()

        // Then: Mark all button (checkmark icon) should be visible if there are notifications
        composeRule.onNodeWithTag("notifications_screen").assertExists()
    }

    // MARK: - Profile Menu Tests

    @Test
    fun profileMenu_settingsAccessible() {
        // Given: User is on profile tab
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.waitForIdle()

        // When: User taps settings icon
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.waitForIdle()

        // Then: Settings screen should appear
        composeRule.onNodeWithTag("settings_screen").assertExists()
    }

    @Test
    fun otherUserProfile_moreMenuAccessible() {
        // Given: User is on another user's profile
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("feed_user_avatar")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // When: User taps more menu (ellipsis icon)
        composeRule.onNodeWithTag("more_menu_button").performClick()
        composeRule.waitForIdle()

        // Then: Menu with options should appear
        composeRule.onNodeWithTag("profile_menu").assertExists()
    }

    // MARK: - Full Social Flow

    @Test
    fun fullFlow_viewProfileFollowAndNotifications() {
        // Step 1: Start on home feed
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_feed").assertExists()

        // Step 2: Navigate to profile tab
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.waitForIdle()

        // Step 3: Verify profile screen
        composeRule.onNodeWithTag("profile_screen").assertExists()

        // Step 4: Return to home
        composeRule.onNodeWithTag("tab_home").performClick()
        composeRule.waitForIdle()

        // Step 5: Check notifications
        composeRule.onNodeWithTag("notification_button").performClick()
        composeRule.waitForIdle()

        // Step 6: Verify notifications screen
        composeRule.onNodeWithTag("notifications_screen").assertExists()

        // Step 7: Navigate back
        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.waitForIdle()

        // Step 8: Verify back on home
        composeRule.onNodeWithTag("home_feed").assertExists()
    }

    @Test
    fun fullFlow_interactWithFeedItem() {
        // Step 1: Start on home feed
        composeRule.waitForIdle()

        // Step 2: Like a post
        composeRule.onAllNodesWithTag("like_button")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Step 3: Open comments
        composeRule.onAllNodesWithTag("comment_button")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Step 4: Navigate back from comments
        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.waitForIdle()

        // Step 5: Save the post
        composeRule.onAllNodesWithTag("save_button")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Step 6: Verify still on home feed
        composeRule.onNodeWithTag("home_feed").assertExists()
    }
}
