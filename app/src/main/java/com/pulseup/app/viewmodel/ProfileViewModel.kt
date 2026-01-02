package com.pulseup.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.Achievement
import com.pulseup.app.data.local.entity.Badge
import com.pulseup.app.data.local.entity.User
import com.pulseup.app.data.repository.AchievementRepository
import com.pulseup.app.data.repository.BadgeRepository
import com.pulseup.app.data.repository.FirebaseLeaderboardRepository
import com.pulseup.app.data.repository.HealthActivityRepository
import com.pulseup.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class ProfileState(
    val user: User? = null,
    val firebaseEmail: String? = null,
    val totalActivities: Int = 0,
    val totalCalories: Int = 0,
    val badges: List<Badge> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val unlockedBadgeCount: Int = 0,
    val isLoading: Boolean = false,
    val activityCountToday: Int = 0
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val activityRepository = HealthActivityRepository(database.healthActivityDao())
    private val badgeRepository = BadgeRepository(database.badgeDao())
    private val achievementRepository = AchievementRepository(database.achievementDao())
    private val firebaseRepo = FirebaseLeaderboardRepository()

    private val auth = Firebase.auth

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            val email = auth.currentUser?.email
            if (email != null) {
                userRepository.getUserByEmail(email).collect { localUser ->
                    if (localUser != null) {
                        fetchUserStats(localUser, email)
                    }
                }
            }
        }
    }

    private suspend fun fetchUserStats(user: User, email: String) {
        val userId = user.id
        val totalActivities = activityRepository.getTotalActivityCount(userId)
        val totalCalories = activityRepository.getTotalCaloriesBurned(userId)
        val activityToday = activityRepository.getActivityCountToday(userId)
        val unlockedCount = achievementRepository.getUniqueBadgeCount(userId)

        badgeRepository.getAllBadges().collect { badgeList ->
            achievementRepository.getAchievementsByUser(userId).collect { achievementList ->
                _profileState.value = ProfileState(
                    user = user,
                    firebaseEmail = email,
                    totalActivities = totalActivities,
                    totalCalories = totalCalories,
                    badges = badgeList,
                    achievements = achievementList,
                    unlockedBadgeCount = unlockedCount,
                    activityCountToday = activityToday,
                    isLoading = false
                )
            }
        }
    }

    fun generateAITips(): List<AITip> {
        val state = _profileState.value
        val tips = mutableListOf<AITip>()

        state.user?.let { user ->
            val bmi = user.calculateBMI()
            when {
                bmi < 18.5 -> tips.add(AITip("💡 BMI Tip", "Your BMI is low. Focus on protein-rich nutrition.", "nutrition"))
                bmi > 25.0 -> tips.add(AITip("💪 Weight Control", "Focus on cardio exercises.", "exercise"))
                else -> tips.add(AITip("✅ Healthy BMI", "Great job! Your weight is ideal.", "success"))
            }

            if (state.activityCountToday == 0) {
                tips.add(AITip("🚀 Get Moving", "You haven't logged any activity today.", "warning"))
            }

            if (user.currentStreak > 0) {
                tips.add(AITip("🔥 Streak", "You are on a ${user.currentStreak}-day streak!", "streak"))
            }
        }

        return if (tips.isEmpty()) listOf(AITip("👋 Welcome", "Start logging activities!", "info")) else tips
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val email = auth.currentUser?.email
                if (email != null) {
                    val user = userRepository.getUserByEmail(email).firstOrNull()
                    if (user != null) {
                        firebaseRepo.removeFromLeaderboard(user.id)
                    }
                }
                auth.signOut()
            } catch (e: Exception) {
                auth.signOut()
            }
        }
    }

    fun updateFullProfile(username: String, phone: String, dob: Long, photoUrl: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val email = auth.currentUser?.email ?: return@launch
            val currentUser = userRepository.getUserByEmail(email).firstOrNull() ?: return@launch
            val updatedUser = currentUser.copy(username = username, phoneNumber = phone, dateOfBirth = dob, profilePictureUrl = photoUrl)
            userRepository.updateUser(updatedUser)
            firebaseRepo.syncUserToLeaderboard(updatedUser.id, updatedUser.username, updatedUser.totalPoints, updatedUser.level, updatedUser.currentStreak)
            _profileState.value = _profileState.value.copy(user = updatedUser)
            onSuccess()
        }
    }

    fun refresh() { loadProfileData() }
}

data class AITip(val title: String, val message: String, val type: String)
