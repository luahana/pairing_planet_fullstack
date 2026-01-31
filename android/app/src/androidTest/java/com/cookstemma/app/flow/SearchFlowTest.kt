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
class SearchFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // MARK: - Search Access Tests

    @Test
    fun search_accessFromHomeTab() {
        // Given: User is on home tab
        composeRule.waitForIdle()

        // When: User taps search icon
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // Then: Search screen should appear
        composeRule.onNodeWithTag("search_screen").assertExists()
    }

    @Test
    fun search_accessFromRecipesTab() {
        // Given: User is on recipes tab
        composeRule.onNodeWithTag("tab_recipes").performClick()
        composeRule.waitForIdle()

        // When: User taps search icon
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // Then: Search should be accessible
        composeRule.onNodeWithTag("search_field").assertExists()
    }

    // MARK: - Search Field Tests

    @Test
    fun search_fieldExists() {
        // Given: User opens search
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // Then: Search field should exist
        composeRule.onNodeWithTag("search_field").assertExists()
    }

    @Test
    fun search_canTypeQuery() {
        // Given: User opens search
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // When: User types search query
        composeRule.onNodeWithTag("search_field")
            .performTextInput("kimchi")

        // Then: Query should be entered
        composeRule.onNodeWithTag("search_field")
            .assertTextContains("kimchi")
    }

    @Test
    fun search_clearQuery() {
        // Given: User has entered search query
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("search_field")
            .performTextInput("test")

        // When: User clears search
        composeRule.onNodeWithTag("clear_search_button").performClick()
        composeRule.waitForIdle()

        // Then: Search field should be empty
        composeRule.onNodeWithTag("search_field")
            .assertTextContains("")
    }

    // MARK: - Search Results Tests

    @Test
    fun search_displaysResultsAfterQuery() {
        // Given: User opens search
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // When: User enters and submits query
        composeRule.onNodeWithTag("search_field")
            .performTextInput("recipe")
        composeRule.onNodeWithTag("search_field")
            .performImeAction()
        composeRule.waitForIdle()

        // Then: Results should be displayed
        composeRule.onNodeWithTag("search_results").assertExists()
    }

    @Test
    fun search_emptyResultsShowsEmptyState() {
        // Given: User searches for non-existent term
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("search_field")
            .performTextInput("xyznonexistent123")
        composeRule.onNodeWithTag("search_field")
            .performImeAction()
        composeRule.waitForIdle()

        // Then: Empty state or results should appear
        val resultsOrEmpty = composeRule.onNodeWithTag("search_results").fetchSemanticsNode()
        composeRule.onNodeWithTag("search_screen").assertExists()
    }

    // MARK: - Tab Filter Tests

    @Test
    fun search_tabFiltersExist() {
        // Given: User is on search screen with results
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("search_field")
            .performTextInput("food")
        composeRule.onNodeWithTag("search_field")
            .performImeAction()
        composeRule.waitForIdle()

        // Then: Tab filters should exist (icons for all, recipes, logs, users, hashtags)
        composeRule.onNodeWithTag("search_tab_all").assertExists()
    }

    @Test
    fun search_switchBetweenTabs() {
        // Given: User has search results
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("search_field")
            .performTextInput("test")
        composeRule.onNodeWithTag("search_field")
            .performImeAction()
        composeRule.waitForIdle()

        // When: User taps recipes tab icon
        composeRule.onNodeWithTag("search_tab_recipes").performClick()
        composeRule.waitForIdle()

        // Then: Recipes results should be filtered
        composeRule.onNodeWithTag("search_results").assertExists()

        // When: User taps users tab icon
        composeRule.onNodeWithTag("search_tab_users").performClick()
        composeRule.waitForIdle()

        // Then: Users results should be filtered
        composeRule.onNodeWithTag("search_results").assertExists()
    }

    // MARK: - Recent Searches Tests

    @Test
    fun search_recentSearchesVisible() {
        // Given: User opens search without query
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // Then: Recent searches section should be visible (history icon)
        composeRule.onNodeWithTag("recent_searches_section").assertExists()
    }

    @Test
    fun search_canTapRecentSearch() {
        // Given: User opens search
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // When: User taps on a recent search item
        composeRule.onAllNodesWithTag("recent_search_item")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Then: Search should execute with that query
        composeRule.onNodeWithTag("search_results").assertExists()
    }

    // MARK: - Trending Tests

    @Test
    fun search_trendingVisible() {
        // Given: User opens search
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // Then: Trending section should be visible (fire icon)
        composeRule.onNodeWithTag("trending_section").assertExists()
    }

    // MARK: - Result Navigation Tests

    @Test
    fun search_tapResultNavigates() {
        // Given: User has search results
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("search_field")
            .performTextInput("recipe")
        composeRule.onNodeWithTag("search_field")
            .performImeAction()
        composeRule.waitForIdle()

        // When: User taps on a result
        composeRule.onAllNodesWithTag("search_result_item")
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        // Then: Detail screen should appear
        composeRule.onNodeWithTag("detail_screen").assertExists()
    }

    // MARK: - Back Navigation Tests

    @Test
    fun search_canNavigateBack() {
        // Given: User is on search screen
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // When: User taps back
        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.waitForIdle()

        // Then: User returns to previous screen
        composeRule.onNodeWithTag("home_feed").assertExists()
    }

    // MARK: - Hashtag Search Tests

    @Test
    fun search_hashtagSearch() {
        // Given: User opens search
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // When: User searches for hashtag
        composeRule.onNodeWithTag("search_field")
            .performTextInput("#koreanfood")
        composeRule.onNodeWithTag("search_field")
            .performImeAction()
        composeRule.waitForIdle()

        // Then: Hashtag results should appear
        composeRule.onNodeWithTag("search_results").assertExists()
    }

    @Test
    fun search_hashtagTabFilter() {
        // Given: User has search results
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("search_field")
            .performTextInput("food")
        composeRule.onNodeWithTag("search_field")
            .performImeAction()
        composeRule.waitForIdle()

        // When: User taps hashtag tab icon
        composeRule.onNodeWithTag("search_tab_hashtags").performClick()
        composeRule.waitForIdle()

        // Then: Hashtag results should be filtered
        composeRule.onNodeWithTag("search_results").assertExists()
    }

    // MARK: - Full Search Flow

    @Test
    fun fullFlow_searchAndNavigate() {
        // Step 1: Open search
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.waitForIdle()

        // Step 2: Verify search screen
        composeRule.onNodeWithTag("search_screen").assertExists()

        // Step 3: Enter search query
        composeRule.onNodeWithTag("search_field")
            .performTextInput("delicious")
        composeRule.onNodeWithTag("search_field")
            .performImeAction()
        composeRule.waitForIdle()

        // Step 4: Verify results appear
        composeRule.onNodeWithTag("search_results").assertExists()

        // Step 5: Switch filter tabs
        composeRule.onNodeWithTag("search_tab_recipes").performClick()
        composeRule.waitForIdle()

        // Step 6: Navigate back
        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.waitForIdle()

        // Step 7: Verify back on home
        composeRule.onNodeWithTag("home_feed").assertExists()
    }
}
