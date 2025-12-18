package com.pulseup.app.data.repository

import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Data class untuk user di leaderboard cloud
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
        Log.d("FIREBASE", "🔥 Firebase Repository initialized")
        Log.d("FIREBASE", "🔥 Database URL: ${database.reference}")

        // Monitor connection status
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d("FIREBASE", "✅ Connected to Firebase!")
                } else {
                    Log.e("FIREBASE", "❌ Disconnected from Firebase!")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "❌ Connection listener error: ${error.message}")
            }
        })
    }

    // Sync user data ke Firebase Cloud
    suspend fun syncUserToLeaderboard(
        userId: Int,
        username: String,
        totalPoints: Int,
        level: Int,
        currentStreak: Int
    ) {
        try {
            Log.d("FIREBASE", "🔥 SYNC START: User=$username, Points=$totalPoints")
            Log.d("FIREBASE", "🔥 Database reference: ${leaderboardRef.toString()}")
            Log.d("FIREBASE", "🔥 Saving to path: leaderboard/$userId")

            val userMap = mapOf(
                "userId" to userId.toString(),
                "username" to username,
                "totalPoints" to totalPoints,
                "level" to level,
                "currentStreak" to currentStreak,
                "lastUpdate" to ServerValue.TIMESTAMP
            )

            Log.d("FIREBASE", "🔥 Data prepared: $userMap")
            Log.d("FIREBASE", "🔥 Calling setValue().await()...")

            // Simpan ke Firebase dengan key userId
            leaderboardRef.child(userId.toString()).setValue(userMap).await()

            Log.d("FIREBASE", "✅ SYNC SUCCESS!")
        } catch (e: com.google.firebase.database.DatabaseException) {
            Log.e("FIREBASE", "❌ DATABASE EXCEPTION: ${e.message}", e)
            Log.e("FIREBASE", "❌ Exception details: ${e.javaClass.name}")
            Log.e("FIREBASE", "❌ Stack trace:", e)
            throw e
        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.e("FIREBASE", "❌ COROUTINE CANCELLED: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e("FIREBASE", "❌ SYNC FAILED: ${e.javaClass.simpleName} - ${e.message}", e)
            Log.e("FIREBASE", "❌ Full exception:", e)
            throw e
        }
    }

    // Ambil leaderboard real-time dari Firebase
    fun getLeaderboard(): Flow<List<LeaderboardUser>> = callbackFlow {
        Log.d("FIREBASE", "🔥 Starting leaderboard listener...")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FIREBASE", "📥 Data received: ${snapshot.childrenCount} users")

                val users = mutableListOf<LeaderboardUser>()

                // Parse setiap user dari Firebase
                snapshot.children.forEach { child ->
                    try {
                        val userId = child.child("userId").getValue(String::class.java) ?: ""
                        val username = child.child("username").getValue(String::class.java) ?: ""
                        val totalPoints = child.child("totalPoints").getValue(Int::class.java) ?: 0
                        val level = child.child("level").getValue(Int::class.java) ?: 1
                        val currentStreak = child.child("currentStreak").getValue(Int::class.java) ?: 0
                        val lastUpdate = child.child("lastUpdate").getValue(Long::class.java) ?: 0L

                        val user = LeaderboardUser(
                            userId = userId,
                            username = username,
                            totalPoints = totalPoints,
                            level = level,
                            currentStreak = currentStreak,
                            lastUpdate = lastUpdate
                        )

                        users.add(user)
                        Log.d("FIREBASE", "✅ Parsed: $username - $totalPoints pts")
                    } catch (e: Exception) {
                        Log.e("FIREBASE", "❌ Parse error for ${child.key}: ${e.message}")
                    }
                }

                // Sort descending (poin tertinggi di atas)
                val sortedUsers = users.sortedByDescending { it.totalPoints }
                Log.d("FIREBASE", "🏆 Top user: ${sortedUsers.firstOrNull()?.username} - ${sortedUsers.firstOrNull()?.totalPoints} pts")

                // Kirim data ke Flow
                trySend(sortedUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE", "❌ Database error: ${error.message}")
                close(error.toException())
            }
        }

        leaderboardRef.addValueEventListener(listener)

        awaitClose {
            Log.d("FIREBASE", "🔥 Closing leaderboard listener")
            leaderboardRef.removeEventListener(listener)
        }
    }

    // Get user rank (posisi di leaderboard)
    suspend fun getUserRank(userId: Int): Int {
        return try {
            val snapshot = leaderboardRef.get().await()

            val users = mutableListOf<LeaderboardUser>()
            snapshot.children.forEach { child ->
                try {
                    val userIdStr = child.child("userId").getValue(String::class.java) ?: ""
                    val username = child.child("username").getValue(String::class.java) ?: ""
                    val totalPoints = child.child("totalPoints").getValue(Int::class.java) ?: 0
                    val level = child.child("level").getValue(Int::class.java) ?: 1
                    val currentStreak = child.child("currentStreak").getValue(Int::class.java) ?: 0
                    val lastUpdate = child.child("lastUpdate").getValue(Long::class.java) ?: 0L

                    val user = LeaderboardUser(
                        userId = userIdStr,
                        username = username,
                        totalPoints = totalPoints,
                        level = level,
                        currentStreak = currentStreak,
                        lastUpdate = lastUpdate
                    )
                    users.add(user)
                } catch (e: Exception) {
                    Log.e("FIREBASE", "Parse error: ${e.message}")
                }
            }

            val sortedUsers = users.sortedByDescending { it.totalPoints }
            val rank = sortedUsers.indexOfFirst { it.userId == userId.toString() } + 1

            if (rank <= 0) 0 else rank
        } catch (e: Exception) {
            Log.e("FIREBASE", "Get rank error: ${e.message}")
            0
        }
    }
}