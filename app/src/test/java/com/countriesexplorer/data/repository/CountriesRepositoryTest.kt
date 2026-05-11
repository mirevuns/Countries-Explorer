package com.countriesexplorer.data.repository

import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.api.CountriesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CountriesRepositoryTest {

    private val server = MockWebServer()

    @Before
    fun setup() {
        server.start()
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
        return CountriesRepository(api)
    }

    @Test
    fun getCountryByCode_returnsParsedCountry() = runBlocking {
        server.enqueue(
            MockResponse()
                .setBody(TestFixtures.singleCountryJsonArray(commonName = "ApiLand", cca2 = "AP"))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
        )
        val repo = repository()
        val country = repo.getCountryByCode("AP")
        assertNotNull(country)
        assertEquals("ApiLand", country!!.displayName)
    }

    @Test
    fun getCountryByCode_apiError_returnsNull() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        val repo = repository()
        assertEquals(null, repo.getCountryByCode("XX"))
    }
}
