package com.countriesexplorer.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.countriesexplorer.data.preferences.AppSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSettingsRepository: AppSettingsRepository
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodicSync() {
        val settings = runBlocking { appSettingsRepository.currentSettings() }
        if (!settings.autoRefreshEnabled) {
            workManager.cancelUniqueWork(PERIODIC_SYNC_WORK)
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (settings.wifiOnlySync) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .build()

        val request = PeriodicWorkRequestBuilder<CountryCacheSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun enqueuePreload() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<CountryPreloadWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(request)
    }

    companion object {
        const val PERIODIC_SYNC_WORK = "country_cache_periodic_sync"
    }
}
