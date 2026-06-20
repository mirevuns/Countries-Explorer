package com.countriesexplorer.data.local

import androidx.room.TypeConverter
import com.countriesexplorer.data.model.Country
import com.google.gson.Gson

class CountryTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromCountry(country: Country?): String? = country?.let { gson.toJson(it) }

    @TypeConverter
    fun toCountry(json: String?): Country? =
        json?.let { gson.fromJson(it, Country::class.java) }
}
