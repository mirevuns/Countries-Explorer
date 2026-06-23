package com.countriesexplorer.di

import android.content.Context
import androidx.room.Room
import com.countriesexplorer.data.local.AppDatabase
import com.countriesexplorer.data.local.CacheMetadataDao
import com.countriesexplorer.data.local.CountryCacheDao
import com.countriesexplorer.data.local.CollectionDao
import com.countriesexplorer.data.local.CountryNoteDao
import com.countriesexplorer.data.local.FavoriteDao
import com.countriesexplorer.data.local.JournalEntryDao
import com.countriesexplorer.data.local.ProfileDao
import com.countriesexplorer.data.local.VisitHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides @Singleton fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()
    @Provides @Singleton fun provideCountryCacheDao(database: AppDatabase): CountryCacheDao = database.countryCacheDao()
    @Provides @Singleton fun provideCacheMetadataDao(database: AppDatabase): CacheMetadataDao = database.cacheMetadataDao()
    @Provides @Singleton fun provideVisitHistoryDao(database: AppDatabase): VisitHistoryDao = database.visitHistoryDao()
    @Provides @Singleton fun provideCountryNoteDao(database: AppDatabase): CountryNoteDao = database.countryNoteDao()
    @Provides @Singleton fun provideProfileDao(database: AppDatabase): ProfileDao = database.profileDao()
    @Provides @Singleton fun provideJournalEntryDao(database: AppDatabase): JournalEntryDao = database.journalEntryDao()
    @Provides @Singleton fun provideCollectionDao(database: AppDatabase): CollectionDao = database.collectionDao()
}
