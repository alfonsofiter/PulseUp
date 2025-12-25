package com.pulseup.app.data.repository

import com.pulseup.app.data.local.dao.UserDao
import com.pulseup.app.data.local.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    // Get current user (Flow - auto update UI)
    fun getCurrentUser(): Flow<User?> = userDao.getCurrentUser()

    // Get current user (One-time)
    suspend fun getCurrentUserOnce(): User? = userDao.getCurrentUserOnce()

    // Get user by ID
    fun getUserById(userId: Int): Flow<User?> = userDao.getUserById(userId)

    // Get user by Email
    fun getUserByEmail(email: String): Flow<User?> = userDao.getUserByEmail(email)

    // Insert new user
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)

    // Update user info
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    // Add points to user
    suspend fun addPoints(userId: Int, points: Int) {
        userDao.addPoints(userId, points)

        // Auto level up logic
        val user = userDao.getUserByIdOnce(userId)
        user?.let {
            val newLevel = (it.totalPoints / 500) + 1
            if (newLevel > it.level) {
                userDao.updateLevel(userId, newLevel)
            }
        }
    }

    // Update streak
    suspend fun updateStreak(userId: Int, streak: Int) {
        userDao.updateStreak(userId, streak)

        // Update longest streak if current is higher
        val user = userDao.getUserByIdOnce(userId)
        user?.let {
            if (streak > it.longestStreak) {
                userDao.updateLongestStreak(userId, streak)
            }
        }
    }

    // Get all users sorted by points (for leaderboard)
    fun getAllUsersSortedByPoints(): Flow<List<User>> =
        userDao.getAllUsersSortedByPoints()

    // Get user count
    suspend fun getUserCount(): Int = userDao.getUserCount()
}