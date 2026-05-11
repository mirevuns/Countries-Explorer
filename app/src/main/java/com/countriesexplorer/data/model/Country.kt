package com.countriesexplorer.data.model

import com.google.gson.annotations.SerializedName

data class Country(
    @SerializedName("name")
    val name: Name,
    @SerializedName("cca2")
    val cca2: String?,
    @SerializedName("cca3")
    val cca3: String?,
    @SerializedName("capital")
    val capital: List<String>?,
    @SerializedName("population")
    val population: Long,
    @SerializedName("area")
    val area: Double?,
    @SerializedName("region")
    val region: String,
    @SerializedName("subregion")
    val subregion: String?,
    @SerializedName("flags")
    val flags: Flags,
    @SerializedName("languages")
    val languages: Map<String, String>?,
    @SerializedName("currencies")
    val currencies: Map<String, Currency>?,
    @SerializedName("timezones")
    val timezones: List<String>?,
    @SerializedName("borders")
    val borders: List<String>?
) {
    val displayName: String
        get() = name.common ?: name.official ?: ""
    
    val countryCode: String
        get() = cca2 ?: cca3 ?: ""
}

data class Name(
    @SerializedName("common")
    val common: String?,
    @SerializedName("official")
    val official: String?,
    @SerializedName("nativeName")
    val nativeName: Map<String, NativeName>?
)

data class NativeName(
    @SerializedName("common")
    val common: String?,
    @SerializedName("official")
    val official: String?
)

data class Flags(
    @SerializedName("png")
    val png: String?,
    @SerializedName("svg")
    val svg: String?
)

data class Currency(
    @SerializedName("name")
    val name: String?,
    @SerializedName("symbol")
    val symbol: String?
)
