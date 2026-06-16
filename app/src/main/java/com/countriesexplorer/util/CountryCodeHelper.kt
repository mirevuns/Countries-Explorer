package com.countriesexplorer.util

import com.countriesexplorer.data.model.Country

object CountryCodeHelper {
    fun getCountryCode(country: Country): String {
        return country.cca2?.takeIf { it.isNotBlank() }
            ?: country.cca3?.takeIf { it.isNotBlank() }
            ?: ""
    }
    
    fun getFlagUrl(country: Country): String {
        return country.flags.png ?: country.flags.svg ?: ""
    }
}
