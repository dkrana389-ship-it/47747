package com.keepfocus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {
    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getStatForDate(date: String): DailyStatEntity?

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun observeStatForDate(date: String): Flow<DailyStatEntity?>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 7")
    fun observeRecentStats(): Flow<List<DailyStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stat: DailyStatEntity)
}
