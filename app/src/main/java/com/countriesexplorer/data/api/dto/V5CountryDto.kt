package com.countriesexplorer.data.api.dto

import com.google.gson.annotations.SerializedName

data class V5CountriesResponse(
    @SerializedName("data")
    val data: V5CountriesDataDto?
)

data class V5CountriesDataDto(
    @SerializedName("objects")
    val objects: List<V5CountryDto>?,
    @SerializedName("meta")
    val meta: V5MetaDto?
)

data class V5MetaDto(
    @SerializedName("more")
    val more: Boolean?,
    @SerializedName("offset")
    val offset: Int?,
    @SerializedName("limit")
    val limit: Int?,
    @SerializedName("total")
    val total: Int?
)

data class V5CountryDto(
    @SerializedName("names")
    val names: V5NamesDto?,
    @SerializedName("codes")
    val codes: V5CodesDto?,
    @SerializedName("capitals")
    val capitals: List<V5CapitalDto>?,
    @SerializedName("population")
    val population: Long?,
    @SerializedName("area")
    val area: V5AreaDto?,
    @SerializedName("region")
    val region: String?,
    @SerializedName("subregion")
    val subregion: String?,
    @SerializedName("flag")
    val flag: V5FlagDto?,
    @SerializedName("languages")
    val languages: List<V5LanguageDto>?,
    @SerializedName("currencies")
    val currencies: List<V5CurrencyItemDto>?,
    @SerializedName("timezones")
    val timezones: List<String>?,
    @SerializedName("borders")
    val borders: List<String>?
) {
    companion object {
        const val DEFAULT_RESPONSE_FIELDS =
            "names.common,names.official,codes.alpha_2,codes.alpha_3,capitals.name,population,area.kilometers,region,subregion,flag.url_png,flag.url_svg,languages.name,currencies.code,currencies.name,currencies.symbol,timezones,borders"

        const val DEFAULT_PAGE_LIMIT = 100
    }
}

data class V5NamesDto(
    @SerializedName("common")
    val common: String?,
    @SerializedName("official")
    val official: String?
)

data class V5CodesDto(
    @SerializedName("alpha_2")
    val alpha2: String?,
    @SerializedName("alpha_3")
    val alpha3: String?
)

data class V5CapitalDto(
    @SerializedName("name")
    val name: String?
)

data class V5AreaDto(
    @SerializedName("kilometers")
    val kilometers: Double?
)

data class V5FlagDto(
    @SerializedName("url_png")
    val urlPng: String?,
    @SerializedName("url_svg")
    val urlSvg: String?
)

data class V5LanguageDto(
    @SerializedName("bcp47")
    val bcp47: String?,
    @SerializedName("iso639_1")
    val iso6391: String?,
    @SerializedName("name")
    val name: String?
)

data class V5CurrencyItemDto(
    @SerializedName("code")
    val code: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("symbol")
    val symbol: String?
)

fun V5CountriesResponse.countries(): List<V5CountryDto> = data?.objects.orEmpty()
