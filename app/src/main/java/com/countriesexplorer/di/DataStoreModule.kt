package com.countriesexplorer.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private val storeLock = Any()
    @Volatile
    private var listPreferencesStore: DataStore<Preferences>? = null

    @Provides
    @Singleton
    fun provideListPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        listPreferencesStore?.let { return it }
        return synchronized(storeLock) {
            listPreferencesStore ?: PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler {
                    emptyPreferences()
                },
                produceFile = { context.preferencesDataStoreFile("list_preferences") }
            ).also { listPreferencesStore = it }
        }
    }
}
