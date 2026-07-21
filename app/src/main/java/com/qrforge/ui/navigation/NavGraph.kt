package com.qrforge.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qrforge.ui.components.QrBottomNavigation
import com.qrforge.ui.screens.home.HomeScreen
import com.qrforge.ui.screens.create.ChooseTypeScreen
import com.qrforge.ui.screens.create.EnterDataScreen
import com.qrforge.ui.screens.create.CustomizeScreen
import com.qrforge.ui.screens.create.QrResultScreen
import com.qrforge.ui.screens.templates.TemplatesScreen
import com.qrforge.ui.screens.templates.TemplatePreviewScreen
import com.qrforge.ui.screens.history.HistoryScreen
import com.qrforge.ui.screens.settings.SettingsScreen
import com.qrforge.ui.screens.detail.QrDetailScreen

@Composable
fun QRForgeNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Templates.route,
        Screen.History.route,
        Screen.Settings.route
    )
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                QrBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = com.qrforge.ui.theme.Background0
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(animationSpec = tween(300)) { it / 8 }
            },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) +
                    slideOutHorizontally(animationSpec = tween(200)) { it / 4 }
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToCreate = { navController.navigate(Screen.ChooseType.route) },
                    onNavigateToEnterData = { qrType ->
                        navController.navigate(Screen.EnterData.createRoute(qrType))
                    },
                    onNavigateToDetail = { id -> navController.navigate(Screen.QrDetail.createRoute(id)) },
                    onNavigateToTemplates = { navController.navigate(Screen.Templates.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) }
                )
            }

            composable(Screen.Templates.route) {
                TemplatesScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToPreview = { id -> navController.navigate(Screen.TemplatePreview.createRoute(id)) },
                    onNavigateToCreate = { navController.navigate(Screen.ChooseType.route) }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToDetail = { id -> navController.navigate(Screen.QrDetail.createRoute(id)) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = hiltViewModel())
            }

            composable(Screen.ChooseType.route) {
                ChooseTypeScreen(
                    onTypeSelected = { qrType ->
                        navController.navigate(Screen.EnterData.createRoute(qrType))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EnterData.route,
                arguments = listOf(
                    navArgument("qrType") { type = NavType.StringType },
                    navArgument("templateId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val qrType = backStackEntry.arguments?.getString("qrType") ?: "url"
                val templateId = backStackEntry.arguments?.getLong("templateId") ?: -1L
                EnterDataScreen(
                    onDataEntered = { historyId ->
                        navController.navigate(Screen.Customize.createRoute(qrType, historyId, templateId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Customize.route,
                arguments = listOf(
                    navArgument("qrType") { type = NavType.StringType },
                    navArgument("historyId") { type = NavType.LongType },
                    navArgument("templateId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                CustomizeScreen(
                    onQrGenerated = { id ->
                        navController.navigate(Screen.QrResult.createRoute(id)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.QrResult.route,
                arguments = listOf(navArgument("historyId") { type = NavType.LongType })
            ) { backStackEntry ->
                val historyId = backStackEntry.arguments?.getLong("historyId") ?: 0L
                QrResultScreen(
                    onBack = { navController.popBackStack(Screen.Home.route, false) },
                    onBackToHome = { navController.popBackStack(Screen.Home.route, false) }
                )
            }

            composable(
                route = Screen.QrDetail.route,
                arguments = listOf(navArgument("historyId") { type = NavType.LongType })
            ) { backStackEntry ->
                val historyId = backStackEntry.arguments?.getLong("historyId") ?: 0L
                QrDetailScreen(
                    historyId = historyId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.TemplatePreview.route,
                arguments = listOf(navArgument("templateId") { type = NavType.LongType })
            ) { backStackEntry ->
                val templateId = backStackEntry.arguments?.getLong("templateId") ?: 0L
                TemplatePreviewScreen(
                    templateId = templateId,
                    onApply = { qrType, id ->
                        navController.navigate(Screen.EnterData.createRoute(qrType, id)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
