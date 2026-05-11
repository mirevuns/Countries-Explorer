package com.countriesexplorer.data.local

import com.countriesexplorer.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteEntityTest {

    @Test
    fun fromCountry_usesCountryCodeHelper() {
        val c = TestFixtures.country("Wonderland", "WD")
        val e = FavoriteEntity.fromCountry(c)
        assertEquals("WD", e.code)
        assertEquals("Wonderland", e.name)
        assertEquals(c, e.country)
    }
}
