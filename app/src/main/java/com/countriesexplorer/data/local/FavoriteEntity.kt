package com.countriesexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.util.CountryCodeHelper

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val country: Country
) {
    companion object {
        fun fromCountry(country: Country): FavoriteEntity {
            val code = CountryCodeHelper.getCountryCode(country)
            return FavoriteEntity(
                code = code,
                name = country.displayName,
                country = country
            )
        }
    }
}
