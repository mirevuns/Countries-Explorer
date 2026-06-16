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
        return v5ResponseBody(
            """{
            "names":{"common":"$esc","official":"$esc"},
            "codes":{"alpha_2":"$cca2","alpha_3":"${cca2}A"},
            "capitals":[{"name":"Capital City"}],
            "population":1000000,
            "area":{"kilometers":100.0},
            "region":"Europe",
            "subregion":"North",
            "flag":{"url_png":"https://example.com/f.png","url_svg":null},
            "languages":[{"bcp47":"eng","name":"English"}],
            "currencies":[{"code":"TST","name":"Test","symbol":"T"}],
            "timezones":["UTC"],
            "borders":[]
        }"""
        )
    }

    fun v5ResponseBody(objectJson: String, more: Boolean = false): String {
        return """{"data":{"objects":[$objectJson],"meta":{"total":1,"count":1,"limit":100,"offset":0,"more":$more}}}"""
            .trimIndent()
            .replace("\n", "")
    }

    fun v5EmptyResponseBody(): String =
        """{"data":{"objects":[],"meta":{"total":0,"count":0,"limit":100,"offset":0,"more":false}}}"""
}
