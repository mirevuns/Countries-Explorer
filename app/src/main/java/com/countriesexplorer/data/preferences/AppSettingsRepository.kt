package com.countriesexplorer.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.countriesexplorer.di.AppSettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsRepository @Inject constructor(
    @AppSettingsDataStore private val dataStore: DataStore<Preferences>
) {
    val appSettings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            cacheTtlHours = prefs[KEY_CACHE_TTL_HOURS] ?: AppSettings.DEFAULT_CACHE_TTL_HOURS,
            autoRefreshEnabled = prefs[KEY_AUTO_REFRESH] ?: true,
            wifiOnlySync = prefs[KEY_WIFI_ONLY_SYNC] ?: false,
            preloadOnWifi = prefs[KEY_PRELOAD_ON_WIFI] ?: true
        )
    }

    suspend fun currentSettings(): AppSettings = appSettings.first()

    suspend fun setCacheTtlHours(hours: Int) {
        val normalized = hours.coerceIn(
            AppSettings.MIN_CACHE_TTL_HOURS,
            AppSettings.MAX_CACHE_TTL_HOURS
        )
        dataStore.edit { it[KEY_CACHE_TTL_HOURS] = normalized }
    }

    suspend fun setAutoRefreshEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_REFRESH] = enabled }
    }

    suspend fun setWifiOnlySync(enabled: Boolean) {
        dataStore.edit { it[KEY_WIFI_ONLY_SYNC] = enabled }
    }

    suspend fun setPreloadOnWifi(enabled: Boolean) {
        dataStore.edit { it[KEY_PRELOAD_ON_WIFI] = enabled }
    }

    companion object {
        private val KEY_CACHE_TTL_HOURS = intPreferencesKey("cache_ttl_hours")
        private val KEY_AUTO_REFRESH = booleanPreferencesKey("auto_refresh_enabled")
        private val KEY_WIFI_ONLY_SYNC = booleanPreferencesKey("wifi_only_sync")
        private val KEY_PRELOAD_ON_WIFI = booleanPreferencesKey("preload_on_wifi")
    }
}
