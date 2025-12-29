package com.pulseup.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.User
import com.pulseup.app.data.repository.FirebaseLeaderboardRepository
import com.pulseup.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = Firebase.auth

    // Add Room Database & Repositories
    private val database = PulseUpDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val firebaseRepo = FirebaseLeaderboardRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser get() = auth.currentUser

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Firebase Auth Login
                auth.signInWithEmailAndPassword(email, password).await()

                // Sync user to Room Database
                syncUserToRoom(email)

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login failed")
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Firebase Auth Sign Up
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                // Update display name
                result.user?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                )?.await()

                // Create user in Room Database
                createUserInRoom(name, email)

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    private suspend fun syncUserToRoom(email: String) {
        // Check if user exists in Room
        val existingUser = userRepository.getUserByEmail(email).firstOrNull()

        if (existingUser == null) {
            // Create new user in Room
            val firebaseUser = auth.currentUser
            val username = firebaseUser?.displayName ?: email.substringBefore("@")

            createUserInRoom(username, email)
        } else {
            // User exists, sync to Firebase Leaderboard
            syncToFirebaseLeaderboard(existingUser)
        }
    }

    private suspend fun createUserInRoom(username: String, email: String) {
        val newUser = User(
            username = username,
            email = email,
            age = 0,
            weight = 0f,
            height = 0f,
            phoneNumber = "",
            dateOfBirth = 0L,
            totalPoints = 0,
            level = 1,
            currentStreak = 0,
            longestStreak = 0
        )

        val userId = userRepository.insertUser(newUser)

        // Sync to Firebase Leaderboard
        firebaseRepo.syncUserToLeaderboard(
            userId = userId.toInt(),
            username = username,
            totalPoints = 0,
            level = 1,
            currentStreak = 0
        )
    }

    private suspend fun syncToFirebaseLeaderboard(user: User) {
        firebaseRepo.syncUserToLeaderboard(
            userId = user.id,
            username = user.username,
            totalPoints = user.totalPoints,
            level = user.level,
            currentStreak = user.currentStreak
        )
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}