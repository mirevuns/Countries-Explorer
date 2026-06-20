package com.countriesexplorer.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.countriesexplorer.data.repository.CountriesRepository
import com.countriesexplorer.data.repository.SyncResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CountryCacheSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val countriesRepository: CountriesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return when (countriesRepository.syncCacheIfNeeded(force = false)) {
            SyncResult.Success -> Result.success()
            is SyncResult.Skipped -> Result.success()
            is SyncResult.Failed -> Result.retry()
        }
    }
}
