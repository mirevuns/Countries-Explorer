package com.countriesexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.util.CountryCodeHelper

@Entity(tableName = "visit_history")
data class VisitHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val countryCode: String,
    val countryName: String,
    val flagUrl: String,
    val visitedAt: Long
) {
    companion object {
        fun fromCountry(
            country: Country,
            profileId: Long,
            visitedAt: Long = System.currentTimeMillis()
        ): VisitHistoryEntity {
            return VisitHistoryEntity(
                profileId = profileId,
                countryCode = CountryCodeHelper.getCountryCode(country),
                countryName = country.displayName,
                flagUrl = CountryCodeHelper.getFlagUrl(country),
                visitedAt = visitedAt
            )
        }
    }
}
