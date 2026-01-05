package com.pulseup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
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
import com.pulseup.app.ui.screens.splash.SplashScreen
import com.pulseup.app.ui.theme.PulseUpTheme
import com.pulseup.app.viewmodel.ThemeViewModel
import com.pulseup.app.utils.NotificationWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // JADWALKAN NOTIFIKASI SAAT STARTUP
        NotificationWorker.scheduleNotifications(this)

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
                            popUpTo(Screen.Dashboard.route) { saveState = true }
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
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onNext = {
                    val nextRoute = if (Firebase.auth.currentUser != null) Screen.Dashboard.route else Screen.Login.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.Login.route) {
                Surface(modifier = Modifier.padding(paddingValues)) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onSignUpClick = { navController.navigate(Screen.SignUp.route) }
                    )
                }
            }

            composable(Screen.SignUp.route) {
                Surface(modifier = Modifier.padding(paddingValues)) {
                    SignUpScreen(
                        onSignUpSuccess = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onLoginClick = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Dashboard.route) {
                Surface(modifier = Modifier.padding(paddingValues)) {
                    DashboardScreen(onNavigate = { navController.navigate(it) })
                }
            }

            composable(Screen.Activities.route) {
                Surface(modifier = Modifier.padding(paddingValues)) {
                    ActivitiesScreen(onNavigate = { navController.navigate(it) })
                }
            }

            composable(Screen.AddActivity.route) {
                Surface(modifier = Modifier.padding(paddingValues)) {
                    AddActivityScreen(activityId = null, onNavigateBack = { navController.popBackStack() })
                }
            }

            composable(
                route = Screen.EditActivity.route,
                arguments = listOf(navArgument("activityId") { type = NavType.IntType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getInt("activityId") ?: 0
                Surface(modifier = Modifier.padding(paddingValues)) {
                    AddActivityScreen(activityId = activityId, onNavigateBack = { navController.popBackStack() })
                }
            }

            composable(Screen.Leaderboard.route) {
                Surface(modifier = Modifier.padding(paddingValues)) {
                    LeaderboardScreen()
                }
            }

            composable(Screen.BMICalculator.route) {
                Surface(modifier = Modifier.padding(paddingValues)) {
                    BMICalculatorScreen(onNavigateBack = { navController.popBackStack() })
                }
            }

            composable(Screen.Profile.route) {
                Surface(modifier = Modifier.padding(paddingValues)) {
                    ProfileScreen(
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToBMI = { navController.navigate(Screen.BMICalculator.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }
            }

            composable(Screen.Settings.route) {
                Surface(modifier = Modifier.padding(paddingValues)) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                        onNavigateToHelpSupport = { navController.navigate(Screen.HelpSupport.route) },
                        onNavigateToAppTheme = { navController.navigate(Screen.AppTheme.route) },
                        onNavigateToHealthGoals = { navController.navigate(Screen.HealthGoals.route) },
                        onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
                    )
                }
            }

            composable(Screen.EditProfile.route) { 
                Surface(modifier = Modifier.padding(paddingValues)) {
                    EditProfileScreen(onNavigateBack = { navController.popBackStack() }) 
                }
            }
            composable(Screen.HelpSupport.route) { 
                Surface(modifier = Modifier.padding(paddingValues)) {
                    HelpSupportScreen(onNavigateBack = { navController.popBackStack() }) 
                }
            }
            composable(Screen.AppTheme.route) { 
                Surface(modifier = Modifier.padding(paddingValues)) {
                    AppThemeScreen(onNavigateBack = { navController.popBackStack() }) 
                }
            }
            composable(Screen.HealthGoals.route) { 
                Surface(modifier = Modifier.padding(paddingValues)) {
                    HealthGoalsScreen(onNavigateBack = { navController.popBackStack() }) 
                }
            }
            composable(Screen.Notifications.route) { 
                Surface(modifier = Modifier.padding(paddingValues)) {
                    NotificationSettingsScreen(onNavigateBack = { navController.popBackStack() }) 
                }
            }
        }
    }
}
