package com.pulseup.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.local.entity.User
import com.pulseup.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())

    private val currentUserId = 1

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUserRank = MutableStateFlow(0)
    val currentUserRank: StateFlow<Int> = _currentUserRank.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLeaderboard()
    }

    // Load leaderboard data
    fun loadLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true

            userRepository.getAllUsersSortedByPoints().collect { userList ->
                _users.value = userList

                // Find current user rank
                val rank = userList.indexOfFirst { it.id == currentUserId } + 1
                _currentUserRank.value = rank

                _isLoading.value = false
            }
        }
    }

    // Refresh leaderboard
    fun refresh() {
        loadLeaderboard()
    }
}