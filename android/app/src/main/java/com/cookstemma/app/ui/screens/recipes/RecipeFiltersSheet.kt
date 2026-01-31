package com.cookstemma.app.ui.screens.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cookstemma.app.domain.model.CookingTimeRange
import com.cookstemma.app.domain.model.FoodCategory
import com.cookstemma.app.domain.model.RecipeFilters
import com.cookstemma.app.domain.model.RecipeSortBy
import com.cookstemma.app.domain.model.ServingsRange
import com.cookstemma.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFiltersSheet(
    currentFilters: RecipeFilters,
    onApplyFilters: (RecipeFilters) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    var selectedSortBy by remember { mutableStateOf(currentFilters.sortBy) }
    var selectedCookingTime by remember { mutableStateOf(currentFilters.cookingTimeRange) }
    var selectedCategory by remember { mutableStateOf(currentFilters.category) }
    var selectedServings by remember { mutableStateOf(currentFilters.servingsRange) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
                .padding(bottom = Spacing.xl)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(
                    onClick = {
                        selectedSortBy = RecipeSortBy.TRENDING
                        selectedCookingTime = null
                        selectedCategory = null
                        selectedServings = null
                    }
                ) {
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Sort By Section
            FilterSectionHeader(
                icon = Icons.Default.Sort,
                title = "Sort By"
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                RecipeSortBy.entries.forEach { sortOption ->
                    FilterRadioOption(
                        text = sortOption.displayName,
                        selected = selectedSortBy == sortOption,
                        onClick = { selectedSortBy = sortOption }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(Spacing.lg))

            // Cooking Time Section
            FilterSectionHeader(
                icon = Icons.Default.Timer,
                title = "Cooking Time"
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                FilterRadioOption(
                    text = "Any",
                    selected = selectedCookingTime == null,
                    onClick = { selectedCookingTime = null }
                )
                CookingTimeRange.entries.forEach { timeRange ->
                    FilterRadioOption(
                        text = timeRange.displayName,
                        selected = selectedCookingTime == timeRange,
                        onClick = { selectedCookingTime = timeRange }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(Spacing.lg))

            // Servings Section
            FilterSectionHeader(
                icon = Icons.Default.People,
                title = "Servings"
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                FilterRadioOption(
                    text = "Any",
                    selected = selectedServings == null,
                    onClick = { selectedServings = null }
                )
                ServingsRange.entries.forEach { servings ->
                    FilterRadioOption(
                        text = servings.displayName,
                        selected = selectedServings == servings,
                        onClick = { selectedServings = servings }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(Spacing.lg))

            // Cooking Style Section
            FilterSectionHeader(
                icon = Icons.Default.Restaurant,
                title = "Cooking Style"
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                FilterRadioOption(
                    text = "🌐 All Styles",
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null }
                )
                FoodCategory.entries.forEach { category ->
                    FilterRadioOption(
                        text = "${category.flag} ${category.displayName}",
                        selected = selectedCategory == category.name,
                        onClick = { selectedCategory = category.name }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Apply Button
            Button(
                onClick = {
                    onApplyFilters(
                        RecipeFilters(
                            cookingTimeRange = selectedCookingTime,
                            category = selectedCategory,
                            servingsRange = selectedServings,
                            sortBy = selectedSortBy
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(Spacing.md)
            ) {
                Text("Apply Filters")
            }

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun FilterSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = Spacing.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun FilterRadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // handled by selectable modifier
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Extension to get display name for RecipeSortBy
private val RecipeSortBy.displayName: String
    get() = when (this) {
        RecipeSortBy.TRENDING -> "Trending"
        RecipeSortBy.MOST_COOKED -> "Most Cooked"
        RecipeSortBy.HIGHEST_RATED -> "Highest Rated"
        RecipeSortBy.NEWEST -> "Newest"
    }
