package com.pulseup.app.data.local.dao

import androidx.room.*
import com.pulseup.app.data.local.entity.Badge
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: Badge): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<Badge>)

    // READ
    @Query("SELECT * FROM badges WHERE id = :badgeId")
    suspend fun getBadgeById(badgeId: Int): Badge?

    @Query("SELECT * FROM badges")
    fun getAllBadges(): Flow<List<Badge>>

    @Query("SELECT * FROM badges WHERE category = :category")
    fun getBadgesByCategory(category: String): Flow<List<Badge>>

    @Query("SELECT * FROM badges ORDER BY rarity DESC")
    fun getBadgesSortedByRarity(): Flow<List<Badge>>

    // UPDATE
    @Update
    suspend fun updateBadge(badge: Badge)

    // DELETE
    @Delete
    suspend fun deleteBadge(badge: Badge)

    @Query("DELETE FROM badges")
    suspend fun deleteAllBadges()

    // STATISTICS
    @Query("SELECT COUNT(*) FROM badges")
    suspend fun getBadgeCount(): Int
}