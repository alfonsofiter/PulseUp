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
import com.pulseup.app.data.local.entity.User
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
import java.util.Calendar

class ActivitiesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val activityRepository = HealthActivityRepository(database.healthActivityDao())
    private val userRepository = UserRepository(database.userDao())
    private val firebaseRepo = FirebaseLeaderboardRepository()
    private val auth = Firebase.auth

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

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val email = auth.currentUser?.email
            if (email != null) {
                val user = userRepository.getUserByEmail(email).firstOrNull()
                _currentUserId.value = user?.id
                user?.id?.let { loadActivities() }
            }
        }
    }

    private suspend fun getCurrentUserId(): Int? {
        if (_currentUserId.value != null) return _currentUserId.value
        val email = auth.currentUser?.email ?: return null
        val user = userRepository.getUserByEmail(email).firstOrNull()
        _currentUserId.value = user?.id
        return user?.id
    }

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
                    activityRepository.getActivitiesByCategory(userId, category).collect { activityList ->
                        _activities.value = activityList
                        _isLoading.value = false
                    }
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    fun addActivity(input: ActivityInput, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId() ?: return@launch
                
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

                // 1. Simpan aktivitas
                activityRepository.insertActivity(activity)
                
                // 2. Tambah poin
                userRepository.addPoints(userId, points)
                
                // 3. Update Streak (Logika diperbaiki)
                updateStreakLogic(userId)

                syncToFirebase(userId)
                onSuccess()
            } catch (e: Exception) {
                Log.e("ACTIVITIES", "Error adding activity", e)
            }
        }
    }

    private suspend fun updateStreakLogic(userId: Int) {
        val user = userRepository.getUserById(userId).firstOrNull() ?: return
        
        val calendar = Calendar.getInstance()
        
        // Waktu awal hari ini (00:00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis
        
        // Waktu awal kemarin (00:00:00)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startOfYesterday = calendar.timeInMillis

        // Ambil SEMUA aktivitas hari ini untuk dicek apakah ini aktivitas pertama
        val countToday = activityRepository.getActivityCountToday(userId)
        
        // Cek aktivitas kemarin
        val countYesterday = activityRepository.getActivityCountBetween(userId, startOfYesterday, startOfToday)

        var newStreak = user.currentStreak

        // Jika ini adalah aktivitas PERTAMA hari ini, baru kita update streaknya
        if (countToday == 1) {
            if (countYesterday > 0) {
                // Berhasil lanjut dari kemarin
                newStreak += 1
                Log.d("STREAK", "🔥 Streak bertambah jadi: $newStreak")
            } else {
                // Kemarin kosong atau streak baru mulai
                newStreak = 1
                Log.d("STREAK", "🌱 Streak mulai dari 1")
            }
            userRepository.updateStreak(userId, newStreak)
        } else {
            Log.d("STREAK", "Aktivitas tambahan hari ini, streak tetap: $newStreak")
        }
    }

    fun updateActivity(activityId: Int, input: ActivityInput, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId() ?: return@launch
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
                    syncToFirebase(userId)
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("ACTIVITIES", "Error updating activity", e)
            }
        }
    }

    fun deleteActivity(activity: HealthActivity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId() ?: return@launch
                activityRepository.deleteActivity(activity)
                userRepository.addPoints(userId, -activity.points)
                syncToFirebase(userId)
                onSuccess()
            } catch (e: Exception) {
                Log.e("ACTIVITIES", "Error deleting activity", e)
            }
        }
    }

    private fun syncToFirebase(userId: Int) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            userRepository.getUserById(userId).firstOrNull()?.let { user ->
                firebaseRepo.syncUserToLeaderboard(
                    userId = user.id,
                    username = user.username,
                    totalPoints = user.totalPoints,
                    level = user.level,
                    currentStreak = user.currentStreak
                )
            }
        }
    }

    suspend fun getActivityById(activityId: Int): HealthActivity? = activityRepository.getActivityById(activityId)
    suspend fun getTotalActivityCount(): Int = getCurrentUserId()?.let { activityRepository.getTotalActivityCount(it) } ?: 0
    suspend fun getTotalCaloriesBurned(): Int = getCurrentUserId()?.let { activityRepository.getTotalCaloriesBurned(it) } ?: 0
    suspend fun getActivityCountToday(): Int = getCurrentUserId()?.let { activityRepository.getActivityCountToday(it) } ?: 0
}