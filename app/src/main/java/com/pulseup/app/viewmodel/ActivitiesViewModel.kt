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

    /**
     * Fungsi Helper untuk menghitung ulang total poin User dari database lokal
     * dan melakukan sinkronisasi ke User Table & Firebase Leaderboard.
     * Ini menjamin konsistensi data antara Home, Profile, dan Leaderboard.
     */
    private suspend fun recalculateAndSyncUserPoints(uid: String, email: String) {
        // 1. Ambil total poin yang benar-benar akurat dari tabel Activity (SUM)
        val trueTotalPoints = activityRepository.getTotalPoints(uid)

        // 2. Ambil data user saat ini
        val user = userRepository.getUserByEmail(email).firstOrNull() ?: return

        // 3. Update objek User dengan poin baru & level baru
        val newLevel = (trueTotalPoints / 500) + 1
        val updatedUser = user.copy(
            totalPoints = trueTotalPoints,
            level = newLevel
        )

        // 4. Simpan ke database Lokal (Room) -> Ini akan memperbaiki Profile Screen
        userRepository.updateUser(updatedUser)

        // 5. Update Streak Logic (jika diperlukan)
        updateStreakLogic(user.id, uid)

        // 6. Sinkronisasi ke Firebase Leaderboard -> Ini akan memperbaiki Leaderboard Screen
        syncToFirebase(uid, updatedUser)
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

                // 1. Insert ke Local DB (Room)
                activityRepository.insertActivity(activity)

                // 2. Push ke Firebase Activity (Fire-and-forget atau safe call)
                try {
                    db.child("activities").child(uid).push().setValue(activity)
                } catch (e: Exception) {
                    Log.e("ACTIVITIES", "Gagal sync activity ke Firebase", e)
                }

                // 3. Hitung ulang total poin User & Sync
                recalculateAndSyncUserPoints(uid, email)

                // 4. Panggil onSuccess agar UI tidak loading terus
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

                val updatedActivity = oldActivity.copy(
                    category = input.category,
                    activityName = input.activityName,
                    description = input.description,
                    points = input.calculatePoints(),
                    caloriesBurned = input.estimateCalories(),
                    duration = input.duration
                )

                // 1. Update LOCAL Activity (Room)
                activityRepository.updateActivity(updatedActivity)

                // 2. Hitung ulang total poin User & Sync (Fix Bug Profile/Leaderboard)
                recalculateAndSyncUserPoints(uid, email)

                // 3. Update Firebase Activity
                // Dibungkus try-catch agar jika gagal/lama tidak menghalangi onSuccess()
                try {
                    val snapshot = db.child("activities").child(uid)
                        .orderByChild("timestamp")
                        .equalTo(oldActivity.timestamp.toDouble())
                        .get().await()

                    if (snapshot.exists()) {
                        snapshot.children.forEach { it.ref.setValue(updatedActivity) }
                    } else {
                        Log.w("ACTIVITIES", "Activity tidak ditemukan di Firebase untuk diupdate")
                    }
                } catch (e: Exception) {
                    Log.e("ACTIVITIES", "Gagal update activity di Firebase (koneksi/timeout)", e)
                }

                // 4. Segera kembalikan UI (Fix Bug Loading Terus)
                onSuccess()

            } catch (e: Exception) {
                Log.e("ACTIVITIES", "Error updating", e)
                // Tetap panggil onSuccess jika error lokal sudah tertangani,
                // atau biarkan user mencoba lagi. Di sini kita biarkan log saja.
            }
        }
    }

    fun deleteActivity(activity: HealthActivity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val uid = getCurrentFirebaseUid() ?: return@launch
                val email = auth.currentUser?.email ?: return@launch

                // 1. Hapus dari LOCAL Activity (Room)
                activityRepository.deleteActivity(activity)

                // 2. Hitung ulang total poin User & Sync (Fix Bug Profile/Leaderboard)
                recalculateAndSyncUserPoints(uid, email)

                // 3. Hapus dari Firebase Activity
                try {
                    val snapshot = db.child("activities").child(uid)
                        .orderByChild("timestamp")
                        .equalTo(activity.timestamp.toDouble())
                        .get().await()
                    snapshot.children.forEach { it.ref.removeValue() }
                } catch (e: Exception) {
                    Log.e("ACTIVITIES", "Gagal hapus activity di Firebase", e)
                }

                // 4. Segera kembalikan UI
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
        // Gunakan NonCancellable agar proses sync tetap berjalan meskipun user sudah keluar dari layar (Back)
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