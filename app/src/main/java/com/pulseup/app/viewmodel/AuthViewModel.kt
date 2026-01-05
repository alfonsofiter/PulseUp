package com.pulseup.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.User
import com.pulseup.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val database = PulseUpDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val auth = Firebase.auth
    private val db = Firebase.database.reference

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val snapshot = db.child("users").child(currentUser.uid).get().await()
                    val remoteUser = snapshot.getValue(User::class.java)
                    
                    val finalUsername = remoteUser?.username ?: currentUser.displayName ?: "User"
                    
                    syncUserToLocal(finalUsername, email, remoteUser) {
                        _authState.value = AuthState.Success
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val newUser = User(username = username, email = email)
                    db.child("users").child(currentUser.uid).setValue(newUser).await()
                    
                    syncUserToLocal(username, email, newUser) {
                        _authState.value = AuthState.Success
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    private fun syncUserToLocal(username: String, email: String, remoteUser: User?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            database.userDao().deleteAllUsers()
            
            val userToSave = User(
                username = remoteUser?.username ?: username,
                email = email,
                height = remoteUser?.height ?: 0f,
                weight = remoteUser?.weight ?: 0f,
                age = remoteUser?.age ?: 0,
                totalPoints = remoteUser?.totalPoints ?: 0,
                level = remoteUser?.level ?: 1,
                currentStreak = remoteUser?.currentStreak ?: 0,
                // PERBAIKAN: Masukkan data Longest Streak dari Firebase
                longestStreak = remoteUser?.longestStreak ?: 0,
                profilePictureUrl = remoteUser?.profilePictureUrl ?: ""
            )
            userRepository.insertUser(userToSave)
            onSuccess()
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            database.userDao().deleteAllUsers()
            _authState.value = AuthState.Idle
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
