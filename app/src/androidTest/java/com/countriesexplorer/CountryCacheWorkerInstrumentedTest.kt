package com.countriesexplorer

import androidx.hilt.work.HiltWorkerFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.countriesexplorer.data.sync.CountryCacheSyncWorker
import com.countriesexplorer.data.sync.CountryPreloadWorker
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CountryCacheWorkerInstrumentedTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun startMockServer() {
            InstrumentedTestSupport.clearDataStoreFiles()
            TestApiHolder.ensureStarted()
        }
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Before
    fun setup() {
        hiltRule.inject()
        InstrumentedTestSupport.ensureWorkManagerInitialized()
        TestApiHolder.resetAlphaErrorArm()
        InstrumentedTestSupport.resetPreferences()
    }

    private fun entryPoint(): InstrumentedTestEntryPoint = InstrumentedTestSupport.entryPoint()

    @Test
    fun preloadWorker_populatesCountryCache() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val worker = TestListenableWorkerBuilder<CountryPreloadWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue(entryPoint().countryCacheDao().count() > 0)
    }

    @Test
    fun syncWorker_populatesCacheWhenAutoRefreshEnabled() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val worker = TestListenableWorkerBuilder<CountryCacheSyncWorker>(context)
            .setWorkerFactory(workerFactory)
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue(entryPoint().countryCacheDao().count() > 0)
    }
}
