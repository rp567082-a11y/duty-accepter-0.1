package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DutyRuleEntity::class,
        DutyLogEntity::class,
        SubscriptionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DutyDatabase : RoomDatabase() {

    abstract fun dutyDao(): DutyDao

    companion object {
        @Volatile
        private var INSTANCE: DutyDatabase? = null

        fun getDatabase(context: Context): DutyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DutyDatabase::class.java,
                    "duty_accepter_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
