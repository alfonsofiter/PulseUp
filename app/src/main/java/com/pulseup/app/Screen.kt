package com.pulseup.app

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Activities : Screen("activities")
    object AddActivity : Screen("add_activity")
    object EditActivity : Screen("edit_activity/{activityId}") {
        fun createRoute(activityId: Int) = "edit_activity/$activityId"
    }
    object Leaderboard : Screen("leaderboard")
    object Profile : Screen("profile")
    object Onboarding : Screen("onboarding")
}