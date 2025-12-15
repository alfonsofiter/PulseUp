package com.pulseup.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_activities")
data class HealthActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val category: ActivityCategory,
    val activityName: String,
    val description: String = "",
    val points: Int,
    val caloriesBurned: Int = 0,
    val duration: Int = 0, // dalam menit
    val timestamp: Long = System.currentTimeMillis()
)

// Enum untuk kategori aktivitas
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

// Data class untuk form input
data class ActivityInput(
    val category: ActivityCategory,
    val activityName: String,
    val description: String,
    val duration: Int
) {
    // Hitung poin berdasarkan kategori dan durasi
    fun calculatePoints(): Int {
        return when (category) {
            ActivityCategory.EXERCISE -> duration * 2 // 2 poin per menit
            ActivityCategory.HYDRATION -> 10 // flat 10 poin per gelas
            ActivityCategory.NUTRITION -> 15 // flat 15 poin per meal sehat
            ActivityCategory.SLEEP -> if (duration >= 420) 50 else 25 // 50 jika 7+ jam
        }
    }

    // Estimasi kalori terbakar
    fun estimateCalories(): Int {
        return when (category) {
            ActivityCategory.EXERCISE -> duration * 5 // rough estimate
            ActivityCategory.HYDRATION -> 0
            ActivityCategory.NUTRITION -> 0
            ActivityCategory.SLEEP -> 0
        }
    }
}