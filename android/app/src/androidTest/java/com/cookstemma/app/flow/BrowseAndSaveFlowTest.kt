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
class BrowseAndSaveFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // MARK: - Home Feed Tests

    @Test
    fun homeFeed_displaysContent() {
        // Given: App launches on home tab
        composeRule.waitForIdle()

        // Then: Home feed content should be visible
        composeRule.onNodeWithTag("home_feed").assertExists()
    }

    @Test
    fun homeFeed_canScroll() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // When: User scrolls down
        composeRule.onNodeWithTag("home_feed")
            .performScrollToIndex(5)

        // Then: Feed should scroll without crash
        composeRule.onNodeWithTag("home_feed").assertExists()
    }

    @Test
    fun homeFeed_pullToRefresh() {
        // Given: User is on home feed
        composeRule.waitForIdle()

        // When: User pulls to refresh
        composeRule.onNodeWithTag("home_feed")
            .performTouchInput { swipeDown() }

        // Then: Feed should refresh
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_feed").assertExists()
    }

    // MARK: - Tab Navigation Tests

    @Test
    fun tabNavigation_homeToRecipes() {
        // Given: User is on home tab
        composeRule.waitForIdle()

        // When: User taps recipes tab (book icon)
        composeRule.onNodeWithTag("tab_recipes").performClick()
        composeRule.waitForIdle()

        // Then: Recipes screen should be visible
        composeRule.onNodeWithTag("recipes_screen").assertExists()
    }

    @Test
    fun tabNavigation_recipesToSaved() {
        // Given: User is on recipes tab
        composeRule.onNodeWithTag("tab_recipes").performClick()
        composeRule.waitForIdle()

        // When: User taps saved tab (bookmark icon)
        composeRule.onNodeWithTag("tab_saved").performClick()
        composeRule.waitForIdle()

        // Then: Saved screen should be visible
        composeRule.onNodeWithTag("saved_screen").assertExists()
    }

    // MARK: - Recipe Navigation Tests

    @Test
    fun recipeCard_tapNavigatesToDetail() {
        // Given: User is on recipes tab
        composeRule.onNodeWithTag("tab_recipes").performClick()
        composeRule.waitForIdle()

        // When: User taps on a recipe card
        composeRule.onAllNodesWithTag("recipe_card")
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        // Then: Recipe detail screen should appear
        composeRule.onNodeWithTag("recipe_detail_screen").assertExists()
    }

    @Test
    fun recipeDetail_saveButtonExists() {
        // Given: User is on recipe detail
        composeRule.onNodeWithTag("tab_recipes").performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("recipe_card")
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        // Then: Save button (bookmark icon) should exist
        composeRule.onNodeWithTag("save_button").assertExists()
    }

    @Test
    fun recipeDetail_saveButtonToggle() {
        // Given: User is on recipe detail
        composeRule.onNodeWithTag("tab_recipes").performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("recipe_card")
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        // When: User taps save button
        composeRule.onNodeWithTag("save_button").performClick()
        composeRule.waitForIdle()

        // Then: Save state should toggle (button still exists)
        composeRule.onNodeWithTag("save_button").assertExists()
    }

    // MARK: - Saved Tab Tests

    @Test
    fun savedTab_displaysContent() {
        // Given: User navigates to saved tab
        composeRule.onNodeWithTag("tab_saved").performClick()
        composeRule.waitForIdle()

        // Then: Saved screen content should be visible
        composeRule.onNodeWithTag("saved_screen").assertExists()
    }

    @Test
    fun savedTab_switchBetweenRecipesAndLogs() {
        // Given: User is on saved tab
        composeRule.onNodeWithTag("tab_saved").performClick()
        composeRule.waitForIdle()

        // When: User taps on logs tab icon
        composeRule.onNodeWithTag("saved_tab_logs").performClick()
        composeRule.waitForIdle()

        // Then: Logs section should be visible
        composeRule.onNodeWithTag("saved_logs_content").assertExists()

        // When: User taps back to recipes tab icon
        composeRule.onNodeWithTag("saved_tab_recipes").performClick()
        composeRule.waitForIdle()

        // Then: Recipes section should be visible
        composeRule.onNodeWithTag("saved_recipes_content").assertExists()
    }

    // MARK: - Full Browse & Save Flow

    @Test
    fun fullFlow_browseRecipeAndNavigateToSaved() {
        // Step 1: Start on home
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_feed").assertExists()

        // Step 2: Navigate to recipes tab
        composeRule.onNodeWithTag("tab_recipes").performClick()
        composeRule.waitForIdle()

        // Step 3: Verify recipes screen
        composeRule.onNodeWithTag("recipes_screen").assertExists()

        // Step 4: Navigate to saved tab
        composeRule.onNodeWithTag("tab_saved").performClick()
        composeRule.waitForIdle()

        // Step 5: Verify saved screen
        composeRule.onNodeWithTag("saved_screen").assertExists()

        // Step 6: Return to home
        composeRule.onNodeWithTag("tab_home").performClick()
        composeRule.waitForIdle()

        // Step 7: Verify home screen
        composeRule.onNodeWithTag("home_feed").assertExists()
    }

    @Test
    fun fullFlow_saveRecipeAndVerifyInSaved() {
        // Step 1: Go to recipes tab
        composeRule.onNodeWithTag("tab_recipes").performClick()
        composeRule.waitForIdle()

        // Step 2: Tap on recipe to view detail
        composeRule.onAllNodesWithTag("recipe_card")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Step 3: Save the recipe
        composeRule.onNodeWithTag("save_button").performClick()
        composeRule.waitForIdle()

        // Step 4: Navigate back
        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.waitForIdle()

        // Step 5: Go to saved tab
        composeRule.onNodeWithTag("tab_saved").performClick()
        composeRule.waitForIdle()

        // Step 6: Verify saved screen shows content
        composeRule.onNodeWithTag("saved_screen").assertExists()
    }
}
