package com.example.timer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.timer.ui.ViewModelFactory
import com.example.timer.ui.screen.EditScreen
import com.example.timer.ui.screen.MainScreen
import com.example.timer.ui.screen.SettingsScreen
import com.example.timer.ui.screen.SplashScreen
import com.example.timer.ui.screen.TimerScreen
import com.example.timer.ui.viewmodel.EditViewModel
import com.example.timer.ui.viewmodel.MainViewModel
import com.example.timer.ui.viewmodel.SettingsViewModel
import com.example.timer.ui.viewmodel.TimerViewModel

/**
 * Main navigation host for the Timer application
 * 
 * Defines the navigation graph and handles screen composition
 * Uses Jetpack Compose Navigation for declarative routing
 * 
 * @param navController Navigation controller for managing navigation
 * @param viewModelFactory Factory for creating ViewModels with dependencies
 * @param modifier Optional modifier for the NavHost
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModelFactory: ViewModelFactory,
    startDestination: String = Routes.Splash.route,
    modifier: Modifier = Modifier,
    onSplashPassed: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Splash screen - initial app screen
        composable(route = Routes.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                    onSplashPassed()
                }
            )
        }
        
        // Main screen - list of timer sequences
        composable(route = Routes.Main.route) {
            val mainViewModel: MainViewModel = viewModel(factory = viewModelFactory)
            
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToTimer = { sequenceId ->
                    navController.navigate(Routes.Timer.createRoute(sequenceId)) {
                        // Ensure back navigation doesn't accidentally hit Splash
                        popUpTo(Routes.Main.route) { inclusive = false }
                    }
                },
                onNavigateToCreate = {
                    navController.navigate(Routes.Edit.createRoute())
                },
                onNavigateToEdit = { sequenceId ->
                    navController.navigate(Routes.Edit.createRoute(sequenceId))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings.route)
                }
            )
        }
        
        // Timer screen - countdown and controls
        composable(
            route = Routes.Timer.route,
            arguments = listOf(
                navArgument(Routes.Timer.ARG_SEQUENCE_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val sequenceId = backStackEntry.arguments?.getLong(Routes.Timer.ARG_SEQUENCE_ID) ?: 0L
            val timerViewModel: TimerViewModel = viewModel(factory = viewModelFactory)
            
            TimerScreen(
                viewModel = timerViewModel,
                sequenceId = sequenceId,
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.Main.route) {
                            popUpTo(Routes.Timer.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        // Edit screen - create or edit a sequence
        composable(
            route = Routes.Edit.route,
            arguments = listOf(
                navArgument(Routes.Edit.ARG_SEQUENCE_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val sequenceIdString = backStackEntry.arguments?.getString(Routes.Edit.ARG_SEQUENCE_ID)
            val sequenceId = sequenceIdString?.toLongOrNull()
            val editViewModel: EditViewModel = viewModel(factory = viewModelFactory)
            
            EditScreen(
                viewModel = editViewModel,
                sequenceId = if (sequenceId == 0L) null else sequenceId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Settings screen - user preferences
        composable(route = Routes.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
