package com.pulseup.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pulseup.app.data.local.dao.*
import com.pulseup.app.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        HealthActivity::class,
        Badge::class,
        Achievement::class
    ],
    version = 2, // Migrasi ke versi 2 karena ada field baru di User
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PulseUpDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun healthActivityDao(): HealthActivityDao
    abstract fun badgeDao(): BadgeDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: PulseUpDatabase? = null

        fun getDatabase(context: Context): PulseUpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PulseUpDatabase::class.java,
                    "pulseup_database"
                )
                    .fallbackToDestructiveMigration() // Cara tercepat untuk development agar tidak foreclose
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.badgeDao().insertBadges(PredefinedBadges.badges)
                    }
                }
            }
        }
    }
}