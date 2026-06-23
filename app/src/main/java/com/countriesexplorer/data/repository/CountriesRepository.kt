package com.countriesexplorer.data.repository

import com.countriesexplorer.data.api.CountriesApi
import com.countriesexplorer.data.api.dto.V5CountryDto
import com.countriesexplorer.data.api.dto.countries
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.model.Currency
import com.countriesexplorer.data.model.Flags
import com.countriesexplorer.data.model.Name
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountriesRepository @Inject constructor(
    private val api: CountriesApi
) {
    private val cacheMutex = Mutex()
    private var cachedCountries: List<Country>? = null

    private val regions = listOf("Africa", "Americas", "Asia", "Europe", "Oceania")

    suspend fun getAllCountries(forceRefresh: Boolean = false): List<Country> {
        return try {
            val countries = loadAllCountriesByRegions()
            cacheMutex.withLock { cachedCountries = countries }
            countries
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 401) {
                throw IOException(
                    "REST Countries API: требуется API-ключ. Добавь REST_COUNTRIES_API_KEY в local.properties и пересобери приложение.",
                    e
                )
            }
            if (!forceRefresh) {
                val cached = cacheMutex.withLock { cachedCountries }
                if (cached != null && cached.isNotEmpty()) {
                    return cached
                }
            }
            throw e
        }
    }

    private suspend fun loadAllCountriesByRegions(): List<Country> = coroutineScope {
        val regionResults = regions.map { region ->
            async {
                try {
                    region to fetchAllCountriesInRegion(region)
                } catch (e: Exception) {
                    if (e is HttpException && e.code() == 401) throw e
                    region to null
                }
            }
        }.awaitAll()

        val failedRegions = regionResults.filter { it.second == null }.map { it.first }
        if (failedRegions.isNotEmpty()) {
            throw IOException(
                "Не удалось загрузить регионы: ${failedRegions.joinToString()}. Проверьте сеть и попробуйте снова."
            )
        }

        val countries = regionResults
            .flatMap { it.second.orEmpty() }
            .map { it.toCountry() }
            .distinctBy { it.cca2 ?: it.cca3 ?: "" }
            .filter { it.countryCode.isNotBlank() }
            .sortedBy { it.displayName }

        if (countries.isEmpty()) {
            throw IOException("Нет подключения к интернету. Проверьте сеть и попробуйте снова.")
        }
        countries
    }

    private suspend fun fetchAllCountriesInRegion(region: String): List<V5CountryDto> {
        val allCountries = mutableListOf<V5CountryDto>()
        var offset = 0
        val limit = V5CountryDto.DEFAULT_PAGE_LIMIT

        while (true) {
            val response = api.getCountriesByRegion(
                region = region,
                limit = limit,
                offset = offset
            )
            val page = response.countries()
            allCountries += page
            if (response.data?.meta?.more != true || page.isEmpty()) break
            offset += limit
        }
        return allCountries
    }

    private suspend fun fetchAllCountries(query: String): List<V5CountryDto> {
        val allCountries = mutableListOf<V5CountryDto>()
        var offset = 0
        val limit = V5CountryDto.DEFAULT_PAGE_LIMIT

        while (true) {
            val response = api.searchCountries(
                query = query,
                limit = limit,
                offset = offset
            )
            val page = response.countries()
            allCountries += page
            if (response.data?.meta?.more != true || page.isEmpty()) break
            offset += limit
        }
        return allCountries
    }

    suspend fun searchCountries(query: String): List<Country> {
        if (query.isBlank()) {
            return getAllCountries()
        }
        val cached = cacheMutex.withLock { cachedCountries }
        if (cached != null) {
            val filtered = cached.filterByQuery(query)
            if (filtered.isNotEmpty()) return filtered
        }
        return try {
            fetchAllCountries(query).map { it.toCountry() }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw IOException(
                    "REST Countries API: требуется API-ключ. Добавь REST_COUNTRIES_API_KEY в local.properties и пересобери приложение.",
                    e
                )
            }
            throw e
        } catch (e: Exception) {
            val offline = cached?.filterByQuery(query)
            if (!offline.isNullOrEmpty()) {
                return offline
            }
            throw e
        }
    }

    private fun List<Country>.filterByQuery(query: String) = filter { country ->
        val name = country.name.common ?: country.name.official ?: ""
        val official = country.name.official ?: ""
        val capitals = country.capital?.joinToString(" ") ?: ""
        val languages = country.languages?.values?.joinToString(" ") ?: ""
        name.contains(query, ignoreCase = true) ||
            official.contains(query, ignoreCase = true) ||
            capitals.contains(query, ignoreCase = true) ||
            languages.contains(query, ignoreCase = true)
    }

    suspend fun getCountryByCode(code: String): Country {
        return try {
            api.getCountryByCode(code)
                .countries()
                .firstOrNull { country ->
                    country.codes?.alpha2.equals(code, ignoreCase = true) ||
                        country.codes?.alpha3.equals(code, ignoreCase = true)
                }
                ?.toCountry()
                ?: throw CountryNotFoundException(code)
        } catch (e: CountryNotFoundException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw IOException(
                    "REST Countries API: требуется API-ключ. Добавь REST_COUNTRIES_API_KEY в local.properties и пересобери приложение.",
                    e
                )
            }
            throw e
        } catch (e: IOException) {
            throw IOException(
                "Нет подключения к интернету. Проверьте сеть и попробуйте снова.",
                e
            )
        }
    }

    private fun V5CountryDto.toCountry(): Country {
        val nameDto = names
        val codesDto = codes
        return Country(
            name = Name(
                common = nameDto?.common,
                official = nameDto?.official,
                nativeName = null
            ),
            cca2 = codesDto?.alpha2,
            cca3 = codesDto?.alpha3,
            capital = capitals?.mapNotNull { it.name },
            population = population ?: 0L,
            area = area?.kilometers,
            region = region.orEmpty(),
            subregion = subregion,
            flags = Flags(png = flag?.urlPng, svg = flag?.urlSvg),
            languages = languages
                ?.mapNotNull { language ->
                    val key = language.bcp47 ?: language.iso6391 ?: language.name
                    val value = language.name
                    if (key.isNullOrBlank() || value.isNullOrBlank()) null else key to value
                }
                ?.toMap(),
            currencies = currencies
                ?.mapNotNull { currency ->
                    val code = currency.code
                    if (code.isNullOrBlank()) null else code to Currency(currency.name, currency.symbol)
                }
                ?.toMap(),
            timezones = timezones,
            borders = borders
        )
    }
}
