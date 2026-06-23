package com.countriesexplorer.data.repository

import com.countriesexplorer.data.api.CountriesApi
import com.countriesexplorer.data.api.dto.V5CountryDto
import com.countriesexplorer.data.api.dto.countries
import com.countriesexplorer.data.local.CacheMetadataEntity
import com.countriesexplorer.data.local.CachedCountryEntity
import com.countriesexplorer.data.local.CacheMetadataDao
import com.countriesexplorer.data.local.CountryCacheDao
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.model.Currency
import com.countriesexplorer.data.model.Flags
import com.countriesexplorer.data.model.Name
import com.countriesexplorer.data.preferences.AppSettingsRepository
import com.countriesexplorer.util.NetworkMonitor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountriesRepository @Inject constructor(
    private val api: CountriesApi,
    private val countryCacheDao: CountryCacheDao,
    private val cacheMetadataDao: CacheMetadataDao,
    private val appSettingsRepository: AppSettingsRepository,
    private val networkMonitor: NetworkMonitor,
    private val visitHistoryRepository: VisitHistoryRepository
) {
    private val regions = listOf("Africa", "Americas", "Asia", "Europe", "Oceania")

    suspend fun getAllCountries(forceRefresh: Boolean = false): CountriesLoadResult {
        val settings = appSettingsRepository.currentSettings()
        val metadata = cacheMetadataDao.get()
        val lastSyncAt = metadata?.lastFullSyncAt
        val localCountries = loadCountriesFromCache()
        val isStale = CachePolicy.isStale(lastSyncAt, settings.cacheTtlHours)

        if (!networkMonitor.isConnected) {
            if (localCountries.isNotEmpty()) {
                return CountriesLoadResult(
                    countries = localCountries,
                    isStale = isStale,
                    lastSyncAt = lastSyncAt,
                    isOffline = true
                )
            }
            throw IOException("Нет подключения к интернету. Проверьте сеть и попробуйте снова.")
        }

        if (localCountries.isNotEmpty() && !forceRefresh && !isStale) {
            return CountriesLoadResult(
                countries = localCountries,
                isStale = false,
                lastSyncAt = lastSyncAt,
                isOffline = false
            )
        }

        if (networkMonitor.shouldBlockSync(settings.wifiOnlySync)) {
            if (localCountries.isNotEmpty()) {
                return CountriesLoadResult(
                    countries = localCountries,
                    isStale = isStale,
                    lastSyncAt = lastSyncAt,
                    isOffline = false,
                    syncBlocked = true
                )
            }
            throw IOException("Синхронизация доступна только по Wi-Fi. Подключитесь к Wi-Fi или отключите ограничение в настройках.")
        }

        return try {
            val fresh = fetchFromNetworkAndPersist()
            CountriesLoadResult(
                countries = fresh,
                isStale = false,
                lastSyncAt = System.currentTimeMillis(),
                isOffline = false
            )
        } catch (e: Exception) {
            handleApiException(e)
            if (localCountries.isNotEmpty()) {
                return CountriesLoadResult(
                    countries = localCountries,
                    isStale = true,
                    lastSyncAt = lastSyncAt,
                    isOffline = !networkMonitor.isConnected
                )
            }
            throw e
        }
    }

    suspend fun syncCacheIfNeeded(force: Boolean = false): SyncResult {
        val settings = appSettingsRepository.currentSettings()
        if (!settings.autoRefreshEnabled && !force) {
            return SyncResult.Skipped("Автообновление отключено")
        }
        if (!networkMonitor.isConnected) {
            return SyncResult.Skipped("Нет сети")
        }
        if (networkMonitor.shouldBlockSync(settings.wifiOnlySync)) {
            return SyncResult.Skipped("Только Wi-Fi")
        }

        val metadata = cacheMetadataDao.get()
        val isStale = CachePolicy.isStale(metadata?.lastFullSyncAt, settings.cacheTtlHours)
        if (!force && !isStale && countryCacheDao.count() > 0) {
            return SyncResult.Skipped("Кэш актуален")
        }

        return try {
            fetchFromNetworkAndPersist()
            SyncResult.Success
        } catch (e: Exception) {
            handleApiException(e)
            cacheMetadataDao.upsert(
                CacheMetadataEntity(
                    lastFullSyncAt = metadata?.lastFullSyncAt,
                    lastSyncStatus = CacheMetadataEntity.STATUS_FAILED
                )
            )
            SyncResult.Failed(e.message ?: "Ошибка синхронизации")
        }
    }

    suspend fun preloadForOffline(): SyncResult {
        val settings = appSettingsRepository.currentSettings()
        if (!networkMonitor.isConnected) {
            return SyncResult.Skipped("Нет сети")
        }
        if (settings.preloadOnWifi && !networkMonitor.isOnWifi) {
            return SyncResult.Skipped("Предзагрузка доступна только по Wi-Fi")
        }
        return syncCacheIfNeeded(force = true)
    }

    suspend fun searchCountries(query: String): CountriesLoadResult {
        if (query.isBlank()) {
            return getAllCountries()
        }

        val allResult = getAllCountries()
        val filtered = allResult.countries.filterByQuery(query)
        if (filtered.isNotEmpty()) {
            return allResult.copy(countries = filtered)
        }

        if (!networkMonitor.isConnected) {
            return allResult.copy(countries = emptyList())
        }

        return try {
            val remote = fetchAllCountries(query).map { it.toCountry() }
            persistCountries(remote, markFullSync = false)
            val metadata = cacheMetadataDao.get()
            CountriesLoadResult(
                countries = remote,
                isStale = cacheIsStale(),
                lastSyncAt = metadata?.lastFullSyncAt,
                isOffline = false
            )
        } catch (e: Exception) {
            handleApiException(e)
            throw e
        }
    }

    suspend fun getCountryByCode(code: String, recordVisit: Boolean = true): CountryLoadResult {
        val cached = countryCacheDao.getByCode(code)?.country

        if (!networkMonitor.isConnected) {
            if (cached != null) {
                if (recordVisit) visitHistoryRepository.recordVisit(cached)
                return countryFromCache(cached, isOffline = true)
            }
            throw IOException("Нет подключения к интернету. Проверьте сеть и попробуйте снова.")
        }

        if (networkMonitor.shouldBlockSync(appSettingsRepository.currentSettings().wifiOnlySync) && cached != null) {
            if (recordVisit) visitHistoryRepository.recordVisit(cached)
            return countryFromCache(cached, isOffline = false, syncBlocked = true)
        }

        return try {
            val country = fetchCountryFromNetwork(code)
            persistCountries(listOf(country), markFullSync = false)
            if (recordVisit) visitHistoryRepository.recordVisit(country)
            CountryLoadResult(country, isStale = cacheIsStale(), isOffline = false)
        } catch (e: CountryNotFoundException) {
            throw e
        } catch (e: Exception) {
            handleApiException(e)
            if (cached != null) {
                if (recordVisit) visitHistoryRepository.recordVisit(cached)
                return countryFromCache(cached, isOffline = false)
            }
            throw e
        }
    }

    private suspend fun cacheIsStale(): Boolean {
        val settings = appSettingsRepository.currentSettings()
        return CachePolicy.isStale(cacheMetadataDao.get()?.lastFullSyncAt, settings.cacheTtlHours)
    }

    private suspend fun countryFromCache(
        country: Country,
        isOffline: Boolean,
        syncBlocked: Boolean = false
    ): CountryLoadResult {
        return CountryLoadResult(
            country = country,
            isStale = cacheIsStale(),
            isOffline = isOffline,
            syncBlocked = syncBlocked
        )
    }

    private suspend fun loadCountriesFromCache(): List<Country> {
        return countryCacheDao.getAll()
            .map { it.country }
            .distinctBy { it.countryCode }
            .filter { it.countryCode.isNotBlank() }
            .sortedBy { it.displayName.lowercase() }
    }

    private suspend fun fetchFromNetworkAndPersist(): List<Country> {
        val countries = loadAllCountriesByRegions()
        persistCountries(countries, markFullSync = true)
        return countries
    }

    private suspend fun persistCountries(countries: List<Country>, markFullSync: Boolean = false) {
        val now = System.currentTimeMillis()
        val entities = countries.map { CachedCountryEntity.fromCountry(it, now) }
        countryCacheDao.insertAll(entities)
        if (markFullSync) {
            cacheMetadataDao.upsert(
                CacheMetadataEntity(
                    lastFullSyncAt = now,
                    lastSyncStatus = CacheMetadataEntity.STATUS_SUCCESS
                )
            )
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

    private suspend fun fetchCountryFromNetwork(code: String): Country {
        return api.getCountryByCode(code)
            .countries()
            .firstOrNull { country ->
                country.codes?.alpha2.equals(code, ignoreCase = true) ||
                    country.codes?.alpha3.equals(code, ignoreCase = true)
            }
            ?.toCountry()
            ?: throw CountryNotFoundException(code)
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

    private fun handleApiException(e: Exception) {
        if (e is HttpException && e.code() == 401) {
            throw IOException(
                "REST Countries API: требуется API-ключ. Добавь REST_COUNTRIES_API_KEY в local.properties и пересобери приложение.",
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

sealed class SyncResult {
    data object Success : SyncResult()
    data class Skipped(val reason: String) : SyncResult()
    data class Failed(val message: String) : SyncResult()
}
