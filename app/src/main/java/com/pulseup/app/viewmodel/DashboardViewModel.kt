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
import com.pulseup.app.data.repository.FirebaseLeaderboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

// State ini SUDAH COCOK 100% dengan DashboardScreen.kt Anda
data class DashboardState(
    val user: User? = null,           // Untuk menampilkan Streak, Level, & Total Poin
    val healthScore: Int = 0,         // Untuk Card Health Score besar
    val activitiesToday: Int = 0,     // Untuk StatCard "Activities Today"
    val pointsToday: Int = 0,         // Untuk StatCard "Points Earned" (Hari ini)
    val caloriesBurned: Int = 0,      // Untuk StatCard "Calories Burned"
    val isLoading: Boolean = false
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val activityRepository = HealthActivityRepository(database.healthActivityDao())
    private val firebaseRepo = FirebaseLeaderboardRepository()
    private val auth = Firebase.auth

    private val _dashboardState = MutableStateFlow(DashboardState(isLoading = true))
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private var observationJob: Job? = null

    init {
        observeUserData()
    }

    fun observeUserData() {
        observationJob?.cancel()

        observationJob = viewModelScope.launch {
            val email = auth.currentUser?.email
            val firebaseUid = auth.currentUser?.uid

            if (email != null && firebaseUid != null) {
                // SINKRONISASI REAL-TIME: Menggabungkan data User & Activity
                // Setiap kali ada perubahan di Room (tambah/hapus activity), UI langsung update otomatis
                combine(
                    userRepository.getUserByEmail(email),
                    activityRepository.getActivitiesByUser(firebaseUid)
                ) { user, activities ->
                    if (user != null) {
                        // Filter aktivitas HANYA hari ini (mulai jam 00:00)
                        val startOfDay = getStartOfDay()
                        val todaysActivities = activities.filter { it.timestamp >= startOfDay }

                        val activitiesCountToday = todaysActivities.size
                        val pointsToday = todaysActivities.sumOf { it.points }
                        val totalCalories = todaysActivities.sumOf { it.caloriesBurned } // Kalori hari ini saja

                        // Hitung Health Score agar dinamis
                        val healthScore = calculateHealthScore(
                            activitiesToday = activitiesCountToday,
                            streak = user.currentStreak,
                            level = user.level
                        )

                        DashboardState(
                            user = user, // Object user lengkap dikirim ke UI
                            healthScore = healthScore,
                            activitiesToday = activitiesCountToday,
                            pointsToday = pointsToday,
                            caloriesBurned = totalCalories,
                            isLoading = false
                        )
                    } else {
                        DashboardState(isLoading = true)
                    }
                }.collect { state ->
                    _dashboardState.value = state
                    // Sinkronisasi ke Firebase Leaderboard (Background Process)
                    // Menggunakan NonCancellable agar sync tetap jalan meski user tutup aplikasi
                    state.user?.let { syncToFirebase(firebaseUid, it) }
                }
            } else {
                _dashboardState.value = DashboardState(isLoading = false)
            }
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun syncToFirebase(uid: String, user: User) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            firebaseRepo.syncUserToLeaderboard(uid, user.username, user.totalPoints, user.level, user.currentStreak)
        }
    }

    private fun calculateHealthScore(activitiesToday: Int, streak: Int, level: Int): Int {
        val baseScore = 50
        val activityScore = minOf(activitiesToday * 10, 30)
        val streakScore = minOf(streak * 2, 20)
        val levelBonus = minOf(level, 10)
        return minOf(baseScore + activityScore + streakScore + levelBonus, 100)
    }

    fun refresh() { observeUserData() }
}