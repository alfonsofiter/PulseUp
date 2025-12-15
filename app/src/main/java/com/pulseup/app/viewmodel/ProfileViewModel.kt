package com.pulseup.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.Achievement
import com.pulseup.app.data.local.entity.Badge
import com.pulseup.app.data.local.entity.User
import com.pulseup.app.data.repository.AchievementRepository
import com.pulseup.app.data.repository.BadgeRepository
import com.pulseup.app.data.repository.HealthActivityRepository
import com.pulseup.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val user: User? = null,
    val totalActivities: Int = 0,
    val totalCalories: Int = 0,
    val badges: List<Badge> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val unlockedBadgeCount: Int = 0,
    val isLoading: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val activityRepository = HealthActivityRepository(database.healthActivityDao())
    private val badgeRepository = BadgeRepository(database.badgeDao())
    private val achievementRepository = AchievementRepository(database.achievementDao())

    private val currentUserId = 1

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadProfileData()
    }

    // Load profile data
    fun loadProfileData() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)

            // Load user
            userRepository.getUserById(currentUserId).collect { user ->
                user?.let {
                    // Load statistics
                    val totalActivities = activityRepository.getTotalActivityCount(currentUserId)
                    val totalCalories = activityRepository.getTotalCaloriesBurned(currentUserId)
                    val unlockedCount = achievementRepository.getUniqueBadgeCount(currentUserId)

                    // Load badges
                    badgeRepository.getAllBadges().collect { badgeList ->
                        // Load achievements
                        achievementRepository.getAchievementsByUser(currentUserId).collect { achievementList ->
                            _profileState.value = ProfileState(
                                user = it,
                                totalActivities = totalActivities,
                                totalCalories = totalCalories,
                                badges = badgeList,
                                achievements = achievementList,
                                unlockedBadgeCount = unlockedCount,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    // Update user profile
    fun updateUserProfile(
        username: String,
        age: Int,
        weight: Float,
        height: Float,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUserOnce()
            currentUser?.let { user ->
                val updatedUser = user.copy(
                    username = username,
                    age = age,
                    weight = weight,
                    height = height
                )
                userRepository.updateUser(updatedUser)
                onSuccess()
            }
        }
    }

    // Generate AI tips based on user activity
    fun generateAITips(): List<String> {
        val state = _profileState.value
        val tips = mutableListOf<String>()

        state.user?.let { user ->
            // Tip based on activity count
            if (state.totalActivities < 10) {
                tips.add("💡 You're just getting started! Try to log at least 3 activities per day.")
            } else if (state.totalActivities < 50) {
                tips.add("💡 Great progress! Keep building your healthy habits.")
            } else {
                tips.add("💡 Amazing! You're a health champion! Keep up the excellent work.")
            }

            // Tip based on streak
            if (user.currentStreak >= 7) {
                tips.add("🔥 Incredible 7-day streak! You're on fire!")
            } else if (user.currentStreak > 0) {
                tips.add("🔥 You're on a ${user.currentStreak}-day streak. Keep it going!")
            } else {
                tips.add("🎯 Start a new streak today! Consistency is key.")
            }

            // Tip based on BMI
            val bmi = user.calculateBMI()
            val bmiCategory = user.getBMICategory()
            when (bmiCategory) {
                "Normal" -> tips.add("✅ Your BMI is in the healthy range. Keep maintaining!")
                "Underweight" -> tips.add("⚠️ Consider consulting a nutritionist to gain healthy weight.")
                "Overweight" -> tips.add("💪 Focus on regular exercise and balanced nutrition.")
                "Obese" -> tips.add("🏃 Start with light exercises and track your progress daily.")
            }
        }

        return tips
    }

    // Refresh profile
    fun refresh() {
        loadProfileData()
    }
}