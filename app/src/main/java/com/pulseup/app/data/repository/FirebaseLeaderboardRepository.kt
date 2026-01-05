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

    suspend fun syncUserToLeaderboard(
        userId: String, // PERBAIKAN: Gunakan String (Firebase UID)
        username: String,
        totalPoints: Int,
        level: Int,
        currentStreak: Int
    ) {
        try {
            val userMap = mapOf(
                "userId" to userId,
                "username" to username,
                "totalPoints" to totalPoints,
                "level" to level,
                "currentStreak" to currentStreak,
                "lastUpdate" to ServerValue.TIMESTAMP
            )
            // Gunakan UID sebagai key agar tidak menimpa user lain
            leaderboardRef.child(userId).setValue(userMap).await()
            Log.d("FIREBASE", "✅ SYNC SUCCESS for $username")
        } catch (e: Exception) {
            Log.e("FIREBASE", "❌ SYNC FAILED: ${e.message}")
        }
    }

    fun getLeaderboard(): Flow<List<LeaderboardUser>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<LeaderboardUser>()
                snapshot.children.forEach { child ->
                    try {
                        val user = child.getValue(LeaderboardUser::class.java)
                        if (user != null) users.add(user)
                    } catch (e: Exception) {
                        Log.e("FIREBASE", "❌ Parse error: ${e.message}")
                    }
                }
                // Urutkan berdasarkan poin terbanyak
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
