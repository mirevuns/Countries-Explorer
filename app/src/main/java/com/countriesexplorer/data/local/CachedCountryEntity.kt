package com.countriesexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.util.CountryCodeHelper

@Entity(tableName = "cached_countries")
data class CachedCountryEntity(
    @PrimaryKey
    val code: String,
    val country: Country,
    val region: String,
    val cachedAt: Long
) {
    companion object {
        fun fromCountry(country: Country, cachedAt: Long = System.currentTimeMillis()): CachedCountryEntity {
            return CachedCountryEntity(
                code = CountryCodeHelper.getCountryCode(country),
                country = country,
                region = country.region,
                cachedAt = cachedAt
            )
        }
    }
}
