package com.pulseup.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.User
import com.pulseup.app.data.repository.HealthActivityRepository
import com.pulseup.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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
    private val auth = Firebase.auth

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int?>(null)

    init {
        loadCurrentUser()
    }

    // Load current user from Room based on Firebase Auth email
    private fun loadCurrentUser() {
        viewModelScope.launch {
            val email = auth.currentUser?.email
            if (email != null) {
                val user = userRepository.getUserByEmail(email).firstOrNull()
                _currentUserId.value = user?.id

                // Load dashboard after getting user ID
                user?.id?.let { loadDashboardData() }
            }
        }
    }

    // Get current user ID
    private suspend fun getCurrentUserId(): Int? {
        if (_currentUserId.value != null) {
            return _currentUserId.value
        }

        val email = auth.currentUser?.email ?: return null
        val user = userRepository.getUserByEmail(email).firstOrNull()
        _currentUserId.value = user?.id
        return user?.id
    }

    // Load dashboard data
    fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)

            val userId = getCurrentUserId()

            if (userId != null) {
                // Load user data
                userRepository.getUserById(userId).collect { user ->
                    user?.let {
                        // Load activity statistics
                        val activitiesToday = activityRepository.getActivityCountToday(userId)
                        val pointsToday = activityRepository.getPointsToday(userId)
                        val totalCalories = activityRepository.getTotalCaloriesBurned(userId)

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
            } else {
                _dashboardState.value = _dashboardState.value.copy(isLoading = false)
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