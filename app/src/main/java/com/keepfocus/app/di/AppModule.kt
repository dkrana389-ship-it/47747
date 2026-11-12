package com.keepfocus.app.di

import android.content.Context
import androidx.room.Room
import com.keepfocus.app.data.local.AppDatabase
import com.keepfocus.app.data.local.StatsDao
import com.keepfocus.app.domain.FreeVersionManager
import com.keepfocus.app.domain.PremiumManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "keep_focus_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideStatsDao(db: AppDatabase): StatsDao = db.statsDao()

    @Provides
    @Singleton
    fun providePremiumManager(): PremiumManager = FreeVersionManager()
}
