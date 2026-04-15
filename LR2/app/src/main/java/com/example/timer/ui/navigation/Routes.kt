package com.example.timer.ui.navigation

sealed class Routes(val route: String) {
    
    data object Splash : Routes("splash")
    
    data object Main : Routes("main")
    
    data object Timer : Routes("timer/{sequenceId}") {
        
        const val ARG_SEQUENCE_ID = "sequenceId"
        
        fun createRoute(sequenceId: Long): String {
            return "timer/$sequenceId"
        }
    }
    
    data object Edit : Routes("edit?sequenceId={sequenceId}") {
        
        const val ARG_SEQUENCE_ID = "sequenceId"
        
        fun createRoute(): String {
            return "edit"
        }
        
        fun createRoute(sequenceId: Long): String {
            return "edit?sequenceId=$sequenceId"
        }
    }
    
    data object Settings : Routes("settings")
    
    companion object {
        val allRoutes = listOf(Splash, Main, Timer, Edit, Settings)
    }
}
