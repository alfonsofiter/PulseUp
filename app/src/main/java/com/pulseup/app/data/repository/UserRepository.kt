package com.pulseup.app.data.repository

import com.pulseup.app.data.local.dao.UserDao
import com.pulseup.app.data.local.entity.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getCurrentUser(): Flow<User?> = userDao.getCurrentUser()
    suspend fun getCurrentUserOnce(): User? = userDao.getCurrentUserOnce()
    fun getUserById(userId: Int): Flow<User?> = userDao.getUserById(userId)
    fun getUserByEmail(email: String): Flow<User?> = userDao.getUserByEmail(email)
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    
    suspend fun updateUser(user: User) {
        // Sebelum update, pastikan rekor terlama tidak hilang
        val existing = userDao.getUserByIdOnce(user.id)
        val finalUser = if (existing != null) {
            val maxStreak = maxOf(user.currentStreak, existing.longestStreak, user.longestStreak)
            user.copy(longestStreak = maxStreak)
        } else {
            user.copy(longestStreak = maxOf(user.currentStreak, user.longestStreak))
        }
        userDao.updateUser(finalUser)
    }

    suspend fun addPoints(userId: Int, points: Int) {
        userDao.addPoints(userId, points)
        val user = userDao.getUserByIdOnce(userId)
        user?.let {
            val newLevel = (it.totalPoints / 500) + 1
            if (newLevel > it.level) {
                userDao.updateLevel(userId, newLevel)
            }
        }
    }

    suspend fun updateStreak(userId: Int, streak: Int) {
        userDao.updateStreak(userId, streak)
        val user = userDao.getUserByIdOnce(userId)
        user?.let {
            // Update rekor terlama jika streak saat ini lebih tinggi
            if (streak > it.longestStreak) {
                userDao.updateLongestStreak(userId, streak)
            }
        }
    }

    fun getAllUsersSortedByPoints(): Flow<List<User>> = userDao.getAllUsersSortedByPoints()
    suspend fun getUserCount(): Int = userDao.getUserCount()
}
