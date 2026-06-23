package com.countriesexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "country_notes",
    primaryKeys = ["profileId", "countryCode"]
)
data class CountryNoteEntity(
    val profileId: Long,
    val countryCode: String,
    val text: String,
    val updatedAt: Long
)
