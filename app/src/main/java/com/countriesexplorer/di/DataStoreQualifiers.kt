package com.countriesexplorer.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ListPreferencesDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppSettingsDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ProfilePreferencesDataStore
