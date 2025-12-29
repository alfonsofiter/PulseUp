package com.pulseup.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ActivitiesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val activityRepository = HealthActivityRepository(database.healthActivityDao())
    private val userRepository = UserRepository(database.userDao())
    private val firebaseRepo = FirebaseLeaderboardRepository()
    private val auth = Firebase.auth

    // State untuk UI
    private val _activities = MutableStateFlow<List<HealthActivity>>(emptyList())
    val activities: StateFlow<List<HealthActivity>> = _activities.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ActivityCategory?>(null)
    val selectedCategory: StateFlow<ActivityCategory?> = _selectedCategory.asStateFlow()

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

                // Load activities after getting user ID
                user?.id?.let { loadActivities() }
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

    // Load all activities
    fun loadActivities() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = getCurrentUserId()

            if (userId != null) {
                activityRepository.getActivitiesByUser(userId).collect { activityList ->
                    _activities.value = activityList
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    // Load activities by category
    fun loadActivitiesByCategory(category: ActivityCategory?) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedCategory.value = category
            val userId = getCurrentUserId()

            if (userId != null) {
                if (category == null) {
                    activityRepository.getActivitiesByUser(userId).collect { activityList ->
                        _activities.value = activityList
                        _isLoading.value = false
                    }
                } else {
                    activityRepository
                        .getActivitiesByCategory(userId, category)
                        .collect { activityList ->
                            _activities.value = activityList
                            _isLoading.value = false
                        }
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    // Add new activity
    fun addActivity(input: ActivityInput, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    Log.e("ACTIVITIES", "❌ User ID is null!")
                    return@launch
                }

                Log.d("ACTIVITIES", "🎯 Adding activity: ${input.activityName}")

                val points = input.calculatePoints()
                val calories = input.estimateCalories()

                val activity = HealthActivity(
                    userId = userId,
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
                userRepository.addPoints(userId, points)

                Log.d("ACTIVITIES", "✅ Saved to local DB")

                // Sync ke Firebase Cloud
                syncToFirebase(userId)

                onSuccess()
            } catch (e: Exception) {
                Log.e("ACTIVITIES", "❌ Add activity failed: ${e.message}", e)
            }
        }
    }

    // Update activity
    fun updateActivity(activityId: Int, input: ActivityInput, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    Log.e("ACTIVITIES", "❌ User ID is null!")
                    return@launch
                }

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
                        userRepository.addPoints(userId, pointsDiff)
                    }

                    Log.d("ACTIVITIES", "✅ Activity updated, syncing to Firebase...")

                    // Sync to Firebase after update
                    syncToFirebase(userId)

                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("ACTIVITIES", "❌ Update activity failed: ${e.message}", e)
            }
        }
    }

    // Delete activity
    fun deleteActivity(activity: HealthActivity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    Log.e("ACTIVITIES", "❌ User ID is null!")
                    return@launch
                }

                activityRepository.deleteActivity(activity)
                userRepository.addPoints(userId, -activity.points)

                Log.d("ACTIVITIES", "✅ Activity deleted, syncing to Firebase...")

                // Sync to Firebase after delete
                syncToFirebase(userId)

                onSuccess()
            } catch (e: Exception) {
                Log.e("ACTIVITIES", "❌ Delete activity failed: ${e.message}", e)
            }
        }
    }

    // Sync user data to Firebase (NON-CANCELLABLE)
    private fun syncToFirebase(userId: Int) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            try {
                Log.d("ACTIVITIES", "🔥 Starting Firebase sync...")

                val user = userRepository.getUserById(userId).firstOrNull()

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
                    Log.e("ACTIVITIES", "❌ User not found!")
                }
            } catch (e: Exception) {
                Log.e("ACTIVITIES", "❌ Firebase sync failed: ${e.message}", e)
            }
        }
    }

    // Get activity by ID
    suspend fun getActivityById(activityId: Int): HealthActivity? {
        return activityRepository.getActivityById(activityId)
    }

    // Statistics
    suspend fun getTotalActivityCount(): Int {
        val userId = getCurrentUserId() ?: return 0
        return activityRepository.getTotalActivityCount(userId)
    }

    suspend fun getTotalCaloriesBurned(): Int {
        val userId = getCurrentUserId() ?: return 0
        return activityRepository.getTotalCaloriesBurned(userId)
    }

    suspend fun getActivityCountToday(): Int {
        val userId = getCurrentUserId() ?: return 0
        return activityRepository.getActivityCountToday(userId)
    }
}