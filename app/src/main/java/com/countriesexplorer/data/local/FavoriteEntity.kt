package com.countriesexplorer.data.local

import androidx.room.Entity
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.util.CountryCodeHelper

@Entity(
    tableName = "favorites",
    primaryKeys = ["profileId", "code"]
)
data class FavoriteEntity(
    val profileId: Long,
    val code: String,
    val name: String,
    val region: String,
    val flagUrl: String
) {
    companion object {
        fun fromCountry(country: Country, profileId: Long): FavoriteEntity {
            val code = CountryCodeHelper.getCountryCode(country)
            return FavoriteEntity(
                profileId = profileId,
                code = code,
                name = country.displayName,
                region = country.region,
                flagUrl = CountryCodeHelper.getFlagUrl(country)
            )
        }
    }
}
