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
import kotlinx.coroutines.withContext

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

    init {
        loadActivities()
    }

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
            } else {
                _isLoading.value = false
            }
        }
    }

    fun loadActivitiesByCategory(category: ActivityCategory?) {
        activityJob?.cancel()
        activityJob = viewModelScope.launch {
            _isLoading.value = true
            val uid = getCurrentFirebaseUid()
            if (uid != null) {
                val flow = if (category == null) {
                    activityRepository.getActivitiesByUser(uid)
                } else {
                    activityRepository.getActivitiesByCategory(uid, category)
                }
                flow.collect {
                    _activities.value = it
                    _isLoading.value = false
                }
            }
        }
    }

    suspend fun getActivityById(activityId: Int): HealthActivity? {
        return activityRepository.getActivityById(activityId)
    }

    private suspend fun recalculateAndSyncUserPoints(uid: String, email: String) {
        withContext(NonCancellable) {
            val trueTotalPoints = activityRepository.getTotalPoints(uid)
            val user = userRepository.getUserByEmail(email).firstOrNull() ?: return@withContext

            val newLevel = (trueTotalPoints / 500) + 1
            val updatedUser = user.copy(
                totalPoints = trueTotalPoints,
                level = newLevel
            )

            userRepository.updateUser(updatedUser)
            updateStreakLogic(user.id, uid)
            syncToFirebase(uid, updatedUser)
        }
    }

    // --- PERBAIKAN FITUR EDIT (UPDATE) ---
    fun updateActivity(activityId: Int, input: ActivityInput, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val uid = getCurrentFirebaseUid() ?: return@launch
                val email = auth.currentUser?.email ?: return@launch
                val oldActivity = activityRepository.getActivityById(activityId) ?: return@launch

                val updatedActivity = oldActivity.copy(
                    category = input.category,
                    activityName = input.activityName,
                    description = input.description,
                    points = input.calculatePoints(),
                    caloriesBurned = input.estimateCalories(),
                    duration = input.duration
                    // Timestamp TETAP SAMA agar bisa dicari di Firebase
                )

                // 1. Update di Local DB
                activityRepository.updateActivity(updatedActivity)

                // 2. Update di Firebase (Metode PENCARIAN MANUAL yang Lebih Akurat)
                withContext(Dispatchers.IO + NonCancellable) {
                    try {
                        // Ambil semua activity user ini dulu
                        val snapshot = db.child("activities").child(uid).get().await()

                        var isFound = false
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                // Cek manual satu per satu: Apakah timestamp-nya sama?
                                val ts = child.child("timestamp").getValue(Long::class.java)
                                if (ts != null && ts == oldActivity.timestamp) {
                                    // KETEMU! Update datanya
                                    child.ref.setValue(updatedActivity).await()
                                    isFound = true
                                    Log.d("FIREBASE_UPDATE", "Activity updated: ${updatedActivity.activityName}")
                                    break
                                }
                            }
                        }

                        // Jika tidak ketemu (misal data hantu atau glitch), buat baru sebagai backup
                        if (!isFound) {
                            Log.w("FIREBASE_UPDATE", "Activity not found by timestamp, creating new entry.")
                            db.child("activities").child(uid).push().setValue(updatedActivity).await()
                        }

                    } catch (e: Exception) {
                        Log.e("FIREBASE_UPDATE", "Failed to update Firebase", e)
                    }

                    // 3. Sync Poin (Leaderboard & Profile)
                    recalculateAndSyncUserPoints(uid, email)
                }

                onSuccess()

            } catch (e: Exception) {
                Log.e("ACTIVITIES", "Error updating", e)
            }
        }
    }

    // --- PERBAIKAN FITUR DELETE ---
    fun deleteActivity(activity: HealthActivity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val uid = getCurrentFirebaseUid() ?: return@launch
                val email = auth.currentUser?.email ?: return@launch

                // 1. Hapus dari Local
                activityRepository.deleteActivity(activity)

                // 2. Hapus dari Firebase (Metode PENCARIAN MANUAL)
                withContext(Dispatchers.IO + NonCancellable) {
                    try {
                        val snapshot = db.child("activities").child(uid).get().await()

                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val ts = child.child("timestamp").getValue(Long::class.java)
                                // Cari yang timestamp-nya COCOK PERSIS
                                if (ts != null && ts == activity.timestamp) {
                                    child.ref.removeValue().await()
                                    Log.d("FIREBASE_DELETE", "Activity deleted successfully")
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FIREBASE_DELETE", "Failed to delete from Firebase", e)
                    }

                    // 3. Sync Poin
                    recalculateAndSyncUserPoints(uid, email)
                }

                _isLoading.value = false
                onSuccess()

            } catch (e: Exception) {
                Log.e("ACTIVITIES", "Error deleting activity", e)
                _isLoading.value = false
            }
        }
    }

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

                withContext(Dispatchers.IO + NonCancellable) {
                    try {
                        db.child("activities").child(uid).push().setValue(activity).await()
                    } catch (e: Exception) {
                        Log.e("ADD", "Gagal push ke Firebase", e)
                    }
                    recalculateAndSyncUserPoints(uid, email)
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("ACTIVITIES", "Error adding", e)
            }
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
            try {
                db.child("users").child(uid).updateChildren(mapOf(
                    "totalPoints" to user.totalPoints,
                    "level" to user.level,
                    "currentStreak" to user.currentStreak
                ))
            } catch (e: Exception) {
                Log.e("FIREBASE", "Gagal sync user details", e)
            }
        }
    }
}