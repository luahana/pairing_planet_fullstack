package com.cookstemma.app.ui.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cookstemma.app.ui.components.*
import com.cookstemma.app.ui.theme.Spacing

enum class SavedTab { RECIPES, LOGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: SavedViewModel = hiltViewModel(),
    onRecipeClick: (String) -> Unit,
    onLogClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(SavedTab.RECIPES) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    // Icon header
                    Icon(
                        imageVector = AppIcons.save,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = Spacing.md)
                            .size(28.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Icon Tab Selector
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                shape = RoundedCornerShape(Spacing.md),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xs)
                ) {
                    SavedTab.values().forEach { tab ->
                        SavedTabIconButton(
                            icon = when (tab) {
                                SavedTab.RECIPES -> AppIcons.recipe
                                SavedTab.LOGS -> AppIcons.log
                            },
                            isSelected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                selectedTab == SavedTab.RECIPES && uiState.savedRecipes.isEmpty() -> {
                    IconEmptyState(
                        icon = AppIcons.saveOutline,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                selectedTab == SavedTab.LOGS && uiState.savedLogs.isEmpty() -> {
                    IconEmptyState(
                        icon = AppIcons.saveOutline,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        when (selectedTab) {
                            SavedTab.RECIPES -> {
                                items(uiState.savedRecipes, key = { it.id }) { recipe ->
                                    SavedGridItem(
                                        imageUrl = recipe.coverImageUrl,
                                        onClick = { onRecipeClick(recipe.id) }
                                    )
                                }
                            }
                            SavedTab.LOGS -> {
                                items(uiState.savedLogs, key = { it.id }) { log ->
                                    SavedLogGridItem(
                                        imageUrl = log.thumbnailUrl,
                                        rating = log.rating ?: 0,
                                        onClick = { onLogClick(log.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Tab Icon Button
@Composable
private fun SavedTabIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(Spacing.xxs))
        // Selection indicator dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CircleShape
                )
        )
    }
}

// MARK: - Saved Grid Item
@Composable
private fun SavedGridItem(
    imageUrl: String?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Spacing.sm),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// MARK: - Saved Log Grid Item (with rating overlay)
@Composable
private fun SavedLogGridItem(
    imageUrl: String?,
    rating: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(Spacing.sm))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Rating stars overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(4.dp)
        ) {
            repeat(rating) {
                Icon(
                    imageVector = AppIcons.star,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color(0xFFFFD700)
                )
            }
        }
    }
}
