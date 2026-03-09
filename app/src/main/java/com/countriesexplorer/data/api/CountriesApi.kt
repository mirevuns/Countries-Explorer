package com.countriesexplorer.data.api

import com.countriesexplorer.data.model.Country
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CountriesApi {
    @GET("v3.1/region/{region}")
    suspend fun getCountriesByRegion(
        @Path("region") region: String,
        @Query("fields") fields: String = "name,cca2,cca3,capital,population,area,region,subregion,flags,languages,currencies,timezones,borders"
    ): List<Country>
    
    @GET("v3.1/name/{name}")
    suspend fun searchCountries(@Path("name") name: String): List<Country>
    
    @GET("v3.1/alpha/{code}")
    suspend fun getCountryByCode(@Path("code") code: String): List<Country>
}
