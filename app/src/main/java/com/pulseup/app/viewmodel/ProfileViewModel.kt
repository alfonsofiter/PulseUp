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
    val isLoading: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val activityRepository = HealthActivityRepository(database.healthActivityDao())
    private val badgeRepository = BadgeRepository(database.badgeDao())
    private val achievementRepository = AchievementRepository(database.achievementDao())
    private val firebaseRepo = FirebaseLeaderboardRepository() // ← TAMBAH INI

    private val auth = Firebase.auth

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadProfileData()
    }

    // Load profile data
    fun loadProfileData() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)

            val currentUser = auth.currentUser
            val email = currentUser?.email
            val name = currentUser?.displayName ?: "User"

            if (email != null) {
                userRepository.getUserByEmail(email).collect { localUser ->
                    if (localUser != null) {
                        fetchUserStats(localUser, email)
                    } else {
                        val placeholderUser = User(
                            username = name,
                            email = email,
                            age = 0,
                            weight = 0f,
                            height = 0f
                        )
                        _profileState.value = ProfileState(
                            user = placeholderUser,
                            firebaseEmail = email,
                            isLoading = false
                        )
                    }
                }
            } else {
                _profileState.value = _profileState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun fetchUserStats(user: User, email: String) {
        val userId = user.id
        val totalActivities = activityRepository.getTotalActivityCount(userId)
        val totalCalories = activityRepository.getTotalCaloriesBurned(userId)
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
                    isLoading = false
                )
            }
        }
    }

    fun updateFullProfile(
        username: String,
        phone: String,
        dob: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val email = auth.currentUser?.email
                if (email != null) {
                    val currentUser = userRepository.getUserByEmail(email).firstOrNull()
                    if (currentUser != null) {
                        val updatedUser = currentUser.copy(
                            username = username,
                            phoneNumber = phone,
                            dateOfBirth = dob
                        )
                        userRepository.updateUser(updatedUser)

                        Log.d("PROFILE", "✅ Profile updated in Room DB")

                        // Sync ke Firebase Leaderboard
                        firebaseRepo.syncUserToLeaderboard(
                            userId = updatedUser.id,
                            username = updatedUser.username,
                            totalPoints = updatedUser.totalPoints,
                            level = updatedUser.level,
                            currentStreak = updatedUser.currentStreak
                        )

                        Log.d("PROFILE", "✅ Profile synced to Firebase")

                        // Update state langsung (immediate UI update)
                        _profileState.value = _profileState.value.copy(
                            user = updatedUser
                        )

                    } else {
                        // Create new user if not exists
                        val newUser = User(
                            username = username,
                            email = email,
                            age = 0,
                            weight = 0f,
                            height = 0f,
                            phoneNumber = phone,
                            dateOfBirth = dob
                        )
                        val userId = userRepository.insertUser(newUser)

                        // Sync to Firebase
                        firebaseRepo.syncUserToLeaderboard(
                            userId = userId.toInt(),
                            username = username,
                            totalPoints = 0,
                            level = 1,
                            currentStreak = 0
                        )

                        // Update state langsung
                        _profileState.value = _profileState.value.copy(
                            user = newUser.copy(id = userId.toInt())
                        )
                    }

                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("PROFILE", "❌ Update profile failed: ${e.message}", e)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun generateAITips(): List<String> {
        val state = _profileState.value
        val tips = mutableListOf<String>()

        state.user?.let { user ->
            if (state.totalActivities < 10) {
                tips.add("💡 You're just getting started! Try to log at least 3 activities per day.")
            } else {
                tips.add("💡 Great progress! Keep building your healthy habits.")
            }

            if (user.currentStreak >= 7) {
                tips.add("🔥 Incredible 7-day streak! You're on fire!")
            } else if (user.currentStreak > 0) {
                tips.add("🔥 You're on a ${user.currentStreak}-day streak. Keep it going!")
            }

            val bmiCategory = user.getBMICategory()
            if (user.height > 0) {
                when (bmiCategory) {
                    "Normal" -> tips.add("✅ Your BMI is in the healthy range. Keep maintaining!")
                    "Underweight" -> tips.add("⚠️ Consider consulting a nutritionist to gain healthy weight.")
                    "Overweight" -> tips.add("💪 Focus on regular exercise and balanced nutrition.")
                }
            }
        }

        return tips
    }

    fun refresh() {
        loadProfileData()
    }
}