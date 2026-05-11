package com.countriesexplorer

import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.model.Currency
import com.countriesexplorer.data.model.Flags
import com.countriesexplorer.data.model.Name

object AndroidTestFixtures {

    fun country(commonName: String = "RoomLand", cca2: String = "RL"): Country = Country(
        name = Name(common = commonName, official = commonName, nativeName = null),
        cca2 = cca2,
        cca3 = "${cca2}A",
        capital = listOf("C"),
        population = 100L,
        area = 1.0,
        region = "Europe",
        subregion = null,
        flags = Flags(png = "https://x/p.png", svg = null),
        languages = null,
        currencies = mapOf("X" to Currency("X", "x")),
        timezones = null,
        borders = null
    )
}
