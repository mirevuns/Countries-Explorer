package com.countriesexplorer

import com.countriesexplorer.data.preferences.ListPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface InstrumentedTestEntryPoint {
    fun listPreferencesRepository(): ListPreferencesRepository
}
