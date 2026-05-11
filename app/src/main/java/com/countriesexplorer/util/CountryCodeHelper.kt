package com.countriesexplorer.util

import com.countriesexplorer.data.model.Country

object CountryCodeHelper {
    fun getCountryCode(country: Country): String {
        return country.countryCode.ifEmpty {
            country.displayName.take(2).uppercase()
        }
    }
    
    fun getFlagUrl(country: Country): String {
        return country.flags.png ?: country.flags.svg ?: ""
    }
}
