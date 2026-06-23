package com.countriesexplorer.di

import android.content.Context
import androidx.room.Room
import com.countriesexplorer.BuildConfig
import com.countriesexplorer.data.local.AppDatabase
import com.countriesexplorer.data.local.CacheMetadataDao
import com.countriesexplorer.data.local.CountryCacheDao
import com.countriesexplorer.data.local.CollectionDao
import com.countriesexplorer.data.local.CountryNoteDao
import com.countriesexplorer.data.local.FavoriteDao
import com.countriesexplorer.data.local.JournalEntryDao
import com.countriesexplorer.data.local.MIGRATION_2_3
import com.countriesexplorer.data.local.MIGRATION_3_4
import com.countriesexplorer.data.local.MIGRATION_4_5
import com.countriesexplorer.data.local.MIGRATION_5_6
import com.countriesexplorer.data.local.ProfileDao
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
        val builder = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "countries_db"
        ).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)

        if (BuildConfig.DEBUG) {
            builder
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDowngrade()
        }

        return builder.build()
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

    @Provides
    @Singleton
    fun provideProfileDao(database: AppDatabase): ProfileDao = database.profileDao()

    @Provides
    @Singleton
    fun provideJournalEntryDao(database: AppDatabase): JournalEntryDao = database.journalEntryDao()

    @Provides
    @Singleton
    fun provideCollectionDao(database: AppDatabase): CollectionDao = database.collectionDao()
}
