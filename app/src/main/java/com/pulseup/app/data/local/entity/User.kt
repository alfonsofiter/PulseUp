package com.pulseup.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String = "", // Tambahkan default ""
    val email: String = "",    // Tambahkan default ""
    val age: Int = 0,          // Tambahkan default 0
    val weight: Float = 0f,    // Tambahkan default 0f
    val height: Float = 0f,    // Tambahkan default 0f
    val phoneNumber: String = "",
    val dateOfBirth: Long = 0L,
    val profilePictureUrl: String = "",
    val totalPoints: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun calculateBMI(): Float {
        if (height == 0f) return 0f
        val heightInMeters = height / 100
        return weight / (heightInMeters * heightInMeters)
    }

    fun getBMICategory(): String {
        val bmi = calculateBMI()
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    fun getProgressToNextLevel(): Float {
        if (totalPoints <= 0) return 0f
        val pointsInCurrentLevel = totalPoints % 500
        val progress = pointsInCurrentLevel.toFloat() / 500f
        return progress.coerceIn(0f, 1f)
    }
}
