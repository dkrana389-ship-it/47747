package com.keepfocus.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DailyStatEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statsDao(): StatsDao
}
