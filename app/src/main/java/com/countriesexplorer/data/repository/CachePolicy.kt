package com.countriesexplorer.data.repository

import com.countriesexplorer.data.preferences.AppSettings

object CachePolicy {
    fun isStale(lastSyncAt: Long?, ttlHours: Int, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (lastSyncAt == null) return true
        val ttlMillis = ttlHours.coerceAtLeast(AppSettings.MIN_CACHE_TTL_HOURS) * 60L * 60L * 1000L
        return nowMillis - lastSyncAt > ttlMillis
    }
}
