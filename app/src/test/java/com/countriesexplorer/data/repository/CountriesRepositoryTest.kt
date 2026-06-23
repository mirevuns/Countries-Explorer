package com.countriesexplorer.data.repository

import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.api.CountriesApi
import com.countriesexplorer.data.local.CachedCountryEntity
import com.countriesexplorer.data.local.CacheMetadataEntity
import com.countriesexplorer.data.preferences.AppSettings
import com.countriesexplorer.data.preferences.AppSettingsRepository
import com.countriesexplorer.data.repository.ProfileRepository.Companion.DEFAULT_PROFILE_ID
import com.countriesexplorer.testdoubles.FakeCacheMetadataDao
import com.countriesexplorer.testdoubles.FakeCountryCacheDao
import com.countriesexplorer.testdoubles.FakeJournalEntryDao
import com.countriesexplorer.testdoubles.FakeVisitHistoryDao
import com.countriesexplorer.util.NetworkMonitor
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class CountriesRepositoryTest {

    private val server = MockWebServer()
    private val countryCacheDao = FakeCountryCacheDao()
    private val cacheMetadataDao = FakeCacheMetadataDao()
    private val visitHistoryDao = FakeVisitHistoryDao()
    private val profileRepository = mockk<ProfileRepository>(relaxed = true) {
        coEvery { currentProfileId() } returns DEFAULT_PROFILE_ID
        every { activeProfileId } returns flowOf(DEFAULT_PROFILE_ID)
    }
    private val journalRepository = JournalRepository(FakeJournalEntryDao(), profileRepository)
    private val visitHistoryRepository = VisitHistoryRepository(
        visitHistoryDao,
        profileRepository,
        journalRepository
    )
    private val appSettingsRepository = mockk<AppSettingsRepository>()
    private val networkMonitor = mockk<NetworkMonitor>()

    @Before
    fun setup() {
        server.start()
        every { networkMonitor.isConnected } returns true
        every { networkMonitor.isOnWifi } returns true
        every { networkMonitor.shouldBlockSync(any()) } returns false
        coEvery { appSettingsRepository.currentSettings() } returns AppSettings()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun repository(): CountriesRepository {
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(CountriesApi::class.java)
        return CountriesRepository(
            api = api,
            countryCacheDao = countryCacheDao,
            cacheMetadataDao = cacheMetadataDao,
            appSettingsRepository = appSettingsRepository,
            networkMonitor = networkMonitor,
            visitHistoryRepository = visitHistoryRepository
        )
    }

    @Test
    fun getCountryByCode_returnsParsedCountry() = runBlocking {
        server.enqueue(
            MockResponse()
                .setBody(TestFixtures.singleCountryJsonArray(commonName = "ApiLand", cca2 = "AP"))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
        )
        val result = repository().getCountryByCode("AP")
        assertEquals("ApiLand", result.country.displayName)
        assertEquals(1, visitHistoryRepository.getRecentVisits().first().size)
    }

    @Test
    fun getCountryByCode_emptyResponse_throwsNotFound() = runBlocking {
        server.enqueue(
            MockResponse()
                .setBody(TestFixtures.v5EmptyResponseBody())
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
        )
        try {
            repository().getCountryByCode("XX")
            fail("Expected CountryNotFoundException")
        } catch (e: CountryNotFoundException) {
            assertEquals("XX", e.code)
        }
    }

    @Test
    fun getAllCountries_mergesRegionResponsesAndPersistsCache() = runBlocking {
        repeat(5) {
            server.enqueue(
                MockResponse()
                    .setBody(TestFixtures.singleCountryJsonArray(commonName = "ApiLand", cca2 = "AP"))
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
            )
        }
        val result = repository().getAllCountries()
        assertEquals(1, result.countries.size)
        assertEquals("ApiLand", result.countries.first().displayName)
        assertEquals(1, countryCacheDao.count())
        assertEquals(CacheMetadataEntity.STATUS_SUCCESS, cacheMetadataDao.get()?.lastSyncStatus)
    }

    @Test
    fun getAllCountries_offline_returnsCachedData() = runBlocking {
        val cached = CachedCountryEntity.fromCountry(TestFixtures.country("Cached", "CA"))
        countryCacheDao.insertAll(listOf(cached))
        cacheMetadataDao.upsert(
            CacheMetadataEntity(lastFullSyncAt = System.currentTimeMillis())
        )
        every { networkMonitor.isConnected } returns false

        val result = repository().getAllCountries()
        assertEquals(1, result.countries.size)
        assertTrue(result.isOffline)
        assertFalse(result.isStale)
    }

    @Test
    fun getAllCountries_regionFailure_withCache_returnsStaleCache() = runBlocking {
        val cached = CachedCountryEntity.fromCountry(TestFixtures.country("Cached", "CA"))
        countryCacheDao.insertAll(listOf(cached))
        repeat(5) {
            server.enqueue(MockResponse().setResponseCode(500))
        }
        val result = repository().getAllCountries(forceRefresh = true)
        assertEquals(1, result.countries.size)
        assertTrue(result.isStale)
    }

    @Test
    fun searchCountries_filtersLocalCacheWithoutNetwork() = runBlocking {
        val cached = CachedCountryEntity.fromCountry(TestFixtures.country("Finland", "FI"))
        countryCacheDao.insertAll(listOf(cached))
        cacheMetadataDao.upsert(
            CacheMetadataEntity(lastFullSyncAt = System.currentTimeMillis())
        )
        every { networkMonitor.isConnected } returns false

        val result = repository().searchCountries("Fin")
        assertEquals(1, result.countries.size)
        assertFalse(result.countries.first().displayName.isBlank())
    }
}
