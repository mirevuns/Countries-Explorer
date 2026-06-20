package com.countriesexplorer

import com.countriesexplorer.data.local.CacheMetadataEntity
import com.countriesexplorer.data.local.CachedCountryEntity
import com.countriesexplorer.data.local.CacheMetadataDao
import com.countriesexplorer.data.local.CountryCacheDao
import com.countriesexplorer.data.local.CountryNoteDao
import com.countriesexplorer.data.local.VisitHistoryDao
import androidx.hilt.work.HiltWorkerFactory
import com.countriesexplorer.data.preferences.AppSettingsRepository
import com.countriesexplorer.data.preferences.ListPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface InstrumentedTestEntryPoint {
    fun listPreferencesRepository(): ListPreferencesRepository
    fun appSettingsRepository(): AppSettingsRepository
    fun countryCacheDao(): CountryCacheDao
    fun cacheMetadataDao(): CacheMetadataDao
    fun countryNoteDao(): CountryNoteDao
    fun visitHistoryDao(): VisitHistoryDao
    fun hiltWorkerFactory(): HiltWorkerFactory
}
