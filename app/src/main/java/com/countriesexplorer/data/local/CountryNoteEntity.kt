package com.countriesexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "country_notes")
data class CountryNoteEntity(
    @PrimaryKey
    val countryCode: String,
    val text: String,
    val updatedAt: Long
)
