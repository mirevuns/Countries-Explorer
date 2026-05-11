package com.countriesexplorer.data.local

import androidx.room.TypeConverter
import com.countriesexplorer.data.model.Country
import com.google.gson.Gson

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun countryToJson(country: Country): String = gson.toJson(country)

    @TypeConverter
    fun jsonToCountry(json: String): Country = gson.fromJson(json, Country::class.java)
}
