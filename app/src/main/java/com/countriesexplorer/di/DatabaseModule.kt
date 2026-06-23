package com.countriesexplorer.di

import android.content.Context
import androidx.room.Room
import com.countriesexplorer.BuildConfig
import com.countriesexplorer.data.local.AppDatabase
import com.countriesexplorer.data.local.FavoriteDao
import com.countriesexplorer.data.local.MIGRATION_2_3
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val builder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "countries_db"
        ).addMigrations(MIGRATION_2_3)

        if (BuildConfig.DEBUG) {
            builder
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favoriteDao()
    }
}
