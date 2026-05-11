package com.countriesexplorer

object TestResponses {

    fun singleCountryJsonArray(): String = """[{
        "name":{"common":"Testland","official":"Testland"},
        "cca2":"TL",
        "cca3":"TLA",
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
