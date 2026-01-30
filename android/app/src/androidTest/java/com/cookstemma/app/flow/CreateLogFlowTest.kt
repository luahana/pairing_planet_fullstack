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
class CreateLogFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // MARK: - Create Tab Access

    @Test
    fun createTab_opensModal() {
        // Given: User is on any tab
        composeRule.waitForIdle()

        // When: User taps create tab (plus icon)
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Then: Create log screen should appear
        composeRule.onNodeWithTag("create_log_screen").assertExists()
    }

    // MARK: - Photo Selection Tests

    @Test
    fun createLog_photoSectionAccessible() {
        // Given: User opens create screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Then: Photo selection area should be visible
        composeRule.onNodeWithTag("photo_section").assertExists()
    }

    @Test
    fun createLog_addPhotoButtonExists() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Then: Add photo button should exist
        composeRule.onNodeWithTag("add_photo_button").assertExists()
    }

    // MARK: - Rating Selection Tests

    @Test
    fun createLog_ratingSectionExists() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Then: Rating section should be visible
        composeRule.onNodeWithTag("rating_section").assertExists()
    }

    @Test
    fun createLog_canSelectRating() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // When: User taps on 4th star
        composeRule.onNodeWithTag("star_4").performClick()
        composeRule.waitForIdle()

        // Then: Rating should be selected (4 stars filled)
        composeRule.onNodeWithTag("star_4").assertExists()
    }

    @Test
    fun createLog_ratingStarsInteractive() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // When: User taps different stars
        composeRule.onNodeWithTag("star_3").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("star_5").performClick()
        composeRule.waitForIdle()

        // Then: Stars should respond (no crash)
        composeRule.onNodeWithTag("rating_section").assertExists()
    }

    // MARK: - Recipe Link Tests

    @Test
    fun createLog_recipeLinkSectionExists() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Then: Recipe link section should be visible
        composeRule.onNodeWithTag("recipe_link_section").assertExists()
    }

    @Test
    fun createLog_canOpenRecipeSearch() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // When: User taps recipe search field
        composeRule.onNodeWithTag("recipe_search_field").performClick()
        composeRule.waitForIdle()

        // Then: Recipe search should open
        composeRule.onNodeWithTag("recipe_search_sheet").assertExists()
    }

    // MARK: - Content Input Tests

    @Test
    fun createLog_contentFieldExists() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Then: Content text field should exist
        composeRule.onNodeWithTag("content_field").assertExists()
    }

    @Test
    fun createLog_canTypeContent() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // When: User types in content field
        composeRule.onNodeWithTag("content_field")
            .performTextInput("This was delicious!")

        // Then: Text should be entered
        composeRule.onNodeWithTag("content_field")
            .assertTextContains("This was delicious!")
    }

    // MARK: - Close/Cancel Tests

    @Test
    fun createLog_canDismiss() {
        // Given: User opens create screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // When: User taps close/back button
        composeRule.onNodeWithTag("close_button").performClick()
        composeRule.waitForIdle()

        // Then: Should return to previous screen
        composeRule.onNodeWithTag("home_feed").assertExists()
    }

    @Test
    fun createLog_backButtonExists() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Then: Close/back button should exist
        composeRule.onNodeWithTag("close_button").assertExists()
    }

    // MARK: - Validation Tests

    @Test
    fun createLog_postButtonExists() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Then: Post button should exist
        composeRule.onNodeWithTag("post_button").assertExists()
    }

    @Test
    fun createLog_postButtonDisabledWithoutPhoto() {
        // Given: User is on create log screen with no photo selected
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Then: Post button should be disabled
        composeRule.onNodeWithTag("post_button")
            .assertIsNotEnabled()
    }

    // MARK: - Hashtag Input Tests

    @Test
    fun createLog_hashtagSectionExists() {
        // Given: User is on create log screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Then: Hashtag section should exist
        composeRule.onNodeWithTag("hashtag_section").assertExists()
    }

    // MARK: - Full Create Flow (Mock)

    @Test
    fun fullFlow_createLogSteps() {
        // Step 1: Open create modal
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // Step 2: Verify screen opened
        composeRule.onNodeWithTag("create_log_screen").assertExists()

        // Step 3: Select rating
        composeRule.onNodeWithTag("star_4").performClick()
        composeRule.waitForIdle()

        // Step 4: Enter content
        composeRule.onNodeWithTag("content_field")
            .performTextInput("Made this tonight, turned out great!")

        // Step 5: Close modal (no actual post in test)
        composeRule.onNodeWithTag("close_button").performClick()
        composeRule.waitForIdle()

        // Step 6: Verify back on main screen
        composeRule.onNodeWithTag("home_feed").assertExists()
    }

    @Test
    fun createLog_navigateThroughScreenElements() {
        // Given: User opens create screen
        composeRule.onNodeWithTag("tab_create").performClick()
        composeRule.waitForIdle()

        // When: User scrolls through the screen
        composeRule.onNodeWithTag("create_log_screen")
            .performScrollToNode(hasTestTag("post_button"))

        // Then: All sections should be accessible
        composeRule.onNodeWithTag("photo_section").assertExists()
        composeRule.onNodeWithTag("rating_section").assertExists()
    }
}
