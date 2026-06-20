package com.countriesexplorer.data.repository

import com.countriesexplorer.data.model.Country

data class CountriesLoadResult(
    val countries: List<Country>,
    val isStale: Boolean,
    val lastSyncAt: Long?,
    val isOffline: Boolean
)

data class CountryLoadResult(
    val country: Country,
    val isStale: Boolean,
    val isOffline: Boolean
)
