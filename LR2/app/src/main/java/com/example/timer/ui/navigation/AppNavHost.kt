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
        
        composable(route = Routes.Main.route) {
            val mainViewModel: MainViewModel = viewModel(factory = viewModelFactory)
            
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToTimer = { sequenceId ->
                    navController.navigate(Routes.Timer.createRoute(sequenceId)) {
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
