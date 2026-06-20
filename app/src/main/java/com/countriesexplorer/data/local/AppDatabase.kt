package com.countriesexplorer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FavoriteEntity::class,
        CachedCountryEntity::class,
        CacheMetadataEntity::class,
        VisitHistoryEntity::class,
        CountryNoteEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(CountryTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun countryCacheDao(): CountryCacheDao
    abstract fun cacheMetadataDao(): CacheMetadataDao
    abstract fun visitHistoryDao(): VisitHistoryDao
    abstract fun countryNoteDao(): CountryNoteDao
}
