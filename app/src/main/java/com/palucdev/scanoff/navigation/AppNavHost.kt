package com.palucdev.scanoff.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.palucdev.scanoff.R
import com.palucdev.scanoff.ui.document.DocumentDetailScreen
import com.palucdev.scanoff.ui.folders.FolderListScreen
import com.palucdev.scanoff.ui.home.HomeScreen
import com.palucdev.scanoff.ui.scanner.ScannerScreen
import com.palucdev.scanoff.ui.settings.SettingsScreen
import com.palucdev.scanoff.ui.theme.NavSelectedIndicator

/**
 * Bottom navigation tab definition.
 */
private data class TopLevelRoute(
    val labelResId: Int,
    val icon: ImageVector,
    val route: Any,
)

private val topLevelRoutes = listOf(
    TopLevelRoute(R.string.nav_home, Icons.Outlined.Home, HomeRoute),
    TopLevelRoute(R.string.nav_scan, Icons.Outlined.CameraAlt, ScannerRoute),
    TopLevelRoute(R.string.nav_pdfs, Icons.Outlined.Description, FolderListRoute),
    TopLevelRoute(R.string.nav_settings, Icons.Outlined.Settings, SettingsRoute),
)

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Bottom bar is hidden on full-screen pushed destinations
    val showBottomBar = currentDestination?.let { dest ->
        !dest.hasRoute<ScannerRoute>() && !dest.hasRoute<DocumentDetailRoute>()
    } ?: true

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {

                CompositionLocalProvider(
                    LocalRippleConfiguration provides null // disable ripple animation on item press
                ) {
                    NavigationBar {
                        topLevelRoutes.forEach { topRoute ->
                            val isSelected =
                                currentDestination?.hasRoute(topRoute.route::class) == true
                            val label = stringResource(topRoute.labelResId)

                            NavigationBarItem(
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                ),
                                alwaysShowLabel = false,
                                selected = isSelected,
                                onClick = {
                                    if (topRoute.route is ScannerRoute) {
                                        navController.navigate(ScannerRoute) {
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.navigate(topRoute.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                icon = {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(width = 56.dp, height = 56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) NavSelectedIndicator
                                                else androidx.compose.ui.graphics.Color.Transparent
                                            ),
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = topRoute.icon,
                                                contentDescription = label,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp),
                                            )
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 4.dp),
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onNavigateToScanner = {
                        navController.navigate(ScannerRoute) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToDocument = { documentId ->
                        navController.navigate(DocumentDetailRoute(documentId))
                    },
                )
            }

            composable<ScannerRoute> {
                ScannerScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable<FolderListRoute> {
                FolderListScreen(
                    onNavigateToScanner = {
                        navController.navigate(ScannerRoute) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToDocument = { documentId ->
                        navController.navigate(DocumentDetailRoute(documentId))
                    },
                )
            }

            composable<SettingsRoute> {
                SettingsScreen()
            }

            composable<DocumentDetailRoute> { backStackEntry ->
                val route: DocumentDetailRoute = backStackEntry.toRoute()
                DocumentDetailScreen(
                    documentId = route.documentId,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
