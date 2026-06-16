package com.countriesexplorer.data.api

import com.countriesexplorer.data.api.dto.V5CountriesResponse
import com.countriesexplorer.data.api.dto.V5CountryDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CountriesApi {
    @GET("countries/v5/region/{region}")
    suspend fun getCountriesByRegion(
        @Path("region") region: String,
        @Query("response_fields") responseFields: String = V5CountryDto.DEFAULT_RESPONSE_FIELDS,
        @Query("limit") limit: Int = V5CountryDto.DEFAULT_PAGE_LIMIT,
        @Query("offset") offset: Int = 0
    ): V5CountriesResponse

    @GET("countries/v5/name")
    suspend fun searchCountries(
        @Query("q") query: String,
        @Query("response_fields") responseFields: String = V5CountryDto.DEFAULT_RESPONSE_FIELDS,
        @Query("limit") limit: Int = V5CountryDto.DEFAULT_PAGE_LIMIT,
        @Query("offset") offset: Int = 0
    ): V5CountriesResponse

    @GET("countries/v5/code")
    suspend fun getCountryByCode(
        @Query("q") code: String,
        @Query("response_fields") responseFields: String = V5CountryDto.DEFAULT_RESPONSE_FIELDS,
        @Query("limit") limit: Int = V5CountryDto.DEFAULT_PAGE_LIMIT,
        @Query("offset") offset: Int = 0
    ): V5CountriesResponse
}
