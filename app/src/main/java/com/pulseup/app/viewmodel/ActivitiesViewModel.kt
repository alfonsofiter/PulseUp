package com.pulseup.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.ActivityCategory
import com.pulseup.app.data.local.entity.ActivityInput
import com.pulseup.app.data.local.entity.HealthActivity
import com.pulseup.app.data.repository.HealthActivityRepository
import com.pulseup.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActivitiesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val activityRepository = HealthActivityRepository(database.healthActivityDao())
    private val userRepository = UserRepository(database.userDao())

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
                // Load all
                activityRepository.getActivitiesByUser(currentUserId).collect { activityList ->
                    _activities.value = activityList
                    _isLoading.value = false
                }
            } else {
                // Filter by category
                activityRepository.getActivitiesByCategory(currentUserId, category).collect { activityList ->
                    _activities.value = activityList
                    _isLoading.value = false
                }
            }
        }
    }

    // Add new activity
    fun addActivity(input: ActivityInput, onSuccess: () -> Unit) {
        viewModelScope.launch {
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

            activityRepository.insertActivity(activity)

            // Update user points
            userRepository.addPoints(currentUserId, points)

            // Call success callback
            onSuccess()
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

                // Update user points (add difference)
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

            // Subtract points from user
            userRepository.addPoints(currentUserId, -activity.points)

            onSuccess()
        }
    }

    // Get activity by ID (for edit)
    suspend fun getActivityById(activityId: Int): HealthActivity? {
        return activityRepository.getActivityById(activityId)
    }

    // Get statistics
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