package com.cookstemma.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cookstemma.app.ui.AppState
import com.cookstemma.app.ui.components.AppIcons
import com.cookstemma.app.ui.theme.Spacing

// MARK: - Bottom Navigation Items (Icons Only)
sealed class BottomNavItem(
    val route: String,
    val requiresAuth: Boolean = false
) {
    abstract val icon: ImageVector
    abstract val selectedIcon: ImageVector

    object Home : BottomNavItem(route = "home", requiresAuth = false) {
        override val icon: ImageVector get() = AppIcons.homeOutline
        override val selectedIcon: ImageVector get() = AppIcons.home
    }
    object Recipes : BottomNavItem(route = "recipes", requiresAuth = false) {
        override val icon: ImageVector get() = AppIcons.recipesOutline
        override val selectedIcon: ImageVector get() = AppIcons.recipes
    }
    object Create : BottomNavItem(route = "create", requiresAuth = true) {
        override val icon: ImageVector get() = AppIcons.createOutline
        override val selectedIcon: ImageVector get() = AppIcons.create
    }
    object Search : BottomNavItem(route = "search", requiresAuth = false) {
        override val icon: ImageVector get() = AppIcons.search
        override val selectedIcon: ImageVector get() = AppIcons.search
    }
    object Profile : BottomNavItem(route = "profile", requiresAuth = true) {
        override val icon: ImageVector get() = AppIcons.profileOutline
        override val selectedIcon: ImageVector get() = AppIcons.profile
    }

    companion object {
        val items: List<BottomNavItem> get() = listOf(Home, Recipes, Create, Search, Profile)
    }
}

// MARK: - Main Scaffold with Custom Bottom Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    onCreateClick: () -> Unit,
    notificationCount: Int = 0,
    appState: AppState? = null,
    isAuthenticated: Boolean = false,
    onTabReselect: ((String) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if bottom bar should be shown
    val showBottomBar = currentDestination?.route in listOf(
        BottomNavItem.Home.route,
        BottomNavItem.Recipes.route,
        BottomNavItem.Search.route,
        BottomNavItem.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CustomBottomBar(
                    navController = navController,
                    onCreateClick = onCreateClick,
                    notificationCount = notificationCount,
                    appState = appState,
                    isAuthenticated = isAuthenticated,
                    onTabReselect = onTabReselect
                )
            }
        }
    ) { padding ->
        content(padding)
    }
}

// MARK: - Custom Bottom Navigation Bar (Icons Only)
@Composable
fun CustomBottomBar(
    navController: NavHostController,
    onCreateClick: () -> Unit,
    notificationCount: Int = 0,
    appState: AppState? = null,
    isAuthenticated: Boolean = false,
    onTabReselect: ((String) -> Unit)? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                val badge = if (item == BottomNavItem.Profile && notificationCount > 0) notificationCount else 0

                BottomNavItemButton(
                    item = item,
                    isSelected = selected,
                    badge = badge,
                    onClick = {
                        handleBottomNavClick(
                            item = item,
                            navController = navController,
                            onCreateClick = onCreateClick,
                            appState = appState,
                            isAuthenticated = isAuthenticated,
                            isReselect = selected,
                            onTabReselect = onTabReselect
                        )
                    }
                )
            }
        }
    }
}

/**
 * Handle bottom nav item click with auth check.
 * Following iOS pattern: Create and Profile require auth.
 * If the tab is already selected (reselect), trigger scroll to top.
 */
private fun handleBottomNavClick(
    item: BottomNavItem,
    navController: NavHostController,
    onCreateClick: () -> Unit,
    appState: AppState?,
    isAuthenticated: Boolean,
    isReselect: Boolean = false,
    onTabReselect: ((String) -> Unit)? = null
) {
    // If tab is already selected, trigger scroll to top
    if (isReselect && item != BottomNavItem.Create) {
        onTabReselect?.invoke(item.route)
        return
    }

    val navigateAction = {
        if (item == BottomNavItem.Create) {
            onCreateClick()
        } else {
            navController.navigate(item.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    
    // If appState is provided and item requires auth, use auth check
    if (appState != null && item.requiresAuth) {
        appState.requireAuth(isAuthenticated, navigateAction)
    } else {
        navigateAction()
    }
}

// MARK: - Bottom Nav Item Button (Icon Only)
@Composable
private fun BottomNavItemButton(
    item: BottomNavItem,
    isSelected: Boolean,
    badge: Int = 0,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "iconColor"
    )

    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.icon,
                contentDescription = null,
                tint = iconColor,
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
        Spacer(Modifier.height(Spacing.xxxs))
        // Selection indicator dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                    CircleShape
                )
        )
    }
}

// MARK: - Create Navigation Button (Prominent Center)
@Composable
private fun CreateNavButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .offset(y = (-12).dp)
            .size(56.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(28.dp)
        )
    }
}

// MARK: - Top App Bar (Icon-Based)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconTopAppBar(
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = { },
        navigationIcon = { navigationIcon?.invoke() },
        actions = { actions?.invoke(this) },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// MARK: - Back Button
@Composable
fun BackIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = AppIcons.back,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

// MARK: - Close Button
@Composable
fun CloseIconButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                CircleShape
            )
    ) {
        Icon(
            imageVector = AppIcons.close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

// MARK: - More Options Button
@Composable
fun MoreOptionsIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = AppIcons.more,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

// MARK: - Notification Button with Badge
@Composable
fun NotificationIconButton(
    count: Int = 0,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Box {
            Icon(
                imageVector = if (count > 0) AppIcons.notifications else AppIcons.notificationsOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
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
}

// MARK: - Search Icon Button
@Composable
fun SearchIconButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = AppIcons.search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

// MARK: - Filter Icon Button
@Composable
fun FilterIconButton(onClick: () -> Unit, hasActiveFilters: Boolean = false) {
    IconButton(onClick = onClick) {
        Box {
            Icon(
                imageVector = AppIcons.filter,
                contentDescription = null,
                tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (hasActiveFilters) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}
