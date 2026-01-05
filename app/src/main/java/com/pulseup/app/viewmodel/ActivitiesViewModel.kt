package com.pulseup.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.ActivityCategory
import com.pulseup.app.data.local.entity.ActivityInput
import com.pulseup.app.data.local.entity.HealthActivity
import com.pulseup.app.data.repository.HealthActivityRepository
import com.pulseup.app.data.repository.UserRepository
import com.pulseup.app.data.repository.FirebaseLeaderboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class ActivitiesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val activityRepository = HealthActivityRepository(database.healthActivityDao())
    private val userRepository = UserRepository(database.userDao())
    private val firebaseRepo = FirebaseLeaderboardRepository()
    private val auth = Firebase.auth
    private val db = Firebase.database.reference

    private val _activities = MutableStateFlow<List<HealthActivity>>(emptyList())
    val activities: StateFlow<List<HealthActivity>> = _activities.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var activityJob: Job? = null

    init { loadActivities() }

    private fun getCurrentFirebaseUid(): String? = auth.currentUser?.uid

    fun loadActivities() {
        activityJob?.cancel()
        activityJob = viewModelScope.launch {
            _isLoading.value = true
            val uid = getCurrentFirebaseUid()
            if (uid != null) {
                activityRepository.getActivitiesByUser(uid).collect { activityList ->
                    _activities.value = activityList
                    _isLoading.value = false
                }
            } else { _isLoading.value = false }
        }
    }

    fun loadActivitiesByCategory(category: ActivityCategory?) {
        activityJob?.cancel()
        activityJob = viewModelScope.launch {
            _isLoading.value = true
            val uid = getCurrentFirebaseUid()
            if (uid != null) {
                val flow = if (category == null) activityRepository.getActivitiesByUser(uid) 
                          else activityRepository.getActivitiesByCategory(uid, category)
                flow.collect { 
                    _activities.value = it
                    _isLoading.value = false 
                }
            }
        }
    }

    suspend fun getActivityById(activityId: Int): HealthActivity? = activityRepository.getActivityById(activityId)

    fun addActivity(input: ActivityInput, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val uid = getCurrentFirebaseUid() ?: return@launch
                val email = auth.currentUser?.email ?: return@launch
                
                val points = input.calculatePoints()
                val activity = HealthActivity(
                    userId = uid,
                    category = input.category,
                    activityName = input.activityName,
                    description = input.description,
                    points = points,
                    caloriesBurned = input.estimateCalories(),
                    duration = input.duration,
                    timestamp = System.currentTimeMillis()
                )

                activityRepository.insertActivity(activity)
                db.child("activities").child(uid).push().setValue(activity)
                
                userRepository.getUserByEmail(email).firstOrNull()?.let { user ->
                    val newTotalPoints = user.totalPoints + points
                    val updatedUser = user.copy(totalPoints = newTotalPoints, level = (newTotalPoints / 500) + 1)
                    userRepository.updateUser(updatedUser)
                    updateStreakLogic(user.id, uid)
                    syncToFirebase(uid, updatedUser)
                }
                onSuccess()
            } catch (e: Exception) { Log.e("ACTIVITIES", "Error adding", e) }
        }
    }

    fun updateActivity(activityId: Int, input: ActivityInput, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val uid = getCurrentFirebaseUid() ?: return@launch
                val email = auth.currentUser?.email ?: return@launch
                val oldActivity = activityRepository.getActivityById(activityId) ?: return@launch
                val pointsDiff = input.calculatePoints() - oldActivity.points

                val updatedActivity = oldActivity.copy(
                    category = input.category,
                    activityName = input.activityName,
                    description = input.description,
                    points = input.calculatePoints(),
                    caloriesBurned = input.estimateCalories(),
                    duration = input.duration
                )

                activityRepository.updateActivity(updatedActivity)
                
                // Update Firebase
                val snapshot = db.child("activities").child(uid)
                    .orderByChild("timestamp")
                    .equalTo(oldActivity.timestamp.toDouble())
                    .get().await()
                
                snapshot.children.forEach { it.ref.setValue(updatedActivity) }

                userRepository.getUserByEmail(email).firstOrNull()?.let { user ->
                    val newPoints = (user.totalPoints + pointsDiff).coerceAtLeast(0)
                    val updatedUser = user.copy(totalPoints = newPoints, level = (newPoints/500)+1)
                    userRepository.updateUser(updatedUser)
                    syncToFirebase(uid, updatedUser)
                }
                onSuccess()
            } catch (e: Exception) { Log.e("ACTIVITIES", "Error updating", e) }
        }
    }

    fun deleteActivity(activity: HealthActivity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val uid = getCurrentFirebaseUid() ?: return@launch
                val email = auth.currentUser?.email ?: return@launch
                activityRepository.deleteActivity(activity)
                
                val snapshot = db.child("activities").child(uid)
                    .orderByChild("timestamp")
                    .equalTo(activity.timestamp.toDouble())
                    .get().await()
                snapshot.children.forEach { it.ref.removeValue() }

                userRepository.getUserByEmail(email).firstOrNull()?.let { user ->
                    val newTotalPoints = (user.totalPoints - activity.points).coerceAtLeast(0)
                    val updatedUser = user.copy(totalPoints = newTotalPoints, level = (newTotalPoints / 500) + 1)
                    userRepository.updateUser(updatedUser)
                    syncToFirebase(uid, updatedUser)
                }
                onSuccess()
            } catch (e: Exception) { Log.e("ACTIVITIES", "Error deleting", e) }
        }
    }

    private suspend fun updateStreakLogic(localUserId: Int, uid: String) {
        val user = userRepository.getUserById(localUserId).firstOrNull() ?: return
        val countToday = activityRepository.getActivityCountToday(uid)
        if (countToday > 0 && user.currentStreak == 0) {
            userRepository.updateStreak(localUserId, 1)
        }
    }

    private fun syncToFirebase(uid: String, user: com.pulseup.app.data.local.entity.User) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            firebaseRepo.syncUserToLeaderboard(uid, user.username, user.totalPoints, user.level, user.currentStreak)
            db.child("users").child(uid).updateChildren(mapOf(
                "totalPoints" to user.totalPoints,
                "level" to user.level,
                "currentStreak" to user.currentStreak
            ))
        }
    }
}
