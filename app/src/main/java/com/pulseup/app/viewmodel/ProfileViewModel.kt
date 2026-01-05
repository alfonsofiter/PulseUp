package com.pulseup.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.User
import com.pulseup.app.data.repository.HealthActivityRepository
import com.pulseup.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Class ChatMessage dan GenerativeModel DIHAPUS dari sini karena sudah pindah ke ChatbotViewModel

data class Badge(val id: String = "", val name: String = "", val icon: String = "")
data class Achievement(val badgeId: String = "")

data class ProfileState(
    val user: User? = null,
    val firebaseEmail: String? = null,
    val totalActivities: Int = 0,
    val badges: List<Badge> = emptyList(),
    val achievements: List<Achievement> = emptyList()
    // aiTip dan chatHistory DIHAPUS karena tidak dipakai di screen Profile lagi
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val activityRepository = HealthActivityRepository(database.healthActivityDao())

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState = _profileState.asStateFlow()

    private val auth = Firebase.auth
    private val db = Firebase.database.reference

    // API KEY & GenerativeModel DIHAPUS (Pindah ke ChatbotViewModel)

    init {
        observeRoomUserData()
    }

    private fun observeRoomUserData() {
        viewModelScope.launch {
            val email = auth.currentUser?.email
            if (email != null) {
                userRepository.getUserByEmail(email).collect { user ->
                    _profileState.update { it.copy(user = user, firebaseEmail = email) }
                    refreshActivityStats()
                }
            }
        }
        refreshOtherData()
    }

    fun refresh() {
        refreshActivityStats()
        refreshOtherData()
    }

    private fun refreshActivityStats() {
        val firebaseUid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val totalCount = activityRepository.getTotalActivityCount(firebaseUid)
            _profileState.update { it.copy(totalActivities = totalCount) }
        }
    }

    private fun refreshOtherData() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val badgesSnapshot = db.child("badges").get().await()
                val badges = badgesSnapshot.children.mapNotNull { it.getValue(Badge::class.java) }

                val achievementsSnapshot = db.child("achievements").child(currentUser.uid).get().await()
                val achievements = achievementsSnapshot.children.mapNotNull { it.getValue(Achievement::class.java) }

                _profileState.update {
                    it.copy(
                        badges = badges,
                        achievements = achievements
                    )
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Fungsi sendChatMessage DIHAPUS (Pindah ke ChatbotViewModel)

    fun updateFullProfile(username: String, phone: String, dob: Long, photoUrl: String, height: Float? = null, weight: Float? = null, age: Int? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = _profileState.value.user
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        username = username, phoneNumber = phone, dateOfBirth = dob, profilePictureUrl = photoUrl,
                        height = height ?: currentUser.height, weight = weight ?: currentUser.weight, age = age ?: currentUser.age
                    )
                    userRepository.updateUser(updatedUser)

                    val userId = auth.currentUser?.uid ?: ""
                    val updates = mutableMapOf<String, Any>("username" to username, "phoneNumber" to phone, "dateOfBirth" to dob, "profilePictureUrl" to photoUrl)
                    height?.let { updates["height"] = it }
                    weight?.let { updates["weight"] = it }
                    age?.let { updates["age"] = it }

                    db.child("users").child(userId).updateChildren(updates).await()
                    onSuccess()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun logout() { auth.signOut() }
}