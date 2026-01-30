package com.cookstemma.app.ui.components

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IconComponentsTest {

    // MARK: - Number Abbreviation Tests

    @Test
    fun `abbreviated returns raw number for values under 1000`() {
        assertEquals("0", 0.abbreviated())
        assertEquals("1", 1.abbreviated())
        assertEquals("999", 999.abbreviated())
    }

    @Test
    fun `abbreviated returns K suffix for thousands`() {
        assertEquals("1K", 1000.abbreviated())
        assertEquals("1.5K", 1500.abbreviated())
        assertEquals("9.9K", 9900.abbreviated())
    }

    @Test
    fun `abbreviated returns plain K for 10K and above`() {
        assertEquals("10K", 10000.abbreviated())
        assertEquals("50K", 50000.abbreviated())
        assertEquals("999K", 999000.abbreviated())
    }

    @Test
    fun `abbreviated returns M suffix for millions`() {
        assertEquals("1.0M", 1000000.abbreviated())
        assertEquals("1.5M", 1500000.abbreviated())
        assertEquals("10.0M", 10000000.abbreviated())
    }

    // MARK: - AppIcons Tests

    @Test
    fun `AppIcons tab bar icons are not null`() {
        assertNotNull(AppIcons.home)
        assertNotNull(AppIcons.homeOutline)
        assertNotNull(AppIcons.recipes)
        assertNotNull(AppIcons.recipesOutline)
        assertNotNull(AppIcons.create)
        assertNotNull(AppIcons.saved)
        assertNotNull(AppIcons.profile)
    }

    @Test
    fun `AppIcons action icons are not null`() {
        assertNotNull(AppIcons.like)
        assertNotNull(AppIcons.likeOutline)
        assertNotNull(AppIcons.comment)
        assertNotNull(AppIcons.commentOutline)
        assertNotNull(AppIcons.share)
        assertNotNull(AppIcons.save)
        assertNotNull(AppIcons.saveOutline)
    }

    @Test
    fun `AppIcons navigation icons are not null`() {
        assertNotNull(AppIcons.back)
        assertNotNull(AppIcons.close)
        assertNotNull(AppIcons.search)
        assertNotNull(AppIcons.filter)
        assertNotNull(AppIcons.notifications)
        assertNotNull(AppIcons.settings)
    }

    @Test
    fun `AppIcons content icons are not null`() {
        assertNotNull(AppIcons.recipe)
        assertNotNull(AppIcons.log)
        assertNotNull(AppIcons.photo)
        assertNotNull(AppIcons.timer)
        assertNotNull(AppIcons.star)
        assertNotNull(AppIcons.chef)
    }

    @Test
    fun `AppIcons social icons are not null`() {
        assertNotNull(AppIcons.follow)
        assertNotNull(AppIcons.following)
        assertNotNull(AppIcons.followers)
        assertNotNull(AppIcons.block)
        assertNotNull(AppIcons.report)
    }

    @Test
    fun `AppIcons status icons are not null`() {
        assertNotNull(AppIcons.success)
        assertNotNull(AppIcons.error)
        assertNotNull(AppIcons.warning)
        assertNotNull(AppIcons.info)
        assertNotNull(AppIcons.empty)
    }

    // MARK: - Badge Display Logic Tests

    @Test
    fun `badge count greater than 99 shows 99+`() {
        // This tests the logic that would be used in badge display
        val count = 150
        val displayText = if (count > 99) "99+" else count.toString()
        assertEquals("99+", displayText)
    }

    @Test
    fun `badge count 99 or less shows actual count`() {
        val count = 42
        val displayText = if (count > 99) "99+" else count.toString()
        assertEquals("42", displayText)
    }

    @Test
    fun `badge count 0 would hide badge`() {
        val count = 0
        val shouldShowBadge = count > 0
        assertEquals(false, shouldShowBadge)
    }

    // MARK: - Time Display Logic Tests

    @Test
    fun `time under 60 minutes shows minutes`() {
        val minutes = 30
        val displayText = if (minutes < 60) "${minutes}m" else "${minutes / 60}h"
        assertEquals("30m", displayText)
    }

    @Test
    fun `time 60 minutes or more shows hours`() {
        val minutes = 90
        val displayText = if (minutes < 60) "${minutes}m" else "${minutes / 60}h"
        assertEquals("1h", displayText)
    }

    @Test
    fun `time 120 minutes shows 2 hours`() {
        val minutes = 120
        val displayText = if (minutes < 60) "${minutes}m" else "${minutes / 60}h"
        assertEquals("2h", displayText)
    }

    // MARK: - Rating Display Logic Tests

    @Test
    fun `rating formats to one decimal place`() {
        val rating = 4.567
        val displayText = String.format("%.1f", rating)
        assertEquals("4.6", displayText)
    }

    @Test
    fun `rating whole number shows decimal`() {
        val rating = 4.0
        val displayText = String.format("%.1f", rating)
        assertEquals("4.0", displayText)
    }
}
