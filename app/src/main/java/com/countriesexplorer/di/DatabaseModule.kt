package com.countriesexplorer.di

import android.content.Context
import androidx.room.Room
import com.countriesexplorer.data.local.AppDatabase
import com.countriesexplorer.data.local.CacheMetadataDao
import com.countriesexplorer.data.local.CountryCacheDao
import com.countriesexplorer.data.local.CountryNoteDao
import com.countriesexplorer.data.local.FavoriteDao
import com.countriesexplorer.data.local.MIGRATION_2_3
import com.countriesexplorer.data.local.MIGRATION_3_4
import com.countriesexplorer.data.local.VisitHistoryDao
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "countries_db"
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .build()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    @Singleton
    fun provideCountryCacheDao(database: AppDatabase): CountryCacheDao = database.countryCacheDao()

    @Provides
    @Singleton
    fun provideCacheMetadataDao(database: AppDatabase): CacheMetadataDao = database.cacheMetadataDao()

    @Provides
    @Singleton
    fun provideVisitHistoryDao(database: AppDatabase): VisitHistoryDao = database.visitHistoryDao()

    @Provides
    @Singleton
    fun provideCountryNoteDao(database: AppDatabase): CountryNoteDao = database.countryNoteDao()
}
