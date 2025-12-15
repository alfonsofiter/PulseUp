package com.pulseup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pulseup.app.ui.components.PulseUpBottomBar
import com.pulseup.app.ui.screens.activities.ActivitiesScreen
import com.pulseup.app.ui.screens.activities.AddActivityScreen
import com.pulseup.app.ui.screens.dashboard.DashboardScreen
import com.pulseup.app.ui.screens.leaderboard.LeaderboardScreen
import com.pulseup.app.ui.screens.profile.ProfileScreen
import com.pulseup.app.ui.theme.PulseUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PulseUpTheme {
                PulseUpApp()
            }
        }
    }
}

@Composable
fun PulseUpApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Screen yang punya bottom navigation
    val screensWithBottomBar = listOf(
        Screen.Dashboard.route,
        Screen.Activities.route,
        Screen.Leaderboard.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in screensWithBottomBar) {
                PulseUpBottomBar(
                    selectedRoute = currentRoute ?: Screen.Dashboard.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to start destination
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                            }
                            // Avoid multiple copies
                            launchSingleTop = true
                            // Restore state when reselecting
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // Activities List
            composable(Screen.Activities.route) {
                ActivitiesScreen(
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // Add Activity
            composable(Screen.AddActivity.route) {
                AddActivityScreen(
                    activityId = null, // null = Add mode
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Edit Activity
            composable(
                route = Screen.EditActivity.route,
                arguments = listOf(
                    navArgument("activityId") {
                        type = NavType.IntType
                    }
                )
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getInt("activityId") ?: 0
                AddActivityScreen(
                    activityId = activityId, // Pass ID untuk edit
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Leaderboard
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen()
            }

            // Profile
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}