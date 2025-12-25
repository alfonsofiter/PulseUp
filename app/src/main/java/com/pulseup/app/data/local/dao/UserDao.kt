package com.pulseup.app.data.local.dao

import androidx.room.*
import com.pulseup.app.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    // READ
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdOnce(userId: Int): User?

    @Query("SELECT * FROM users ORDER BY totalPoints DESC")
    fun getAllUsersSortedByPoints(): Flow<List<User>>

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<User?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUserOnce(): User?

    // UPDATE
    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET totalPoints = totalPoints + :points WHERE id = :userId")
    suspend fun addPoints(userId: Int, points: Int)

    @Query("UPDATE users SET level = :level WHERE id = :userId")
    suspend fun updateLevel(userId: Int, level: Int)

    @Query("UPDATE users SET currentStreak = :streak WHERE id = :userId")
    suspend fun updateStreak(userId: Int, streak: Int)

    @Query("UPDATE users SET longestStreak = :streak WHERE id = :userId")
    suspend fun updateLongestStreak(userId: Int, streak: Int)

    // DELETE
    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    // STATISTICS
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT SUM(totalPoints) FROM users")
    suspend fun getTotalPointsAllUsers(): Int
}