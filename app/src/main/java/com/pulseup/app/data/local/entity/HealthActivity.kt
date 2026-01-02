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
        if (duration <= 0) return 0 // Jangan beri poin jika durasi/jumlah belum diisi
        
        return when (category) {
            ActivityCategory.EXERCISE -> duration * 2
            ActivityCategory.HYDRATION -> 10 // flat per entry
            ActivityCategory.NUTRITION -> 15 // flat per meal
            ActivityCategory.SLEEP -> if (duration >= 420) 50 else 25 
        }
    }

    // Estimasi kalori terbakar
    fun estimateCalories(): Int {
        if (duration <= 0) return 0
        return when (category) {
            ActivityCategory.EXERCISE -> duration * 5
            else -> 0
        }
    }
}