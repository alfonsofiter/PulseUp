package com.pulseup.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.User
import com.pulseup.app.data.repository.HealthActivityRepository
import com.pulseup.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardState(
    val user: User? = null,
    val healthScore: Int = 0,
    val activitiesToday: Int = 0,
    val pointsToday: Int = 0,
    val caloriesBurned: Int = 0,
    val isLoading: Boolean = false
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val activityRepository = HealthActivityRepository(database.healthActivityDao())

    private val currentUserId = 1

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    init {
        loadDashboardData()
        initializeUserIfNeeded()
    }

    // Initialize default user if not exists
    private fun initializeUserIfNeeded() {
        viewModelScope.launch {
            val userCount = userRepository.getUserCount()
            if (userCount == 0) {
                // Create default user
                val defaultUser = User(
                    id = 1,
                    username = "John Doe",
                    email = "john.doe@pulseup.com",
                    age = 25,
                    weight = 70f,
                    height = 170f,
                    totalPoints = 0,
                    level = 1,
                    currentStreak = 0,
                    longestStreak = 0
                )
                userRepository.insertUser(defaultUser)
            }
        }
    }

    // Load dashboard data
    fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)

            // Load user data
            userRepository.getUserById(currentUserId).collect { user ->
                user?.let {
                    // Load activity statistics
                    val activitiesToday = activityRepository.getActivityCountToday(currentUserId)
                    val pointsToday = activityRepository.getPointsToday(currentUserId)
                    val totalCalories = activityRepository.getTotalCaloriesBurned(currentUserId)

                    // Calculate health score (0-100)
                    val healthScore = calculateHealthScore(
                        activitiesToday = activitiesToday,
                        streak = it.currentStreak,
                        level = it.level
                    )

                    _dashboardState.value = DashboardState(
                        user = it,
                        healthScore = healthScore,
                        activitiesToday = activitiesToday,
                        pointsToday = pointsToday,
                        caloriesBurned = totalCalories,
                        isLoading = false
                    )
                }
            }
        }
    }

    // Calculate health score algorithm
    private fun calculateHealthScore(activitiesToday: Int, streak: Int, level: Int): Int {
        // Simple algorithm:
        // - Base: 50 points
        // - Activities today: +10 per activity (max 30)
        // - Streak: +2 per day (max 20)
        // - Level bonus: +1 per level (max 10)

        val baseScore = 50
        val activityScore = minOf(activitiesToday * 10, 30)
        val streakScore = minOf(streak * 2, 20)
        val levelBonus = minOf(level, 10)

        return minOf(baseScore + activityScore + streakScore + levelBonus, 100)
    }

    // Refresh dashboard
    fun refresh() {
        loadDashboardData()
    }
}