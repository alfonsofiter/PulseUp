package com.pulseup.app

sealed class Screen(val route: String) {
    object Splash : Screen("splash") // Tambahkan ini
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Dashboard : Screen("dashboard")
    object Activities : Screen("activities")
    object AddActivity : Screen("add_activity")
    object EditActivity : Screen("edit_activity/{activityId}") {
        fun createRoute(activityId: Int) = "edit_activity/$activityId"
    }
    object Leaderboard : Screen("leaderboard")
    object Profile : Screen("profile")
    object BMICalculator : Screen("bmi_calculator")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object AppTheme : Screen("app_theme")
    object HealthGoals : Screen("health_goals")
    object Notifications : Screen("notifications")
    object HelpSupport : Screen("help_support")
    object Onboarding : Screen("onboarding")

    // Rute Baru untuk Chatbot
    object Chatbot : Screen("chatbot")
}