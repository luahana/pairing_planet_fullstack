package com.cookstemma.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.cookstemma.app.ui.theme.AvatarSize
import com.cookstemma.app.ui.theme.Spacing

// MARK: - Avatar View (matches iOS AvatarView)
@Composable
fun AvatarView(
    url: String?,
    name: String? = null,
    size: Dp = AvatarSize.md,
    modifier: Modifier = Modifier
) {
    val isValidUrl = url != null && url.isNotEmpty() &&
            (url.startsWith("http://") || url.startsWith("https://"))

    val initial = name?.let { n ->
        val cleanName = if (n.startsWith("@")) n.drop(1) else n
        cleanName.firstOrNull()?.uppercase() ?: ""
    } ?: ""

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isValidUrl) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    // Show initial while loading
                    AvatarFallback(initial = initial, size = size)
                },
                error = {
                    // Show fallback on error
                    AvatarFallback(initial = initial, size = size)
                }
            )
        } else {
            AvatarFallback(initial = initial, size = size)
        }
    }
}

@Composable
private fun AvatarFallback(
    initial: String,
    size: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        if (initial.isNotEmpty()) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (size.value * 0.4f).sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

// MARK: - App Icons
object AppIcons {
    // Tab Bar
    val home = Icons.Filled.Home
    val homeOutline = Icons.Outlined.Home
    val recipes = Icons.Filled.MenuBook
    val recipesOutline = Icons.Outlined.MenuBook
    val create = Icons.Filled.AddCircle
    val createOutline = Icons.Outlined.AddCircle
    val saved = Icons.Filled.Bookmark
    val savedOutline = Icons.Outlined.BookmarkBorder
    val profile = Icons.Filled.Person
    val profileOutline = Icons.Outlined.Person

    // Actions
    val like = Icons.Filled.Favorite
    val likeOutline = Icons.Outlined.FavoriteBorder
    val comment = Icons.Filled.ChatBubble
    val commentOutline = Icons.Outlined.ChatBubbleOutline
    val share = Icons.Filled.Share
    val save = Icons.Filled.Bookmark
    val saveOutline = Icons.Outlined.BookmarkBorder
    val more = Icons.Filled.MoreVert

    // Navigation
    val back = Icons.Filled.ArrowBack
    val forward = Icons.Filled.ArrowForward
    val close = Icons.Filled.Close
    val search = Icons.Filled.Search
    val filter = Icons.Filled.FilterList
    val sort = Icons.Filled.Sort
    val notifications = Icons.Filled.Notifications
    val notificationsOutline = Icons.Outlined.Notifications
    val settings = Icons.Filled.Settings

    // Content
    val recipe = Icons.Filled.MenuBook
    val log = Icons.Filled.CameraAlt
    val photo = Icons.Filled.Photo
    val addPhoto = Icons.Filled.AddPhotoAlternate
    val timer = Icons.Filled.Timer
    val servings = Icons.Filled.People
    val star = Icons.Filled.Star
    val starOutline = Icons.Outlined.Star
    val fire = Icons.Filled.LocalFireDepartment
    val chef = Icons.Filled.Restaurant

    // Social
    val follow = Icons.Filled.PersonAdd
    val following = Icons.Filled.HowToReg
    val followers = Icons.Filled.People
    val block = Icons.Filled.Block
    val report = Icons.Filled.Flag

    // Status
    val success = Icons.Filled.CheckCircle
    val error = Icons.Filled.Error
    val warning = Icons.Filled.Warning
    val info = Icons.Filled.Info
    val empty = Icons.Outlined.Inbox

    // Edit
    val edit = Icons.Filled.Edit
    val delete = Icons.Filled.Delete
    val camera = Icons.Filled.CameraAlt
    val gallery = Icons.Filled.PhotoLibrary

    // Search & Filter
    val history = Icons.Filled.History
    val trending = Icons.Filled.TrendingUp
    val trash = Icons.Filled.Delete
    val checkmark = Icons.Filled.Check
    val checkmarkAll = Icons.Filled.DoneAll
    val newBadge = Icons.Filled.FiberNew
    val reset = Icons.Filled.Refresh
    val gridView = Icons.Filled.GridView
}

// MARK: - Icon Action Button
@Composable
fun IconActionButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    inactiveColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    size: Float = 24f,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        label = "iconColor"
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size.dp)
        )
    }
}

// MARK: - Icon with Count (Compact)
@Composable
fun IconWithCount(
    icon: ImageVector,
    activeIcon: ImageVector,
    count: Int,
    isActive: Boolean = false,
    activeColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "iconColor"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isActive) activeIcon else icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        if (count > 0) {
            Text(
                text = count.abbreviated(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// MARK: - Icon Badge
@Composable
fun IconBadge(
    icon: ImageVector,
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        if (count > 0) {
            Badge(
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp)
            ) {
                Text(if (count > 99) "99+" else count.toString())
            }
        }
    }
}

// MARK: - Tab Icon
@Composable
fun TabIcon(
    icon: ImageVector,
    activeIcon: ImageVector,
    isSelected: Boolean,
    badge: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Icon(
            imageVector = if (isSelected) activeIcon else icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        if (badge > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            )
        }
    }
}

// MARK: - Follow Icon Button
@Composable
fun FollowIconButton(
    isFollowing: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isFollowing) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    }

    val iconColor = if (isFollowing) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = if (isFollowing) AppIcons.following else AppIcons.follow,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// MARK: - Stat Icon
@Composable
fun StatIcon(
    icon: ImageVector,
    value: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value.abbreviated(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// MARK: - Rating Badge
@Composable
fun RatingBadge(rating: Double, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.xxxs),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = AppIcons.star,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color(0xFFFFD700),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = String.format("%.1f", rating),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// MARK: - Time Badge
@Composable
fun TimeBadge(minutes: Int, modifier: Modifier = Modifier) {
    TimeBadge(
        text = if (minutes < 60) "${minutes}m" else "${minutes / 60}h",
        modifier = modifier
    )
}

@Composable
fun TimeBadge(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = AppIcons.timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// MARK: - Servings Badge
@Composable
fun ServingsBadge(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = AppIcons.servings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// MARK: - Cook Count Badge
@Composable
fun CookCountBadge(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = AppIcons.chef,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = count.abbreviated(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// MARK: - Empty State (Icon-Focused)
@Composable
fun IconEmptyState(
    icon: ImageVector,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        subtitle?.let {
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// MARK: - Action Row (Icons Only)
@Composable
fun ActionRow(
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    isSaved: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            // Like
            Row(
                modifier = Modifier.clickable(onClick = onLikeClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconWithCount(
                    icon = AppIcons.likeOutline,
                    activeIcon = AppIcons.like,
                    count = likeCount,
                    isActive = isLiked,
                    activeColor = MaterialTheme.colorScheme.error
                )
            }

            // Comment
            Row(
                modifier = Modifier.clickable(onClick = onCommentClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconWithCount(
                    icon = AppIcons.commentOutline,
                    activeIcon = AppIcons.comment,
                    count = commentCount
                )
            }

            // Share
            IconButton(onClick = onShareClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = AppIcons.share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Save
        IconButton(onClick = onSaveClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (isSaved) AppIcons.save else AppIcons.saveOutline,
                contentDescription = null,
                tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// MARK: - Create FAB (Prominent)
@Composable
fun CreateFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
    }
}

// MARK: - Extension for number abbreviation
fun Int.abbreviated(): String {
    return when {
        this < 1000 -> this.toString()
        this < 10000 -> String.format("%.1fK", this / 1000.0).replace(".0K", "K")
        this < 1000000 -> "${this / 1000}K"
        else -> String.format("%.1fM", this / 1000000.0)
    }
}
