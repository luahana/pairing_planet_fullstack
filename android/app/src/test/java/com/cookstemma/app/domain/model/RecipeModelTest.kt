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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecipeModelTest {

    private lateinit var gson: Gson

    @Before
    fun setup() {
        gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            .create()
    }

    // MARK: - RecipeSummary Tests

    @Test
    fun `RecipeSummary parses from JSON`() {
        // Given
        val json = """
        {
            "id": "recipe-123",
            "title": "Kimchi Fried Rice",
            "description": "A delicious Korean dish",
            "coverImageUrl": "https://example.com/image.jpg",
            "cookingTimeRange": "BETWEEN_15_AND_30_MIN",
            "servings": 2,
            "cookCount": 156,
            "averageRating": 4.3,
            "author": {
                "id": "user-1",
                "username": "chefkim",
                "displayName": "Chef Kim",
                "avatarUrl": "https://example.com/avatar.jpg"
            },
            "isSaved": false,
            "category": "KOREAN",
            "createdAt": "2024-01-15T10:30:00"
        }
        """.trimIndent()

        // When
        val recipe = gson.fromJson(json, RecipeSummary::class.java)

        // Then
        assertEquals("recipe-123", recipe.id)
        assertEquals("Kimchi Fried Rice", recipe.title)
        assertEquals("A delicious Korean dish", recipe.description)
        assertEquals("https://example.com/image.jpg", recipe.coverImageUrl)
        assertEquals(CookingTimeRange.BETWEEN_15_AND_30_MIN, recipe.cookingTimeRange)
        assertEquals(2, recipe.servings)
        assertEquals(156, recipe.cookCount)
        assertEquals(4.3, recipe.averageRating)
        assertEquals("chefkim", recipe.author.username)
        assertEquals(false, recipe.isSaved)
    }

    @Test
    fun `RecipeSummary handles null optional fields`() {
        // Given
        val json = """
        {
            "id": "recipe-123",
            "title": "Simple Recipe",
            "description": null,
            "coverImageUrl": null,
            "cookingTimeRange": null,
            "servings": null,
            "cookCount": 0,
            "averageRating": null,
            "author": {
                "id": "user-1",
                "username": "user",
                "displayName": null,
                "avatarUrl": null
            },
            "isSaved": false,
            "category": null,
            "createdAt": "2024-01-15T10:30:00"
        }
        """.trimIndent()

        // When
        val recipe = gson.fromJson(json, RecipeSummary::class.java)

        // Then
        assertEquals("recipe-123", recipe.id)
        assertNull(recipe.description)
        assertNull(recipe.coverImageUrl)
        assertNull(recipe.cookingTimeRange)
        assertNull(recipe.servings)
        assertNull(recipe.averageRating)
        assertNull(recipe.category)
    }

    // MARK: - RecipeDetail Tests

    @Test
    fun `RecipeDetail parses from JSON with ingredients and steps`() {
        // Given
        val json = """
        {
            "id": "recipe-123",
            "title": "Kimchi Fried Rice",
            "description": "A delicious Korean dish",
            "coverImageUrl": "https://example.com/cover.jpg",
            "images": ["https://example.com/img1.jpg", "https://example.com/img2.jpg"],
            "cookingTimeRange": "BETWEEN_30_AND_60_MIN",
            "servings": 4,
            "cookCount": 156,
            "saveCount": 89,
            "averageRating": 4.5,
            "author": {
                "id": "user-1",
                "username": "chefkim",
                "displayName": "Chef Kim",
                "avatarUrl": null
            },
            "ingredients": [
                {"name": "Cooked rice", "amount": "2 cups", "category": "MAIN"},
                {"name": "Kimchi", "amount": "1 cup", "category": "MAIN"},
                {"name": "Green onions", "amount": "2 stalks", "category": "SECONDARY"},
                {"name": "Sesame oil", "amount": "1 tbsp", "category": "SEASONING"}
            ],
            "steps": [
                {"order": 1, "instruction": "Heat oil in a large pan", "imageUrl": null, "tipContent": null},
                {"order": 2, "instruction": "Add kimchi and stir fry", "imageUrl": "https://example.com/step2.jpg", "tipContent": "Use well-fermented kimchi"}
            ],
            "hashtags": ["korean", "friedrice", "quickmeal"],
            "isSaved": true,
            "category": "KOREAN",
            "createdAt": "2024-01-15T10:30:00",
            "updatedAt": "2024-01-20T14:00:00"
        }
        """.trimIndent()

        // When
        val recipe = gson.fromJson(json, RecipeDetail::class.java)

        // Then
        assertEquals("recipe-123", recipe.id)
        assertEquals("Kimchi Fried Rice", recipe.title)
        assertEquals(2, recipe.images.size)
        assertEquals(CookingTimeRange.BETWEEN_30_AND_60_MIN, recipe.cookingTimeRange)
        assertEquals(4, recipe.servings)
        assertEquals(4, recipe.ingredients.size)
        assertEquals(2, recipe.steps.size)
        assertEquals(listOf("korean", "friedrice", "quickmeal"), recipe.hashtags)
        assertTrue(recipe.isSaved)
    }

    @Test
    fun `RecipeDetail ingredient categories`() {
        // Given
        val mainIngredient = Ingredient("Rice", "2 cups", IngredientCategory.MAIN)
        val secondaryIngredient = Ingredient("Onion", "1 piece", IngredientCategory.SECONDARY)
        val seasoning = Ingredient("Salt", "1 tsp", IngredientCategory.SEASONING)

        // Then
        assertEquals(IngredientCategory.MAIN, mainIngredient.category)
        assertEquals(IngredientCategory.SECONDARY, secondaryIngredient.category)
        assertEquals(IngredientCategory.SEASONING, seasoning.category)
    }

    // MARK: - CookingTimeRange Tests

    @Test
    fun `CookingTimeRange all cases have display names`() {
        assertEquals("Under 15 min", CookingTimeRange.UNDER_15_MIN.displayName)
        assertEquals("15-30 min", CookingTimeRange.BETWEEN_15_AND_30_MIN.displayName)
        assertEquals("30-60 min", CookingTimeRange.BETWEEN_30_AND_60_MIN.displayName)
        assertEquals("Over 1 hour", CookingTimeRange.OVER_60_MIN.displayName)
    }

    @Test
    fun `CookingTimeRange valueOf works for all cases`() {
        assertEquals(CookingTimeRange.UNDER_15_MIN, CookingTimeRange.valueOf("UNDER_15_MIN"))
        assertEquals(CookingTimeRange.BETWEEN_15_AND_30_MIN, CookingTimeRange.valueOf("BETWEEN_15_AND_30_MIN"))
        assertEquals(CookingTimeRange.BETWEEN_30_AND_60_MIN, CookingTimeRange.valueOf("BETWEEN_30_AND_60_MIN"))
        assertEquals(CookingTimeRange.OVER_60_MIN, CookingTimeRange.valueOf("OVER_60_MIN"))
    }

    // MARK: - Ingredient Tests

    @Test
    fun `Ingredient data class works correctly`() {
        // Given
        val ingredient = Ingredient(
            name = "Soy sauce",
            amount = "2 tbsp",
            category = IngredientCategory.SEASONING
        )

        // Then
        assertEquals("Soy sauce", ingredient.name)
        assertEquals("2 tbsp", ingredient.amount)
        assertEquals(IngredientCategory.SEASONING, ingredient.category)
    }

    @Test
    fun `IngredientCategory all values exist`() {
        val categories = IngredientCategory.entries
        assertEquals(3, categories.size)
        assertTrue(categories.contains(IngredientCategory.MAIN))
        assertTrue(categories.contains(IngredientCategory.SECONDARY))
        assertTrue(categories.contains(IngredientCategory.SEASONING))
    }

    // MARK: - RecipeStep Tests

    @Test
    fun `RecipeStep parses correctly`() {
        // Given
        val step = RecipeStep(
            order = 1,
            instruction = "Heat oil in a pan",
            imageUrl = "https://example.com/step1.jpg",
            tipContent = "Use high heat"
        )

        // Then
        assertEquals(1, step.order)
        assertEquals("Heat oil in a pan", step.instruction)
        assertEquals("https://example.com/step1.jpg", step.imageUrl)
        assertEquals("Use high heat", step.tipContent)
    }

    @Test
    fun `RecipeStep handles null optional fields`() {
        // Given
        val step = RecipeStep(
            order = 2,
            instruction = "Add ingredients",
            imageUrl = null,
            tipContent = null
        )

        // Then
        assertNull(step.imageUrl)
        assertNull(step.tipContent)
    }

    // MARK: - FoodCategory Tests

    @Test
    fun `FoodCategory all cases have display names`() {
        assertEquals("Korean", FoodCategory.KOREAN.displayName)
        assertEquals("Japanese", FoodCategory.JAPANESE.displayName)
        assertEquals("Chinese", FoodCategory.CHINESE.displayName)
        assertEquals("Italian", FoodCategory.ITALIAN.displayName)
        assertEquals("American", FoodCategory.AMERICAN.displayName)
        assertEquals("Mexican", FoodCategory.MEXICAN.displayName)
        assertEquals("Indian", FoodCategory.INDIAN.displayName)
        assertEquals("Thai", FoodCategory.THAI.displayName)
        assertEquals("Vietnamese", FoodCategory.VIETNAMESE.displayName)
        assertEquals("French", FoodCategory.FRENCH.displayName)
        assertEquals("Other", FoodCategory.OTHER.displayName)
    }

    // MARK: - RecipeSortBy Tests

    @Test
    fun `RecipeSortBy all cases have values`() {
        assertEquals("trending", RecipeSortBy.TRENDING.value)
        assertEquals("most_cooked", RecipeSortBy.MOST_COOKED.value)
        assertEquals("highest_rated", RecipeSortBy.HIGHEST_RATED.value)
        assertEquals("newest", RecipeSortBy.NEWEST.value)
    }

    // MARK: - RecipeFilters Tests

    @Test
    fun `RecipeFilters default values`() {
        // Given
        val filters = RecipeFilters()

        // Then
        assertNull(filters.cookingTimeRange)
        assertNull(filters.category)
        assertNull(filters.searchQuery)
        assertEquals(RecipeSortBy.TRENDING, filters.sortBy)
    }

    @Test
    fun `RecipeFilters with custom values`() {
        // Given
        val filters = RecipeFilters(
            cookingTimeRange = CookingTimeRange.UNDER_15_MIN,
            category = "korean",
            searchQuery = "kimchi",
            sortBy = RecipeSortBy.NEWEST
        )

        // Then
        assertEquals(CookingTimeRange.UNDER_15_MIN, filters.cookingTimeRange)
        assertEquals("korean", filters.category)
        assertEquals("kimchi", filters.searchQuery)
        assertEquals(RecipeSortBy.NEWEST, filters.sortBy)
    }

    @Test
    fun `RecipeFilters copy works correctly`() {
        // Given
        val original = RecipeFilters(
            cookingTimeRange = CookingTimeRange.UNDER_15_MIN,
            sortBy = RecipeSortBy.TRENDING
        )

        // When
        val updated = original.copy(sortBy = RecipeSortBy.NEWEST)

        // Then
        assertEquals(CookingTimeRange.UNDER_15_MIN, updated.cookingTimeRange)
        assertEquals(RecipeSortBy.NEWEST, updated.sortBy)
        // Original unchanged
        assertEquals(RecipeSortBy.TRENDING, original.sortBy)
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
