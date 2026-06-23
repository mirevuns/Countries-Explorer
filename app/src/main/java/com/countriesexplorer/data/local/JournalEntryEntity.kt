package com.countriesexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.util.CountryCodeHelper

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val countryCode: String?,
    val countryName: String,
    val flagUrl: String,
    val text: String,
    val createdAt: Long
) {
    companion object {
        fun fromVisit(country: Country, profileId: Long, createdAt: Long = System.currentTimeMillis()): JournalEntryEntity {
            return JournalEntryEntity(
                profileId = profileId,
                countryCode = CountryCodeHelper.getCountryCode(country),
                countryName = country.displayName,
                flagUrl = CountryCodeHelper.getFlagUrl(country),
                text = "Просмотр: ${country.displayName}",
                createdAt = createdAt
            )
        }

        fun manual(profileId: Long, text: String, createdAt: Long = System.currentTimeMillis()): JournalEntryEntity {
            return JournalEntryEntity(
                profileId = profileId,
                countryCode = null,
                countryName = "",
                flagUrl = "",
                text = text.trim(),
                createdAt = createdAt
            )
        }
    }
}
