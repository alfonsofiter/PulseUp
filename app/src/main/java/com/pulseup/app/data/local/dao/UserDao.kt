package com.pulseup.app.data.local.dao

import androidx.room.*
import com.pulseup.app.data.local.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

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

    @Update
    suspend fun updateUser(user: User)

    // PERBAIKAN: Gunakan CASE untuk memastikan poin tidak pernah di bawah 0
    @Query("""
        UPDATE users 
        SET totalPoints = CASE 
            WHEN (totalPoints + :points) < 0 THEN 0 
            ELSE (totalPoints + :points) 
        END 
        WHERE id = :userId
    """)
    suspend fun addPoints(userId: Int, points: Int)

    @Query("UPDATE users SET level = :level WHERE id = :userId")
    suspend fun updateLevel(userId: Int, level: Int)

    @Query("UPDATE users SET currentStreak = :streak WHERE id = :userId")
    suspend fun updateStreak(userId: Int, streak: Int)

    @Query("UPDATE users SET longestStreak = :streak WHERE id = :userId")
    suspend fun updateLongestStreak(userId: Int, streak: Int)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
