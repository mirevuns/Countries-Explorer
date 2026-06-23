package com.countriesexplorer.data.local

import androidx.room.Entity
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.util.CountryCodeHelper

@Entity(
    tableName = "collection_countries",
    primaryKeys = ["collectionId", "countryCode"]
)
data class CollectionCountryEntity(
    val collectionId: Long,
    val countryCode: String,
    val countryName: String,
    val flagUrl: String,
    val addedAt: Long
) {
    companion object {
        fun fromCountry(collectionId: Long, country: Country, addedAt: Long = System.currentTimeMillis()): CollectionCountryEntity {
            return CollectionCountryEntity(
                collectionId = collectionId,
                countryCode = CountryCodeHelper.getCountryCode(country),
                countryName = country.displayName,
                flagUrl = CountryCodeHelper.getFlagUrl(country),
                addedAt = addedAt
            )
        }
    }
}
