package com.countriesexplorer.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataStoreModule::class]
)
object TestDataStoreModule {

    private val storeLock = Any()
    @Volatile
    private var listPreferencesStore: DataStore<Preferences>? = null

    @Provides
    @Singleton
    fun provideListPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        listPreferencesStore?.let { return it }
        return synchronized(storeLock) {
            listPreferencesStore ?: PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = {
                    context.preferencesDataStoreFile("test_list_preferences")
                }
            ).also { listPreferencesStore = it }
        }
    }
}
