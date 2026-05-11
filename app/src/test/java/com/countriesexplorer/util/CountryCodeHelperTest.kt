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
    fun getCountryCode_emptyIso_usesFirstTwoLettersOfDisplayName() {
        val c = Country(
            name = Name(common = "Abcd", official = "Abcd", nativeName = null),
            cca2 = "",
            cca3 = "",
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
        assertEquals("AB", CountryCodeHelper.getCountryCode(c))
    }
}
