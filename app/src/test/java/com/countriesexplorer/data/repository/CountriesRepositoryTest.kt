package com.countriesexplorer.data.repository

import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.api.CountriesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

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
        assertEquals("ApiLand", country.displayName)
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
    fun getCountryByCode_apiError_throws() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        try {
            repository().getCountryByCode("XX")
            fail("Expected API error")
        } catch (_: Exception) {
            // expected
        }
    }

    @Test
    fun getAllCountries_mergesRegionResponses() = runBlocking {
        repeat(5) {
            server.enqueue(
                MockResponse()
                    .setBody(TestFixtures.singleCountryJsonArray(commonName = "ApiLand", cca2 = "AP"))
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
            )
        }
        val countries = repository().getAllCountries()
        assertEquals(1, countries.size)
        assertEquals("ApiLand", countries.first().displayName)
    }

    @Test
    fun getAllCountries_regionFailure_throws() = runBlocking {
        repeat(5) {
            server.enqueue(MockResponse().setResponseCode(500))
        }
        try {
            repository().getAllCountries()
            fail("Expected region load failure")
        } catch (e: IOException) {
            assertTrue(e.message!!.contains("регион"))
        }
    }

    @Test
    fun searchCountries_apiError_withoutCache_throws() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        try {
            repository().searchCountries("query")
            fail("Expected search API error")
        } catch (_: Exception) {
            // expected
        }
    }
}
