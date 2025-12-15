package com.pulseup.app.data.local
import androidx.room.TypeConverter
import com.pulseup.app.data.local.entity.ActivityCategory
import com.pulseup.app.data.local.entity.BadgeRarity

class Converters {

    // ActivityCategory converters
    @TypeConverter
    fun fromActivityCategory(category: ActivityCategory): String {
        return category.name
    }

    @TypeConverter
    fun toActivityCategory(categoryString: String): ActivityCategory {
        return ActivityCategory.valueOf(categoryString)
    }

    // BadgeRarity converters
    @TypeConverter
    fun fromBadgeRarity(rarity: BadgeRarity): String {
        return rarity.name
    }

    @TypeConverter
    fun toBadgeRarity(rarityString: String): BadgeRarity {
        return BadgeRarity.valueOf(rarityString)
    }
}