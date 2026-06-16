package com.countriesexplorer.util

import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.model.Flags
import com.countriesexplorer.data.model.Name
import org.junit.Assert.assertEquals
import org.junit.Test

class CountryCodeHelperTest {

    @Test
    fun getCountryCode_prefersCca2() {
        assertEquals("TL", CountryCodeHelper.getCountryCode(TestFixtures.country(commonName = "T", cca2 = "TL")))
    }

    @Test
    fun getCountryCode_fallsBackToCca3WhenCca2Missing() {
        val country = Country(
            name = Name(common = "Testland", official = "Testland", nativeName = null),
            cca2 = null,
            cca3 = "TLA",
            capital = null,
            population = 0L,
            area = null,
            region = "R",
            subregion = null,
            flags = Flags(null, null),
            languages = null,
            currencies = null,
            timezones = null,
            borders = null
        )
        assertEquals("TLA", CountryCodeHelper.getCountryCode(country))
    }
}
