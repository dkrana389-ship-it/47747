package com.keepfocus.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatEntity(
    @PrimaryKey val date: String,
    val reelsWatched: Int = 0,
    val popupsShown: Int = 0,
    val closeAppClicked: Int = 0
)
