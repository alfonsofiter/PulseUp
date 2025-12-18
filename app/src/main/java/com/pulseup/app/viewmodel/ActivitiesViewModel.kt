package com.pulseup.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.ActivityCategory
import com.pulseup.app.data.local.entity.ActivityInput
import com.pulseup.app.data.local.entity.HealthActivity
import com.pulseup.app.data.repository.HealthActivityRepository
import com.pulseup.app.data.repository.UserRepository
import com.pulseup.app.data.repository.FirebaseLeaderboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ActivitiesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val activityRepository =
        HealthActivityRepository(database.healthActivityDao())
    private val userRepository =
        UserRepository(database.userDao())

    // Firebase Leaderboard Repository
    private val firebaseRepo = FirebaseLeaderboardRepository()

    // Current user ID (hardcoded for now, nanti bisa dari login)
    private val currentUserId = 1

    // State untuk UI
    private val _activities = MutableStateFlow<List<HealthActivity>>(emptyList())
    val activities: StateFlow<List<HealthActivity>> = _activities.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ActivityCategory?>(null)
    val selectedCategory: StateFlow<ActivityCategory?> = _selectedCategory.asStateFlow()

    init {
        loadActivities()
    }

    // Load all activities
    fun loadActivities() {
        viewModelScope.launch {
            _isLoading.value = true
            activityRepository.getActivitiesByUser(currentUserId).collect { activityList ->
                _activities.value = activityList
                _isLoading.value = false
            }
        }
    }

    // Load activities by category
    fun loadActivitiesByCategory(category: ActivityCategory?) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedCategory.value = category

            if (category == null) {
                activityRepository.getActivitiesByUser(currentUserId).collect { activityList ->
                    _activities.value = activityList
                    _isLoading.value = false
                }
            } else {
                activityRepository
                    .getActivitiesByCategory(currentUserId, category)
                    .collect { activityList ->
                        _activities.value = activityList
                        _isLoading.value = false
                    }
            }
        }
    }

    // Add new activity
    fun addActivity(input: ActivityInput, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("ACTIVITIES", "🎯 Adding activity: ${input.activityName}")

                val points = input.calculatePoints()
                val calories = input.estimateCalories()

                val activity = HealthActivity(
                    userId = currentUserId,
                    category = input.category,
                    activityName = input.activityName,
                    description = input.description,
                    points = points,
                    caloriesBurned = calories,
                    duration = input.duration,
                    timestamp = System.currentTimeMillis()
                )

                // Save to local database
                activityRepository.insertActivity(activity)
                userRepository.addPoints(currentUserId, points)

                Log.d("ACTIVITIES", "✅ Saved to local DB")

                // Sync ke Firebase Cloud dengan NON-CANCELLABLE COROUTINE
                launch(Dispatchers.IO + NonCancellable) {
                    try {
                        Log.d("ACTIVITIES", "🔥 Starting Firebase sync...")

                        val user = userRepository.getCurrentUserOnce()

                        if (user != null) {
                            Log.d("ACTIVITIES", "👤 User: ${user.username}, Points: ${user.totalPoints}")

                            firebaseRepo.syncUserToLeaderboard(
                                userId = user.id,
                                username = user.username,
                                totalPoints = user.totalPoints,
                                level = user.level,
                                currentStreak = user.currentStreak
                            )

                            Log.d("ACTIVITIES", "✅ Firebase sync completed!")
                        } else {
                            Log.e("ACTIVITIES", "❌ User is null!")
                        }
                    } catch (e: Exception) {
                        Log.e("ACTIVITIES", "❌ Firebase sync failed: ${e.message}", e)
                    }
                }

                // Call onSuccess IMMEDIATELY after local save
                onSuccess()
            } catch (e: Exception) {
                Log.e("ACTIVITIES", "❌ Add activity failed: ${e.message}", e)
            }
        }
    }

    // Update activity
    fun updateActivity(activityId: Int, input: ActivityInput, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val existingActivity = activityRepository.getActivityById(activityId)

            existingActivity?.let { old ->
                val newPoints = input.calculatePoints()
                val newCalories = input.estimateCalories()
                val pointsDiff = newPoints - old.points

                val updatedActivity = old.copy(
                    category = input.category,
                    activityName = input.activityName,
                    description = input.description,
                    points = newPoints,
                    caloriesBurned = newCalories,
                    duration = input.duration
                )

                activityRepository.updateActivity(updatedActivity)

                if (pointsDiff != 0) {
                    userRepository.addPoints(currentUserId, pointsDiff)
                }

                onSuccess()
            }
        }
    }

    // Delete activity
    fun deleteActivity(activity: HealthActivity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            activityRepository.deleteActivity(activity)
            userRepository.addPoints(currentUserId, -activity.points)
            onSuccess()
        }
    }

    // Get activity by ID
    suspend fun getActivityById(activityId: Int): HealthActivity? {
        return activityRepository.getActivityById(activityId)
    }

    // Statistics
    suspend fun getTotalActivityCount(): Int {
        return activityRepository.getTotalActivityCount(currentUserId)
    }

    suspend fun getTotalCaloriesBurned(): Int {
        return activityRepository.getTotalCaloriesBurned(currentUserId)
    }

    suspend fun getActivityCountToday(): Int {
        return activityRepository.getActivityCountToday(currentUserId)
    }
}
