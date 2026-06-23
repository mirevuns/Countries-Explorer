package com.countriesexplorer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.countriesexplorer.data.local.CacheMetadataEntity
import com.countriesexplorer.data.local.CachedCountryEntity
import com.countriesexplorer.data.local.ProfileEntity
import com.countriesexplorer.data.repository.ProfileRepository
import com.countriesexplorer.ui.navigation.NavGraph
import com.countriesexplorer.ui.theme.CountriesExplorerTheme
import com.countriesexplorer.ui.viewmodel.FavoritesSharedViewModel
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.EntryPointAccessors
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

typealias HiltComposeTestRule =
    AndroidComposeTestRule<ActivityScenarioRule<HiltTestActivity>, HiltTestActivity>

object InstrumentedTestSupport {

    fun entryPoint(): InstrumentedTestEntryPoint {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        return EntryPointAccessors.fromApplication(app, InstrumentedTestEntryPoint::class.java)
    }

    fun ensureWorkManagerInitialized() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        try {
            WorkManager.getInstance(context)
            return
        } catch (_: IllegalStateException) {
        }
        val factory = entryPoint().hiltWorkerFactory()
        WorkManager.initialize(
            context,
            Configuration.Builder()
                .setWorkerFactory(factory)
                .build()
        )
    }

    fun clearDataStoreFiles() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.filesDir.resolve("datastore").takeIf(File::exists)?.deleteRecursively()
    }

    fun resetPreferences() = runBlocking(Dispatchers.IO) {
        val entryPoint = entryPoint()
        entryPoint.listPreferencesRepository().setSortByName(true)
        entryPoint.appSettingsRepository().setWifiOnlySync(false)
        entryPoint.appSettingsRepository().setPreloadOnWifi(false)
        entryPoint.appSettingsRepository().setAutoRefreshEnabled(true)
    }

    fun seedTestlandCache() = runBlocking(Dispatchers.IO) {
        val entryPoint = entryPoint()
        val now = System.currentTimeMillis()
        val country = AndroidTestFixtures.testland()
        entryPoint.countryCacheDao().insertAll(
            listOf(CachedCountryEntity.fromCountry(country, now))
        )
        entryPoint.cacheMetadataDao().upsert(
            CacheMetadataEntity(
                lastFullSyncAt = now,
                lastSyncStatus = CacheMetadataEntity.STATUS_SUCCESS
            )
        )
    }

    fun clearVisitHistory() = runBlocking(Dispatchers.IO) {
        entryPoint().visitHistoryDao().clearForProfile(ProfileRepository.DEFAULT_PROFILE_ID)
    }

    fun ensureDefaultProfile() = runBlocking(Dispatchers.IO) {
        val entryPoint = entryPoint()
        val profileDao = entryPoint.profileDao()
        if (profileDao.count() == 0) {
            profileDao.insert(
                ProfileEntity(
                    name = ProfileRepository.DEFAULT_PROFILE_NAME,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        entryPoint.profileRepository().ensureDefaultProfile()
    }

    fun launchNavGraph(
        composeRule: HiltComposeTestRule,
        seedCache: Boolean = true,
        clearHistory: Boolean = false
    ) {
        ensureWorkManagerInitialized()
        ensureDefaultProfile()
        if (seedCache) {
            seedTestlandCache()
        }
        if (clearHistory) {
            clearVisitHistory()
        }
        composeRule.setContent {
            CountriesExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val favoritesViewModel: FavoritesSharedViewModel =
                        androidx.hilt.navigation.compose.hiltViewModel()
                    val favoritesSet by favoritesViewModel.favorites.collectAsStateWithLifecycle()

                    NavGraph(
                        navController = androidx.navigation.compose.rememberNavController(),
                        favoritesViewModel = favoritesViewModel,
                        favoritesSet = favoritesSet,
                        onFavoriteToggle = { code, country ->
                            favoritesViewModel.toggleFavorite(code, country)
                        }
                    )
                }
            }
        }
    }
}
