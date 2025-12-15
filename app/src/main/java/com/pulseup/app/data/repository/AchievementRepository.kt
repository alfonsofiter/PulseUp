package com.pulseup.app.data.repository

import com.pulseup.app.data.local.dao.AchievementDao
import com.pulseup.app.data.local.entity.Achievement
import kotlinx.coroutines.flow.Flow

class AchievementRepository(private val achievementDao: AchievementDao) {

    // Get achievements by user
    fun getAchievementsByUser(userId: Int): Flow<List<Achievement>> =
        achievementDao.getAchievementsByUser(userId)

    // Get recent achievements
    fun getRecentAchievements(userId: Int, limit: Int): Flow<List<Achievement>> =
        achievementDao.getRecentAchievements(userId, limit)

    // Check if badge is unlocked
    suspend fun checkIfBadgeUnlocked(userId: Int, badgeId: Int): Achievement? =
        achievementDao.checkIfBadgeUnlocked(userId, badgeId)

    // Insert achievement (unlock badge)
    suspend fun insertAchievement(achievement: Achievement): Long =
        achievementDao.insertAchievement(achievement)

    // Update achievement
    suspend fun updateAchievement(achievement: Achievement) =
        achievementDao.updateAchievement(achievement)

    // Delete achievement
    suspend fun deleteAchievement(achievement: Achievement) =
        achievementDao.deleteAchievement(achievement)

    // Get achievement count
    suspend fun getAchievementCount(userId: Int): Int =
        achievementDao.getAchievementCount(userId)

    // Get unique badge count (total badges unlocked)
    suspend fun getUniqueBadgeCount(userId: Int): Int =
        achievementDao.getUniqueBadgeCount(userId)
}