package com.pulseup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pulseup.app.ui.components.PulseUpBottomBar
import com.pulseup.app.ui.screens.activities.ActivitiesScreen
import com.pulseup.app.ui.screens.activities.AddActivityScreen
import com.pulseup.app.ui.screens.auth.LoginScreen
import com.pulseup.app.ui.screens.auth.SignUpScreen
import com.pulseup.app.ui.screens.bmi.BMICalculatorScreen
import com.pulseup.app.ui.screens.dashboard.DashboardScreen
import com.pulseup.app.ui.screens.leaderboard.LeaderboardScreen
import com.pulseup.app.ui.screens.profile.*
import com.pulseup.app.ui.theme.PulseUpTheme
import com.pulseup.app.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState(initial = false)
            
            PulseUpTheme(darkTheme = isDarkMode) {
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

    // Check if user is already logged in
    val startDestination = remember {
        if (Firebase.auth.currentUser != null) {
            Screen.Dashboard.route
        } else {
            Screen.Login.route
        }
    }

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
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onSignUpClick = {
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }

            // Sign Up Screen
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }

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
                    activityId = null,
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
                    activityId = activityId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Leaderboard
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen()
            }

            // BMI Calculator
            composable(Screen.BMICalculator.route) {
                BMICalculatorScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Profile
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToBMI = {
                        navController.navigate(Screen.BMICalculator.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToHelpSupport = {
                        navController.navigate(Screen.HelpSupport.route)
                    },
                    onNavigateToAppTheme = {
                        navController.navigate(Screen.AppTheme.route)
                    },
                    onNavigateToHealthGoals = {
                        navController.navigate(Screen.HealthGoals.route)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Screen.Notifications.route)
                    }
                )
            }

            // Edit Profile
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Help & Support
            composable(Screen.HelpSupport.route) {
                HelpSupportScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // App Theme
            composable(Screen.AppTheme.route) {
                AppThemeScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Health Goals
            composable(Screen.HealthGoals.route) {
                HealthGoalsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Notifications
            composable(Screen.Notifications.route) {
                NotificationSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}