package com.countriesexplorer

import com.countriesexplorer.data.model.Country
import com.countriesexplorer.data.model.Currency
import com.countriesexplorer.data.model.Flags
import com.countriesexplorer.data.model.Name

object TestFixtures {

    fun country(
        commonName: String = "Testland",
        cca2: String = "TL",
        population: Long = 1_000_000L
    ): Country = Country(
        name = Name(common = commonName, official = commonName, nativeName = null),
        cca2 = cca2,
        cca3 = "${cca2}A",
        capital = listOf("Capital City"),
        population = population,
        area = 100.0,
        region = "Europe",
        subregion = "North",
        flags = Flags(png = "https://example.com/flag.png", svg = null),
        languages = mapOf("eng" to "English"),
        currencies = mapOf("TST" to Currency(name = "Test", symbol = "T")),
        timezones = listOf("UTC"),
        borders = emptyList()
    )

    fun singleCountryJsonArray(commonName: String = "Testland", cca2: String = "TL"): String {
        val esc = commonName.replace("\"", "\\\"")
        return """[{
            "name":{"common":"$esc","official":"$esc"},
            "cca2":"$cca2",
            "cca3":"${cca2}A",
            "capital":["Capital City"],
            "population":1000000,
            "area":100.0,
            "region":"Europe",
            "subregion":"North",
            "flags":{"png":"https://example.com/f.png","svg":null},
            "languages":{"eng":"English"},
            "currencies":{"TST":{"name":"Test","symbol":"T"}},
            "timezones":["UTC"],
            "borders":[]
        }]""".trimIndent().replace("\n", "")
    }
}
