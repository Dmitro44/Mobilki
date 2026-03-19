package com.example.timer.ui.navigation

/**
 * Sealed class hierarchy defining all navigation routes in the app
 * Provides type-safe navigation with compile-time route validation
 * 
 * Usage:
 * ```
 * // Navigate to main screen
 * navController.navigate(Routes.Main.route)
 * 
 * // Navigate to timer screen with sequence ID
 * navController.navigate(Routes.Timer.createRoute(sequenceId = 123))
 * ```
 */
sealed class Routes(val route: String) {
    
    /**
     * Splash screen route - initial app screen
     */
    data object Splash : Routes("splash")
    
    /**
     * Main screen route - displays list of timer sequences
     */
    data object Main : Routes("main")
    
    /**
     * Timer screen route - displays countdown timer for a sequence
     * Requires sequenceId parameter
     */
    data object Timer : Routes("timer/{sequenceId}") {
        
        // Argument name constant
        const val ARG_SEQUENCE_ID = "sequenceId"
        
        /**
         * Create a route with the sequence ID parameter filled in
         * @param sequenceId The ID of the sequence to run
         * @return Complete navigation route string
         */
        fun createRoute(sequenceId: Long): String {
            return "timer/$sequenceId"
        }
    }
    
    /**
     * Edit screen route - create or edit a timer sequence
     * Optional sequenceId parameter (null for create mode)
     */
    data object Edit : Routes("edit?sequenceId={sequenceId}") {
        
        // Argument name constant
        const val ARG_SEQUENCE_ID = "sequenceId"
        
        /**
         * Create a route for creating a new sequence
         * @return Route string for create mode
         */
        fun createRoute(): String {
            return "edit"
        }
        
        /**
         * Create a route for editing an existing sequence
         * @param sequenceId The ID of the sequence to edit
         * @return Complete navigation route string
         */
        fun createRoute(sequenceId: Long): String {
            return "edit?sequenceId=$sequenceId"
        }
    }
    
    /**
     * Settings screen route - user preferences
     */
    data object Settings : Routes("settings")
    
    companion object {
        /**
         * Get all routes for navigation graph setup
         */
        val allRoutes = listOf(Splash, Main, Timer, Edit, Settings)
    }
}
