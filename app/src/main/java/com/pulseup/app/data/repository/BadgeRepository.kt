package com.pulseup.app.data.repository

import com.pulseup.app.data.local.dao.BadgeDao
import com.pulseup.app.data.local.entity.Badge
import kotlinx.coroutines.flow.Flow

class BadgeRepository(private val badgeDao: BadgeDao) {

    // Get all badges
    fun getAllBadges(): Flow<List<Badge>> = badgeDao.getAllBadges()

    // Get badges by category
    fun getBadgesByCategory(category: String): Flow<List<Badge>> =
        badgeDao.getBadgesByCategory(category)

    // Get badges sorted by rarity
    fun getBadgesSortedByRarity(): Flow<List<Badge>> =
        badgeDao.getBadgesSortedByRarity()

    // Get badge by ID
    suspend fun getBadgeById(badgeId: Int): Badge? =
        badgeDao.getBadgeById(badgeId)

    // Insert badge
    suspend fun insertBadge(badge: Badge): Long =
        badgeDao.insertBadge(badge)

    // Insert multiple badges
    suspend fun insertBadges(badges: List<Badge>) =
        badgeDao.insertBadges(badges)

    // Update badge
    suspend fun updateBadge(badge: Badge) =
        badgeDao.updateBadge(badge)

    // Delete badge
    suspend fun deleteBadge(badge: Badge) =
        badgeDao.deleteBadge(badge)

    // Get badge count
    suspend fun getBadgeCount(): Int =
        badgeDao.getBadgeCount()
}