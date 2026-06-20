package com.countriesexplorer.data.preferences

data class AppSettings(
    val cacheTtlHours: Int = DEFAULT_CACHE_TTL_HOURS,
    val autoRefreshEnabled: Boolean = true,
    val wifiOnlySync: Boolean = false,
    val preloadOnWifi: Boolean = true
) {
    companion object {
        const val DEFAULT_CACHE_TTL_HOURS = 24
        const val MIN_CACHE_TTL_HOURS = 1
        const val MAX_CACHE_TTL_HOURS = 168
    }
}
