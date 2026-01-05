package com.pulseup.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_activities")
data class HealthActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String, // DIUBAH: Menjadi String (Firebase UID)
    val category: ActivityCategory,
    val activityName: String,
    val description: String = "",
    val points: Int,
    val caloriesBurned: Int = 0,
    val duration: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActivityCategory(val displayName: String, val icon: String, val color: Long) {
    EXERCISE("Olahraga", "🏃", 0xFFFF6B6B),
    HYDRATION("Hidrasi", "💧", 0xFF4ECDC4),
    NUTRITION("Nutrisi", "🍎", 0xFF95E1D3),
    SLEEP("Tidur", "😴", 0xFF9B59B6);

    companion object {
        fun fromString(value: String): ActivityCategory {
            return values().find { it.name == value } ?: EXERCISE
        }
    }
}

data class ActivityInput(
    val category: ActivityCategory,
    val activityName: String,
    val description: String,
    val duration: Int
) {
    fun calculatePoints(): Int {
        if (duration <= 0) return 0
        return when (category) {
            ActivityCategory.EXERCISE -> duration * 2
            ActivityCategory.HYDRATION -> 10
            ActivityCategory.NUTRITION -> 15
            ActivityCategory.SLEEP -> if (duration >= 420) 50 else 25 
        }
    }

    fun estimateCalories(): Int {
        if (duration <= 0) return 0
        return when (category) {
            ActivityCategory.EXERCISE -> duration * 5
            else -> 0
        }
    }
}
