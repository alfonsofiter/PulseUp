package com.pulseup.app.data.repository

import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class LeaderboardUser(
    val userId: String = "",
    val username: String = "",
    val totalPoints: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val lastUpdate: Long = 0
)

class FirebaseLeaderboardRepository {

    private val database = FirebaseDatabase.getInstance()
    private val leaderboardRef = database.getReference("leaderboard")

    init {
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) Log.d("FIREBASE", "✅ Connected to Firebase!")
                else Log.e("FIREBASE", "❌ Disconnected from Firebase!")
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    suspend fun syncUserToLeaderboard(
        userId: Int,
        username: String,
        totalPoints: Int,
        level: Int,
        currentStreak: Int
    ) {
        try {
            val userMap = mapOf(
                "userId" to userId.toString(),
                "username" to username,
                "totalPoints" to totalPoints,
                "level" to level,
                "currentStreak" to currentStreak,
                "lastUpdate" to ServerValue.TIMESTAMP
            )
            leaderboardRef.child(userId.toString()).setValue(userMap).await()
            Log.d("FIREBASE", "✅ SYNC SUCCESS for $username")
        } catch (e: Exception) {
            Log.e("FIREBASE", "❌ SYNC FAILED: ${e.message}")
        }
    }

    // Fungsi baru untuk menghapus user dari leaderboard
    suspend fun removeFromLeaderboard(userId: Int) {
        try {
            leaderboardRef.child(userId.toString()).removeValue().await()
            Log.d("FIREBASE", "✅ User $userId removed from leaderboard")
        } catch (e: Exception) {
            Log.e("FIREBASE", "❌ Failed to remove user $userId: ${e.message}")
        }
    }

    fun getLeaderboard(): Flow<List<LeaderboardUser>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<LeaderboardUser>()
                snapshot.children.forEach { child ->
                    try {
                        val userId = child.child("userId").getValue(String::class.java) ?: ""
                        val username = child.child("username").getValue(String::class.java) ?: ""
                        val totalPoints = child.child("totalPoints").getValue(Int::class.java) ?: 0
                        val level = child.child("level").getValue(Int::class.java) ?: 1
                        val currentStreak = child.child("currentStreak").getValue(Int::class.java) ?: 0
                        val lastUpdate = child.child("lastUpdate").getValue(Long::class.java) ?: 0L

                        users.add(LeaderboardUser(userId, username, totalPoints, level, currentStreak, lastUpdate))
                    } catch (e: Exception) {
                        Log.e("FIREBASE", "❌ Parse error: ${e.message}")
                    }
                }
                trySend(users.sortedByDescending { it.totalPoints })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        leaderboardRef.addValueEventListener(listener)
        awaitClose { leaderboardRef.removeEventListener(listener) }
    }
}