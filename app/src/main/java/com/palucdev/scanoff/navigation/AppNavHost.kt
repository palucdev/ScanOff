package com.palucdev.scanoff.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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

/**
 * Bottom navigation tab definition.
 */
private data class TopLevelRoute(
    val labelResId: Int,
    val icon: ImageVector,
    val route: Any,
)

private val topLevelRoutes = listOf(
    TopLevelRoute(R.string.nav_home, Icons.Default.Home, HomeRoute),
    TopLevelRoute(R.string.nav_scan, Icons.Default.CameraAlt, ScannerRoute),
    TopLevelRoute(R.string.nav_pdfs, Icons.Default.PictureAsPdf, FolderListRoute),
    TopLevelRoute(R.string.nav_settings, Icons.Default.Settings, SettingsRoute),
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
                NavigationBar {
                    topLevelRoutes.forEach { topRoute ->
                        val isSelected = currentDestination?.hasRoute(topRoute.route::class) == true

                        NavigationBarItem(
                            icon = {
                                Icon(topRoute.icon, contentDescription = stringResource(topRoute.labelResId))
                            },
                            label = { Text(stringResource(topRoute.labelResId)) },
                            selected = isSelected,
                            onClick = {
                                // Scan tab always pushes as a fresh full-screen destination
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
                        )
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
