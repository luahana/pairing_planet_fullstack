package com.cookstemma.app.ui.screens.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cookstemma.app.ui.components.AppIcons
import com.cookstemma.app.ui.components.StarRating
import com.cookstemma.app.ui.navigation.CloseIconButton
import com.cookstemma.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLogScreen(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CreateLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.take(uiState.photosRemaining).forEach { uri ->
            viewModel.addPhoto(uri)
        }
    }

    LaunchedEffect(uiState.isSubmitSuccess) {
        if (uiState.isSubmitSuccess) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    CloseIconButton(onClick = onDismiss)
                },
                actions = {
                    // Post button as icon
                    IconButton(
                        onClick = {
                            // TODO: Convert URIs to Files and submit
                            // viewModel.submit(photoFiles)
                        },
                        enabled = uiState.canSubmit
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                tint = if (uiState.canSubmit)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (uiState.canSubmit)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else
                                            Color.Transparent,
                                        CircleShape
                                    )
                                    .padding(4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Photos Section (Icon-focused)
            PhotosSection(
                photos = uiState.photos,
                onAddPhoto = { photoPickerLauncher.launch("image/*") },
                onRemovePhoto = viewModel::removePhoto,
                photosRemaining = uiState.photosRemaining
            )

            // Rating Section (Icon-focused)
            RatingSection(
                rating = uiState.rating,
                onRatingChange = viewModel::setRating
            )

            // Link Recipe Section (Icon-focused)
            LinkRecipeSection(
                linkedRecipe = uiState.linkedRecipe,
                searchQuery = uiState.recipeSearchQuery,
                searchResults = uiState.recipeSearchResults,
                isSearching = uiState.isSearchingRecipes,
                onSearchQueryChange = viewModel::setRecipeSearchQuery,
                onSelectRecipe = viewModel::selectRecipe,
                onClearRecipe = viewModel::clearLinkedRecipe
            )

            // Content Section (Icon-focused)
            ContentSection(
                content = uiState.content,
                onContentChange = viewModel::setContent
            )

            // Privacy Section (Icon-focused)
            PrivacySection(
                isPrivate = uiState.isPrivate,
                onPrivateChange = viewModel::setPrivate
            )

            // Error Message
            uiState.error?.let { error ->
                Snackbar(
                    action = {
                        IconButton(onClick = viewModel::clearError) {
                            Icon(AppIcons.close, contentDescription = null)
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

// MARK: - Photos Section (Icon-focused)
@Composable
private fun PhotosSection(
    photos: List<Uri>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Uri) -> Unit,
    photosRemaining: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            // Header with icon and count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AppIcons.photo,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${photos.size}/5",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            if (photos.isEmpty()) {
                // Empty state with dashed border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(Spacing.sm)
                        )
                        .clip(RoundedCornerShape(Spacing.sm))
                        .clickable(onClick = onAddPhoto),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Icon(
                            imageVector = AppIcons.addPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(photos) { uri ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(Spacing.sm))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Remove button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .clickable { onRemovePhoto(uri) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AppIcons.close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    // Add more button
                    if (photosRemaining > 0) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(Spacing.sm))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(Spacing.sm)
                                    )
                                    .clickable(onClick = onAddPhoto),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Rating Section (Icon-focused)
@Composable
private fun RatingSection(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Icon(
                imageVector = AppIcons.star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                StarRating(
                    rating = rating,
                    onRatingChange = onRatingChange,
                    size = 40.dp,
                    interactive = true
                )
            }
        }
    }
}

// MARK: - Link Recipe Section (Icon-focused)
@Composable
private fun LinkRecipeSection(
    linkedRecipe: com.cookstemma.app.domain.model.RecipeSummary?,
    searchQuery: String,
    searchResults: List<com.cookstemma.app.domain.model.RecipeSummary>,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectRecipe: (com.cookstemma.app.domain.model.RecipeSummary) -> Unit,
    onClearRecipe: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = AppIcons.recipe,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            if (linkedRecipe != null) {
                // Selected recipe display
                if (linkedRecipe.coverImageUrl != null) {
                    AsyncImage(
                        model = linkedRecipe.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(Spacing.xs)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                }
                Text(
                    text = linkedRecipe.title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                // Clear button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .clickable(onClick = onClearRecipe),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            } else {
                // Search field or forward indicator
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = AppIcons.forward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Search results dropdown (if searching)
        if (linkedRecipe == null && searchResults.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
            ) {
                searchResults.take(5).forEach { recipe ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRecipe(recipe) }
                            .padding(vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (recipe.coverImageUrl != null) {
                            AsyncImage(
                                model = recipe.coverImageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(Spacing.xxs)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                        }
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// MARK: - Content Section (Icon-focused)
@Composable
private fun ContentSection(
    content: String,
    onContentChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Icon(
                imageVector = AppIcons.edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                )
            )
        }
    }
}

// MARK: - Privacy Section (Icon-focused)
@Composable
private fun PrivacySection(
    isPrivate: Boolean,
    onPrivateChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.md),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPrivate) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Switch(
                checked = isPrivate,
                onCheckedChange = onPrivateChange
            )
        }
    }
}
