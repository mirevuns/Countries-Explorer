package com.countriesexplorer.data.local

import com.countriesexplorer.TestFixtures
import com.countriesexplorer.data.repository.ProfileRepository
import com.countriesexplorer.util.CountryCodeHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteEntityTest {

    @Test
    fun fromCountry_storesDisplayFieldsOnly() {
        val c = TestFixtures.country("Wonderland", "WD")
        val e = FavoriteEntity.fromCountry(c, ProfileRepository.DEFAULT_PROFILE_ID)
        assertEquals("WD", e.code)
        assertEquals("Wonderland", e.name)
        assertEquals(c.region, e.region)
        assertEquals(CountryCodeHelper.getFlagUrl(c), e.flagUrl)
    }
}
