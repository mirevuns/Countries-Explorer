package com.countriesexplorer

import com.countriesexplorer.data.repository.ProfileRepository
import com.countriesexplorer.data.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class CountriesApplication : BaseCountriesApplication() {

    @Inject lateinit var syncScheduler: SyncScheduler
    @Inject lateinit var profileRepository: ProfileRepository

    override fun onCreate() {
        super.onCreate()
        runBlocking { profileRepository.ensureDefaultProfile() }
        syncScheduler.schedulePeriodicSync()
    }
}
