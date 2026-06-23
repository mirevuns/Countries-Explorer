package com.countriesexplorer

object TestResponses {

    fun singleCountryJsonArray(): String = """{
        "names":{"common":"Testland","official":"Testland"},
        "codes":{"alpha_2":"TL","alpha_3":"TLA"},
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
    }""".trimIndent().replace("\n", "")

    private fun v5Response(objectJson: String): String =
        """{"data":{"objects":[$objectJson],"meta":{"total":1,"count":1,"limit":100,"offset":0,"more":false}}}"""

    fun regionCountriesJsonArray(): String = v5Response(singleCountryJsonArray())

    fun searchCountriesJsonArray(): String = v5Response(singleCountryJsonArray())

    fun singleCountryJsonResponse(): String = v5Response(singleCountryJsonArray())
}
