package com.countriesexplorer.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val listPreferences: Flow<ListPreferences> = dataStore.data.map { prefs ->
        ListPreferences(
            showFavoritesOnly = prefs[KEY_SHOW_FAVORITES_ONLY] ?: false,
            sortByName = prefs[KEY_SORT_BY_NAME] ?: true
        )
    }

    suspend fun setShowFavoritesOnly(value: Boolean) {
        dataStore.edit { it[KEY_SHOW_FAVORITES_ONLY] = value }
    }

    suspend fun setSortByName(value: Boolean) {
        dataStore.edit { it[KEY_SORT_BY_NAME] = value }
    }

    companion object {
        private val KEY_SHOW_FAVORITES_ONLY = booleanPreferencesKey("show_favorites_only")
        private val KEY_SORT_BY_NAME = booleanPreferencesKey("sort_by_name")
    }
}
