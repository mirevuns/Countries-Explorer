package com.countriesexplorer.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
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

    private val listStoreLock = Any()
    @Volatile
    private var listPreferencesStore: DataStore<Preferences>? = null

    private val appSettingsStoreLock = Any()
    @Volatile
    private var appSettingsStore: DataStore<Preferences>? = null

    private val profileStoreLock = Any()
    @Volatile
    private var profileStore: DataStore<Preferences>? = null

    @Provides
    @Singleton
    @ListPreferencesDataStore
    fun provideListPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        listPreferencesStore?.let { return it }
        return synchronized(listStoreLock) {
            listPreferencesStore ?: PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = { context.preferencesDataStoreFile("list_preferences") }
            ).also { listPreferencesStore = it }
        }
    }

    @Provides
    @Singleton
    @AppSettingsDataStore
    fun provideAppSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        appSettingsStore?.let { return it }
        return synchronized(appSettingsStoreLock) {
            appSettingsStore ?: PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = { context.preferencesDataStoreFile("app_settings") }
            ).also { appSettingsStore = it }
        }
    }

    @Provides
    @Singleton
    @ProfilePreferencesDataStore
    fun provideProfilePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        profileStore?.let { return it }
        return synchronized(profileStoreLock) {
            profileStore ?: PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = { context.preferencesDataStoreFile("profile_preferences") }
            ).also { profileStore = it }
        }
    }
}
