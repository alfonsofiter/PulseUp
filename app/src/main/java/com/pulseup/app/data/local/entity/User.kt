package com.pulseup.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val age: Int,
    val weight: Float, // dalam kg
    val height: Float, // dalam cm
    val phoneNumber: String = "",
    val dateOfBirth: Long = 0L,
    val profilePictureUrl: String = "",
    val totalPoints: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Hitung BMI
    fun calculateBMI(): Float {
        if (height == 0f) return 0f
        val heightInMeters = height / 100
        return weight / (heightInMeters * heightInMeters)
    }

    // Status BMI
    fun getBMICategory(): String {
        val bmi = calculateBMI()
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    // Progress ke level berikutnya
    fun getProgressToNextLevel(): Float {
        val pointsForNextLevel = level * 500
        val pointsInCurrentLevel = totalPoints % 500
        return (pointsInCurrentLevel.toFloat() / 500f)
    }
}