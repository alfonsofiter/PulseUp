package com.pulseup.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val icon: String, // emoji atau icon name
    val requirement: Int, // jumlah aktivitas yang dibutuhkan
    val category: String, // kategori badge (EXERCISE, HYDRATION, dll)
    val rarity: BadgeRarity = BadgeRarity.COMMON
)

enum class BadgeRarity(val displayName: String, val color: Long) {
    COMMON("Common", 0xFF95A5A6),
    RARE("Rare", 0xFF3498DB),
    EPIC("Epic", 0xFF9B59B6),
    LEGENDARY("Legendary", 0xFFF39C12)
}

// Predefined badges untuk di-insert ke database
object PredefinedBadges {
    val badges = listOf(
        Badge(
            name = "Marathon Runner",
            description = "Selesaikan 10 aktivitas olahraga",
            icon = "🏃",
            requirement = 10,
            category = "EXERCISE",
            rarity = BadgeRarity.RARE
        ),
        Badge(
            name = "Hydration Hero",
            description = "Streak 7 hari minum air",
            icon = "💧",
            requirement = 7,
            category = "HYDRATION",
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            name = "Nutrition Master",
            description = "Catat 20 meal sehat",
            icon = "🥗",
            requirement = 20,
            category = "NUTRITION",
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            name = "Sleep Champion",
            description = "30 hari tidur cukup",
            icon = "😴",
            requirement = 30,
            category = "SLEEP",
            rarity = BadgeRarity.LEGENDARY
        ),
        Badge(
            name = "Week Warrior",
            description = "Streak 7 hari semua aktivitas",
            icon = "🔥",
            requirement = 7,
            category = "ALL",
            rarity = BadgeRarity.EPIC
        ),
        Badge(
            name = "First Step",
            description = "Selesaikan aktivitas pertama kamu",
            icon = "👣",
            requirement = 1,
            category = "ALL",
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            name = "Century",
            description = "Kumpulkan 100 aktivitas",
            icon = "💯",
            requirement = 100,
            category = "ALL",
            rarity = BadgeRarity.LEGENDARY
        ),
        Badge(
            name = "Point Master",
            description = "Kumpulkan 5000 poin",
            icon = "⭐",
            requirement = 5000,
            category = "POINTS",
            rarity = BadgeRarity.EPIC
        )
    )
}