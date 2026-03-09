package com.countriesexplorer.data.repository

import com.countriesexplorer.data.api.CountriesApi
import com.countriesexplorer.data.model.Country
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountriesRepository @Inject constructor(
    private val api: CountriesApi
) {
    private var cachedCountries: List<Country>? = null
    
    private val regions = listOf("Africa", "Americas", "Asia", "Europe", "Oceania", "Antarctic")
    
    suspend fun getAllCountries(): List<Country> {
        return try {
            val countries = loadAllCountriesByRegions()
            cachedCountries = countries
            countries
        } catch (e: Exception) {
            val cached = cachedCountries
            if (cached != null && cached.isNotEmpty()) {
                return cached
            }
            val errorMsg = e.message ?: ""
            if (errorMsg.contains("400") || errorMsg.contains("Bad Request")) {
                return emptyList()
            }
            throw e
        }
    }
    
    private suspend fun loadAllCountriesByRegions(): List<Country> = coroutineScope {
        val results = regions.map { region ->
            async {
                try {
                    api.getCountriesByRegion(region)
                } catch (e: Exception) {
                    emptyList<Country>()
                }
            }
        }.awaitAll()
        val countries = results.flatten().distinctBy { it.cca2 ?: it.cca3 ?: "" }
            .sortedBy { it.displayName }
        if (countries.isEmpty() && cachedCountries.isNullOrEmpty()) {
            throw java.io.IOException("Нет подключения к интернету. Проверьте сеть и попробуйте снова.")
        }
        countries
    }
    
    suspend fun searchCountries(query: String): List<Country> {
        if (query.isBlank()) {
            return getAllCountries()
        }
        val cached = cachedCountries
        if (cached != null) {
            val filtered = cached.filterByQuery(query)
            if (filtered.isNotEmpty()) return filtered
        }
        return try {
            api.searchCountries(query)
        } catch (e: Exception) {
            cached?.filterByQuery(query) ?: emptyList()
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
    
    suspend fun getCountryByCode(code: String): Country? {
        return try {
            api.getCountryByCode(code).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
