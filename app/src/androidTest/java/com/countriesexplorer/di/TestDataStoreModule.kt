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

import dagger.hilt.android.qualifiers.ApplicationContext

import dagger.hilt.components.SingletonComponent

import dagger.hilt.testing.TestInstallIn

import java.util.concurrent.atomic.AtomicInteger

import javax.inject.Singleton



@Module

@TestInstallIn(

    components = [SingletonComponent::class],

    replaces = [DataStoreModule::class]

)

object TestDataStoreModule {



    private val listStoreId = AtomicInteger(0)

    private val appSettingsStoreId = AtomicInteger(0)



    @Provides

    @Singleton

    @ListPreferencesDataStore

    fun provideListPreferencesDataStore(

        @ApplicationContext context: Context

    ): DataStore<Preferences> {

        val id = listStoreId.incrementAndGet()

        return PreferenceDataStoreFactory.create(

            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },

            produceFile = {

                context.preferencesDataStoreFile("test_list_preferences_$id")

            }

        )

    }



    @Provides

    @Singleton

    @AppSettingsDataStore

    fun provideAppSettingsDataStore(

        @ApplicationContext context: Context

    ): DataStore<Preferences> {

        val id = appSettingsStoreId.incrementAndGet()

        return PreferenceDataStoreFactory.create(

            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },

            produceFile = {

                context.preferencesDataStoreFile("test_app_settings_$id")

            }

        )

    }

}

