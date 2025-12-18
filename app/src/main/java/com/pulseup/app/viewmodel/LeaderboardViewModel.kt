package com.pulseup.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulseup.app.data.repository.FirebaseLeaderboardRepository
import com.pulseup.app.data.repository.LeaderboardUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepo = FirebaseLeaderboardRepository()
    private val currentUserId = 1

    private val _users = MutableStateFlow<List<LeaderboardUser>>(emptyList())
    val users: StateFlow<List<LeaderboardUser>> = _users.asStateFlow()

    private val _currentUserRank = MutableStateFlow(0)
    val currentUserRank: StateFlow<Int> = _currentUserRank.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLeaderboard()
    }

    // Load leaderboard from Firebase (real-time!)
    fun loadLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                firebaseRepo.getLeaderboard().collect { userList ->
                    _users.value = userList

                    // Find current user rank
                    val rank = userList.indexOfFirst { it.userId == currentUserId.toString() } + 1
                    _currentUserRank.value = if (rank <= 0) 0 else rank

                    _isLoading.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    // Refresh leaderboard
    fun refresh() {
        loadLeaderboard()
    }
}